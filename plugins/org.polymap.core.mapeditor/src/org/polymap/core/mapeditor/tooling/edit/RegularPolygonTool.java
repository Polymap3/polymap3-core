/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor.tooling.edit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import org.eclipse.ui.IMemento;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;
import org.polymap.core.mapeditor.tooling.EditorTools;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.mapeditor.tooling.ToolingEvent;
import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.mapeditor.tooling.ToolingListener;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;

import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
import org.polymap.openlayers.rap.widget.handler.PolygonHandler;
import org.polymap.openlayers.rap.widget.handler.RegularPolygonHandler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RegularPolygonTool
        extends DefaultEditorTool
        implements PropertyChangeListener {

    private static Log log = LogFactory.getLog( RegularPolygonTool.class );
    
    private static final IMessages  i18n = Messages.forPrefix( "RegularPolygonTool" );
    
    public static final NumberFormat nf = NumberFormat.getNumberInstance( Polymap.getSessionLocale() );

    private static final String     PROP_ACTIVE = "active";
    private static final String     PROP_SIDES = "sides";
    private static final String     PROP_RADIUS = "radius";
    private static final String     PROP_SNAPANGLE = "snapangle";
    
    private DigitizeTool            parentTool;

    private RegularPolygonHandler   handler;
    

    @Override
    public void init( IEditorToolSite site ) {
        super.init( site );

        // disable deactivation for other tools
        getSite().removeListener( this );
        
        parentTool = (DigitizeTool)Iterables.getFirst( 
                site.filterTools( EditorTools.isEqual( getToolPath().removeLastSegments( 1 ) ) ), null );
        assert parentTool != null;

        // the handler also initializes tool state
        parentTool.addListener( RegularPolygonTool.this );

        // listen to state changes of parentTool
        site.addListener( new ToolingListener() {
            public void toolingChanged( ToolingEvent ev ) {
                if (ev.getSource().equals( parentTool )) {
                    if (ev.getType() == EventType.TOOL_ACTIVATED) {
                    }
                    else if (ev.getType() == EventType.TOOL_DEACTIVATING) {
                        getSite().getMemento().putBoolean( PROP_ACTIVE, isActive() );
                    }
                }
            }
        });
    }

    
    @Override
    public void dispose() {
        parentTool.removeListener( this );
        super.dispose();
    }


    /**
     * Called when selected layer of parent tool has changed (during init or
     * by the user).
     */
    @Override
    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getPropertyName().equals( BaseLayerEditorTool.PROP_LAYER_ACTIVATED )) {
            // delay triggerTool() until UI has been created
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    boolean activate = false;
                    boolean enable = false;
                    // Polygone?
                    if (parentTool.getDrawControl().getHandler() instanceof PolygonHandler) {
                        enable = true;
                        activate = Objects.firstNonNull( 
                                getSite().getMemento().getBoolean( PROP_ACTIVE ),
                                Boolean.FALSE ).booleanValue();
                    }
                    
                    // reset tool if layer of parentTool has changed
                    if (isActive() && activate) {
                        getSite().triggerTool( getSite().getToolPath(), false );
                    }

                    getSite().enableTool( getSite().getToolPath(), enable );
                    getSite().triggerTool( getSite().getToolPath(), activate );
                }
            });
        }
    }


    @Override
    public void onActivate() {
        super.onActivate();
        
        DrawFeatureControl drawControl = parentTool.getDrawControl();
        handler = new RegularPolygonHandler();
        drawControl.changeHandler( handler );
    }


    @Override
    public void onDeactivate() {
        super.onDeactivate();
        
        DrawFeatureControl drawControl = parentTool.getDrawControl();
        if (drawControl != null) {
            drawControl.changeHandler( new PolygonHandler() );
        }
    }


    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );

        final IMemento memento = getSite().getMemento();
        
        // sides
        String[] sides = {
                i18n.get( "triangle" ),
                i18n.get( "rectangle" ),
                i18n.get( "circle" ),
                i18n.get( "circle-fine" )
        };
        final CCombo sidesCombo = getSite().getToolkit().createCombo( parent, sides );
        sidesCombo.setEditable( false );
        sidesCombo.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                switch (sidesCombo.getSelectionIndex()) {
                    case 0: handler.setSides( 3 ); break;
                    case 1: handler.setSides( 4 ); break;
                    case 2: handler.setSides( 12 ); break;
                    case 3: handler.setSides( 24 ); break;
                }
                memento.putInteger( PROP_SIDES, sidesCombo.getSelectionIndex() );
            }
        });
        sidesCombo.select( Objects.firstNonNull( memento.getInteger( PROP_SIDES ), 1 ) );
        sidesCombo.notifyListeners( SWT.Selection, new Event() );
        layoutControl( i18n.get( "sides" ), sidesCombo );
        
        // radius
        final CCombo radiusCombo = getSite().getToolkit().createCombo( parent, new String[] {i18n.get( "variable")} );
        radiusCombo.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                try {
                    float radius = nf.parse( radiusCombo.getText() ).floatValue();
                    handler.setRadius( radius );
                    memento.putFloat( PROP_RADIUS, radius );
                }
                catch (ParseException e) {
                    //PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, RegularPolygonTool.this, "", e );
                }
            }
        });
        radiusCombo.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                handler.setRadius( -1 );
                memento.putFloat( PROP_RADIUS, -1 );
            }
        });
        Float savedRadius = memento.getFloat( PROP_RADIUS );
        log.info( "savedRadius: " + savedRadius );
        if (Objects.firstNonNull( savedRadius, -1f ).equals( -1f )) {
            radiusCombo.select( 0 );
            radiusCombo.notifyListeners( SWT.Selection, new Event() );
        }
        else {
            radiusCombo.setText( nf.format( savedRadius ) );
        }
        layoutControl( i18n.get( "radius" ), radiusCombo );
        
        // snap
        String[] snaps = { "--", "15°", "45°", "90°" };
        final CCombo snapCombo = getSite().getToolkit().createCombo( parent, snaps );
        snapCombo.setEditable( false );
        snapCombo.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                switch (snapCombo.getSelectionIndex()) {
                    case 0: handler.setSnapAngle( -1 ); break;
                    case 1: handler.setSnapAngle( 15 ); break;
                    case 2: handler.setSnapAngle( 45 ); break;
                    case 3: handler.setSnapAngle( 90 ); break;
                }
                memento.putInteger( PROP_SNAPANGLE, snapCombo.getSelectionIndex() );
            }
        });
        snapCombo.select( Objects.firstNonNull( memento.getInteger( PROP_SNAPANGLE ), 0 ) );
        snapCombo.notifyListeners( SWT.Selection, new Event() );
        layoutControl( i18n.get( "snap" ), snapCombo );
    }
    
}