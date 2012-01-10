/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
package org.polymap.core.mapeditor.edit;

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.type.GeometryDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * Editor action for the {@link EditFeatureSupport}. This actions controls
 * the {@link DrawFeatureControl}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class DrawFeatureEditorAction
        extends AbstractEditEditorAction
        implements IEditorActionDelegate, OpenLayersEventListener {

    private static Log log = LogFactory.getLog( DrawFeatureEditorAction.class );

    
    public DrawFeatureEditorAction() {
        controlType = DrawFeatureControl.class;
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        mapEditor.activateSupport( support, action.isChecked() );
        
        if (action.isChecked()) {
            try {
                DrawFeatureControl control = (DrawFeatureControl)support.getControl( DrawFeatureControl.class );
                if (control == null) {
                    // find geometry type
                    PipelineFeatureSource fs = PipelineFeatureSource.forLayer( support.layer, true );
                    GeometryDescriptor geom = fs.getSchema().getGeometryDescriptor();
                    String geomType = geom.getType().getBinding().getSimpleName();
                    log.debug( "Geometry: " + geomType );

                    String handler = null;
                    if ("MultiLineString".equals( geomType )
                            || "LineString".equals( geomType )) {
                        handler = DrawFeatureControl.HANDLER_LINE;
                    }
                    else if ("MultiPolygon".equals( geomType )
                            || "Polygon".equals( geomType )) {
                        handler = DrawFeatureControl.HANDLER_POLYGON;
                    }
                    else if ("MultiPoint".equals( geomType )
                            || "Point".equals( geomType )) {
                        handler = DrawFeatureControl.HANDLER_POINT;
                    }
                    else {
                        log.warn( "Unhandled geometry type: " + geomType + ". Using polygone handler..." );
                        handler = DrawFeatureControl.HANDLER_POLYGON;
                        throw new Exception( "Dieser Geometrietyp kann nicht bearbeitet werden: " + geom.getType().getName() );
                    }
                    control = new DrawFeatureControl( support.vectorLayer, handler );
                    support.addControl( control );

                    // register event handler
                    Map<String, String> payload = new HashMap<String, String>();
                    payload.put( "features", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
                    support.vectorLayer.events.register( this, 
                            DrawFeatureControl.EVENT_ADDED, payload );
                }
                support.setControlActive( DrawFeatureControl.class, true );

            }
            catch (Exception e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
            }
        }
    }

    
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            log.debug( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 500 ) );
        }
        
        try {
            NewFeatureOperation op = new NewFeatureOperation( support.layer, null, payload.get( "features" ) );
            
            OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {
                public void done( IJobChangeEvent event ) {
                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
                        public void run() {
                            WMSLayer olayer = (WMSLayer)mapEditor.findLayer( support.layer );
                            if (olayer != null) {
                                olayer.redraw( true );
                            }
                        }
                    });
                }
            });

//            // redraw map layer
//            WMSLayer olayer = (WMSLayer)mapEditor.findLayer( support.layer );
//            olayer.redraw( true );
        }
        catch (Throwable e) {
            log.warn( "", e );
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
        }
    }

}
