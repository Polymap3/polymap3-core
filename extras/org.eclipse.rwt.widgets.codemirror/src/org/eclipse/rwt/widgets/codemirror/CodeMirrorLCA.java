/*
 * polymap.org Copyright 2011, Falko Br�utigam, and other contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.eclipse.rwt.widgets.codemirror;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.rwt.lifecycle.AbstractWidgetLCA;
import org.eclipse.rwt.lifecycle.ControlLCAUtil;
import org.eclipse.rwt.lifecycle.ILifeCycleAdapter;
import org.eclipse.rwt.lifecycle.IWidgetAdapter;
import org.eclipse.rwt.lifecycle.JSWriter;
import org.eclipse.rwt.lifecycle.WidgetLCAUtil;
import org.eclipse.rwt.lifecycle.WidgetUtil;

/**
 * Widget life cycle adapter of the {@link CodeMirror}.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class CodeMirrorLCA 
        extends AbstractWidgetLCA
        implements ILifeCycleAdapter {

    private static Log log = LogFactory.getLog( CodeMirrorLCA.class );
    
    static final String             PROP_TEXT = "text";
    

    /*
     * Initial creation procedure of the widget
     */
    public void renderInitialization( final Widget widget )
            throws IOException {
        JSWriter writer = JSWriter.getWriterFor( widget );
        
        String id = WidgetUtil.getId( widget );
        writer.newWidget( "org.eclipse.rwt.widgets.CodeMirror", new Object[] { id } );
        writer.set( "appearance", "composite" );
        writer.set( "overflow", "hidden" );
        
        ControlLCAUtil.writeStyleFlags( (CodeMirror)widget );
        writer.call( (CodeMirror)widget, "loadLib",
                new Object[] { ((CodeMirror)widget).getJSLocation() } );
    }


    public void preserveValues( final Widget widget ) {
        // preserve properties that are inherited from Control
        ControlLCAUtil.preserveValues( ( Control )widget );
        
        IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
        adapter.preserve( PROP_TEXT, ((CodeMirror)widget).text );
        
        // only needed for themeing
        WidgetLCAUtil.preserveCustomVariant( widget );
    }


    /*
     * Read the parameters transfered from the client
     */
    public void readData( final Widget widget ) {
        CodeMirror codeMirror = (CodeMirror)widget;
        
//        HttpServletRequest request = ContextProvider.getRequest();
//        log.info( "readData(): " + request.getParameterMap() );

        if (!codeMirror.load_lib_done) {
            String load_lib_done = WidgetLCAUtil.readPropertyValue( codeMirror, "load_lib_done" );
            if (load_lib_done != null) {
                codeMirror.load_lib_done = Boolean.valueOf( load_lib_done ).booleanValue();
            }
        }

        String text = WidgetLCAUtil.readPropertyValue( codeMirror, PROP_TEXT );
        if (text != null) {
            codeMirror.text = text;
        }

//        String event = WidgetLCAUtil.readPropertyValue( map, "event_name" );
//
//        if (event != null) {
//            OpenLayersSessionHandler wp = OpenLayersSessionHandler.getInstance();
//
//            OpenLayersObject src = wp.obj_ref2obj.get( WidgetLCAUtil.readPropertyValue( map,
//                    "event_src_obj" ) );
//
//            HashMap<String, String> payload_map = new HashMap<String, String>();
//
//            Map<String, String> payload = src.events.getPayload( event );
//            if (payload != null) {
//                for (String act : payload.keySet()) {
//                    payload_map.put( act, WidgetLCAUtil.readPropertyValue( map, "event_payload_"
//                            + act ) );
//                }
//            }
//            try {
//                src.events.process_event( event, payload_map );
//            }
//            // catch everything readData() must not throw anything
//            catch (Throwable e) {
//                System.out.println( "Unhandled exception in OpenLayersWidgetLCA.readData(): " + e );
//            }
//        }

    }


    public void renderChanges( final Widget widget )
            throws IOException {
        CodeMirror codeMirror = (CodeMirror)widget;
        ControlLCAUtil.writeChanges( codeMirror );
         
        JSWriter writer = JSWriter.getWriterFor( widget );

        IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
        if (!codeMirror.text.equals( adapter.getPreserved( PROP_TEXT ) )) {
            //log.debug( "CHANGED: " + ((CodeMirror)widget).text );
            writer.set( PROP_TEXT, codeMirror.text );
        }

        // only needed for custom variants (theming)
        WidgetLCAUtil.writeCustomVariant( widget );
    }


    public void renderDispose( final Widget widget )
            throws IOException {
        JSWriter writer = JSWriter.getWriterFor( widget );
        writer.dispose();
    }


    public void createResetHandlerCalls( String typePoolId )
            throws IOException {
    }


    public String getTypePoolId( Widget widget ) {
        return null;
    }

}
