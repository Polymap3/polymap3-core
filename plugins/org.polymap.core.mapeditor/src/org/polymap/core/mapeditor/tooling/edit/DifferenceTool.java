/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.edit;

import static org.polymap.core.data.DataPlugin.ff;

import java.util.HashMap;
import java.util.Map;

import java.io.StringReader;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.ModifyFeaturesOperation;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
import org.polymap.openlayers.rap.widget.controls.KeyboardDefaultsControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.handler.PolygonHandler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DifferenceTool
        extends BaseLayerEditorTool 
        implements OpenLayersEventListener {

    private static final Log log = LogFactory.getLog( DifferenceTool.class );

    private EditVectorLayer         vectorLayer;

    private DrawFeatureControl      drawControl;

    private NavigationControl       naviControl;
    
    private KeyboardDefaultsControl keyboardControl;

    @Override
    public void init( IEditorToolSite site ) {
        super.init( site );
        
        additionalLayerFilter = new Predicate<ILayer>() {
            public boolean apply( ILayer input ) {
                return ACLUtils.checkPermission( input, AclPermission.WRITE, false );
            }
        };
    }
    

    @Override
    public void dispose() {
        if (isActive()) {
            onDeactivate();
        }
        super.dispose();
    }

    
    @Override
    public EditVectorLayer getVectorLayer() {
        return vectorLayer;
    }


    @Override
    public void onActivate() {
        super.onActivate();
        
        if (getSelectedLayer() == null) {
            return;
        }
//      // keyboardControl
//        keyboardControl = new KeyboardDefaultsControl();
//        getSite().getEditor().addControl( keyboardControl );
//        keyboardControl.activate();
//
//        // naviControl
//        naviControl = new NavigationControl();
//        getSite().getEditor().addControl( naviControl );
//        naviControl.activate();
        
        vectorLayer = new EditVectorLayer( getSite(), getSelectedLayer() );
        vectorLayer.activate();
        
        // re-create the styler controls if this activation is due to a layer change
        if (getParent() != null && !getParent().isDisposed()) {
            vectorLayer.getStyler().createPanelControl( getParent(), this );
            getParent().layout( true );
        }

        // control
        PolygonHandler handler = new PolygonHandler();
        drawControl = new DrawFeatureControl( vectorLayer.getVectorLayer(), handler );
        getSite().getEditor().addControl( drawControl );

        // register event handler
        Map<String,String> payload = new HashMap();
        payload.put( "features", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
        //vectorLayer.getVectorLayer().events.register( this, DrawFeatureControl.EVENT_ADDED, payload );
        drawControl.events.register( this, DrawFeatureControl.EVENT_ADDED, payload );

        drawControl.activate();
        vectorLayer.getVectorLayer().redraw();
        
        fireEvent( this, PROP_LAYER_ACTIVATED, getSelectedLayer() );
    }


    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );
        
        vectorLayer.getStyler().createPanelControl( parent, this );     
    }

    
    @Override
    public void onDeactivate() {
        super.onDeactivate();
        
        if (keyboardControl != null) {
            getSite().getEditor().removeControl( keyboardControl );
            keyboardControl.deactivate();
            keyboardControl.dispose();
            keyboardControl = null;
        }
        if (naviControl != null) {
            getSite().getEditor().removeControl( naviControl );
            naviControl.deactivate();
            naviControl.dispose();
            naviControl = null;
        }
        if (drawControl != null) {
            getSite().getEditor().removeControl( drawControl );
            drawControl.deactivate();
            // FIXME this crashes
//          drawControl.destroy();
            drawControl.dispose();
            drawControl = null;
        }
        if (vectorLayer != null) {
            vectorLayer.dispose();
            vectorLayer = null;
            lastControl = layersList;
        }
    }

    
    @Override
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        //log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            log.info( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 50 ) );
        }
        try {
            modifyGeometries( payload.get( "features" ) );
        }
        catch (Throwable e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, i18n( "errorMsg" ), e );
        }
    }

    
    protected void modifyGeometries( String json ) throws Exception {
        // parse JSON
        MultiPolygon hole = parseJSON( json );
        
        // find features to modify
        PipelineFeatureSource fs = PipelineFeatureSource.forLayer( getSelectedLayer(), false );
        SimpleFeatureType schema = fs.getSchema();
        FeatureCollection<SimpleFeatureType,SimpleFeature> intersected = fs.getFeatures( ff.intersects( ff.property( "" ), ff.literal( hole ) ) );
        log.info( "Intersected: " + intersected.size() );
        
        // modify
        FeatureIterator<SimpleFeature> it = intersected.features();
        try {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                Geometry newGeom = ((Geometry)feature.getDefaultGeometry()).difference( hole );

                // execute operation
                String property = schema.getGeometryDescriptor().getLocalName();
                ModifyFeaturesOperation op = new ModifyFeaturesOperation( getSelectedLayer(),
                        fs, feature.getID(), property, newGeom );

                OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {
                    public void done( IJobChangeEvent ev ) {
                        Polymap.getSessionDisplay().asyncExec( new Runnable() {
                            public void run() {
                                // disable tool
                                getSite().triggerTool( getSite().getToolPath(), false );
                            }
                        });
                    }
                });
            }
        }
        finally {
            it.close();
        }
    }
    
    
    protected MultiPolygon parseJSON( String json ) throws Exception {
        FeatureJSON io = new FeatureJSON();
        SimpleFeature feature = io.readFeature( new StringReader( json ) );
        log.info( "JSON Feature: " + feature );

// FIXME       // transform CRS
//        CoordinateReferenceSystem layerCRS = schema.getCoordinateReferenceSystem();
//        CoordinateReferenceSystem mapCRS = layer.getMap().getCRS();
//        if (!mapCRS.equals( layerCRS )) {
//            //log.debug( "    transforming geom: " + mapCRS.getName() + " -> " + layerCRS.getName() );
//            MathTransform transform = CRS.findMathTransform( mapCRS, layerCRS );
//            geom = JTS.transform( geom, transform );
//        }

        return (MultiPolygon)feature.getDefaultGeometry();
    }

    
    @Override
    public String i18n( String key, Object... args ) {
        return Messages.get( "DifferenceTool_" + key, args );    
    }
    
}
