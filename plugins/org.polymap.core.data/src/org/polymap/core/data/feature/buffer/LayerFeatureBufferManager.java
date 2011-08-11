/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import edu.emory.mathcs.backport.java.util.Collections;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.buffer.FeatureChangeEvent.Type;
import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The API and meditor of the feature buffer system.
 * <p/>
 * There is one buffer per layer per session. The buffer content is injected into the
 * feature pipeline by the {@link FeatureBufferProcessor}. The processor is installed
 * by the {@link InstallBufferPipelineListener}.
 * <p/>
 * The buffer manager handles save/revert events by writing down the buffer content
 * to the underlying data store. <b>Concurrent changes</b> are checked against the
 * original copy of the features that the buffer is providing via
 * {@link FeatureBufferState}. This strategy might be memory consuming but it is also
 * a robust way to check consurrent changes that does not depend on a timestamp in
 * the data type.
 * <p/>
 * The buffer manager writes down the changes to the underlying FeatureStore directly
 * without using the pipeline of the layer. So the buffer processor MUST be the first
 * after the {@link DataSourceProcessor}.
 * <p/>
 * XXX Currently the original state of an modified feature is requested from the
 * underlying store not before the modification request arrives. This may lead to
 * <b>lost updates</b> if the feature has changed meanwhile. In order to detect
 * <b>every</b> modification a cache could be used that holds the states of all
 * features read in this session.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class LayerFeatureBufferManager 
        implements IOperationSaveListener {

    private static Log log = LogFactory.getLog( LayerFeatureBufferManager.class );

    public static final FilterFactory   ff = CommonFactoryFinder.getFilterFactory( null );

    
    /**
     * The Session holds the managers of the session.
     */
    static class Session
            extends SessionSingletonBase { 
        
        protected WeakHashMap<ILayer,LayerFeatureBufferManager> managers = new WeakHashMap();
        
        public static Session instance() {
            return (Session)getInstance( Session.class );
        }
    }


    /**
     * Gets the buffer manager for the given layer of the current session. If no
     * manager exists yet than a new one is created with default buffer type/impl and
     * settings if <code>create</code> is true, otherwise null might be returned.
     * 
     * @param layer
     * @param create True specifies that a new buffer manager is created if
     *        necessary.
     * @return The buffer manager for the given layer.
     */
    public static LayerFeatureBufferManager forLayer( ILayer layer, boolean create ) {
        assert layer != null;
        
        WeakHashMap<ILayer, LayerFeatureBufferManager> managers = Session.instance().managers;
        synchronized (managers) {
            LayerFeatureBufferManager result = managers.get( layer );
            if (result == null && create) {
                result = new LayerFeatureBufferManager( layer );
                managers.put( layer, result );
            }
            return result;
        }
    }
    
   
    // instance *******************************************
    
    private ILayer                  layer;

    private IFeatureBuffer          buffer;
    
    private FeatureBufferProcessor  processor;
    
    private Transaction             tx;
    
    private ListenerList<IFeatureChangeListener> listeners = new ListenerList();
    
    private long                    storeVersion;
    

    protected LayerFeatureBufferManager( ILayer layer ) {
        super();
        this.layer = layer;
        this.storeVersion = FeatureStoreVersion.forLayer( layer );
        
        buffer = new MemoryFeatureBuffer();
        buffer.init( new IFeatureBufferSite() {
            public void fireFeatureChangeEvent( Type type, Collection<Feature> features ) {
                LayerFeatureBufferManager.this.fireFeatureChangeEvent( type, features );
            }
        });
        
        processor = new FeatureBufferProcessor( buffer );
        
        OperationSupport.instance().addOperationSaveListener( this );
    }

    
    public void addFeatureChangeListener( IFeatureChangeListener l ) {
        listeners.add( l );
    }
    
    public void removeFeatureChangeListener( IFeatureChangeListener l ) {
        listeners.remove( l );
    }
    
    protected void fireFeatureChangeEvent( FeatureChangeEvent.Type type, Collection<Feature> features ) {
        FeatureChangeEvent ev = new FeatureChangeEvent( this, type, features );
        for (IFeatureChangeListener l : listeners) {
            l.featureChange( ev );
        }
    }
    
    
    public ILayer getLayer() {
        return layer;
    }

    public IFeatureBuffer getBuffer() {
        return buffer;
    }

    public FeatureBufferProcessor getProcessor() {
        return processor;
    }
    
    public long getStoreVersion() {
        return storeVersion;
    }


    public void save( OperationSupport os, IProgressMonitor monitor ) {
        if (tx == null) {
            return;
        }
        try {
            monitor.beginTask( layer.getLabel() , buffer.size() );

            storeVersion = FeatureStoreVersion.checkSetForLayer( layer, storeVersion );
            try {
                tx.commit();
                buffer.clear();
            }
            finally {
                tx.close();
                tx = null;
            }
            monitor.done();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Unable to commit the transaction. The data has inconsistent state! Please contact the administrator.", e );
        }
    }


    public void rollback( OperationSupport os, IProgressMonitor monitor ) {
        if (tx == null) {
            return;
        }
        try {
            monitor.beginTask( layer.getLabel() , buffer.size() );
            try {
                tx.rollback();
            }
            finally {
                tx.close();
                tx = null;
            }
            monitor.done();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Unable to rollback transaction. The data has inconsistent state! Please contact the administrator.", e );
        }
    }


    public void prepareSave( OperationSupport os, IProgressMonitor monitor )
    throws Exception {
        if (tx != null) {
            throw new IllegalStateException( "Pending transaction found." );
        }

        if (buffer.isEmpty()) {
            return;
        }
        
        monitor.beginTask( layer.getLabel() , buffer.size() );
        tx = new DefaultTransaction( "Submit buffer: layer-" + layer.id() + "-" + System.currentTimeMillis() );

        // store directly to the underlying store; so the buffer processor MUST be
        // the first processor after the DataStoreProcessor
        IGeoResource geores = layer.getGeoResource();
        FeatureStore fs = geores.resolve( FeatureStore.class, null );
  
        fs.setTransaction( tx );

        int count = 0;
        for (FeatureBufferState buffered : buffer.content()) {
            if (monitor.isCanceled()) {
                return;
            }
            if (count++ % 10 == 0) {
                monitor.subTask( "Features: " + count );
            }
            if (buffered.isAdded()) {
                checkSubmitAdded( fs, buffered );
            }
            else if (buffered.isModified()) {
                checkSubmitModified( fs, buffered );
            }
            else if (buffered.isRemoved()) {
                checkSubmitRemoved( fs, buffered );
            }
            else {
                log.warn( "Buffered feature is not added/removed/modified!" );
            }
            monitor.worked( 1 );
        }
        monitor.done();
    }

    
    public void revert( OperationSupport os, IProgressMonitor monitor ) {
        try {
            monitor.beginTask( layer.getLabel() , buffer.size() );

            buffer.clear();
            fireFeatureChangeEvent( FeatureChangeEvent.Type.FLUSHED, null );
            
            monitor.done();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Unable to revert changes.", e );
        }
    }
    
    
    protected void checkSubmitAdded( FeatureStore fs, FeatureBufferState buffered ) 
    throws Exception {
        // check concurrent modifications
        FeatureId fid = buffered.feature().getIdentifier();
        Id fidFilter = ff.id( Collections.singleton( fid ) );
        
        if (!fs.getFeatures( fidFilter ).isEmpty()) {
            throw new ConcurrentModificationException( "Feature has been added concurrently: " + fid );
        }
        
        // write down
        FeatureCollection coll = FeatureCollections.newCollection();
        coll.add( buffered.feature() );
        fs.addFeatures( coll );
    }


    protected void checkSubmitModified( FeatureStore fs, FeatureBufferState buffered ) 
    throws Exception {
        // check concurrent modifications
        FeatureId fid = buffered.feature().getIdentifier();
        Id fidFilter = ff.id( Collections.singleton( fid ) );
        
        List<Feature> stored = loadFeatures( fs, fidFilter );         
        if (stored.size() == 0) {
            throw new ConcurrentModificationException( "Feature has been removed concurrently: " + fid );
        }
        else if (stored.size() > 1) {
            throw new IllegalStateException( "More than one feature for id: " + fid + "!?" );
        }
        if (isFeatureModified( stored.get( 0 ), buffered.original() )) {
            throw new ConcurrentModificationException( "Feature has been modified concurrently: " + fid );
        }
        
        // write down
        AttributeDescriptor[] type = {};
        Object[] value = {};
        
        for (Property origProp : buffered.original().getProperties()) {
            if (origProp.getDescriptor() instanceof AttributeDescriptor) {
                Property newProp = buffered.feature().getProperty( origProp.getName() );
                if (isPropertyModified( origProp.getValue(), newProp.getValue() )) {
                    type = (AttributeDescriptor[])ArrayUtils.add( type, origProp.getDescriptor() );
                    value = ArrayUtils.add( value, newProp.getValue() );
                    
                    log.info( "Attribute modified: " + origProp.getDescriptor().getName() + " = " + newProp.getValue() + " (" + fid.getID() + ")" );
                }
            }
        }
        fs.modifyFeatures( type, value, fidFilter );
    }

    
    protected void checkSubmitRemoved( FeatureStore fs, FeatureBufferState buffered ) 
    throws Exception {
        FeatureId fid = buffered.feature().getIdentifier();
        Id fidFilter = ff.id( Collections.singleton( fid ) );
        fs.removeFeatures( fidFilter );
    }

    
    private List<Feature> loadFeatures( FeatureStore fs, Filter filter )
    throws IOException {
        List<Feature> result = new ArrayList();
        
        FeatureCollection features = fs.getFeatures( filter );
        Iterator it = null;
        try {
            for (it=features.iterator(); it.hasNext(); ) {
                 result.add( (Feature)it.next() );
            }
        }
        finally {
            features.close( it );
        }
        return result;
    }


    private boolean isFeatureModified( Feature feature, Feature original ) 
    throws IOException {
        // XXX complex features
        SimpleFeatureType schema = ((SimpleFeature)original).getType(); 
        for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
            
            Object value1 = ((SimpleFeature)feature).getAttribute( attribute.getName() );
            Object value2 = ((SimpleFeature)original).getAttribute( attribute.getName() );
            
            if (isPropertyModified( value1, value2 )) {
                return true;
            }
        }
        return false;
    }

    
    private boolean isPropertyModified( Object value1, Object value2 ) {
        if (value1 instanceof Geometry) {
            if (!((Geometry)value1).equalsExact( (Geometry)value2 )) {
                return true;
            }
        }
        else if ((value1 != null && !value1.equals( value2 ))
                || value1 == null && value2 != null) {
            return true;
        }
        return false;
    }
    
}
