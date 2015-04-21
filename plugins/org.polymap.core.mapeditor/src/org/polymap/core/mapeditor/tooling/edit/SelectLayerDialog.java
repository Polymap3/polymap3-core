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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IMemento;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SelectLayerDialog
        extends Dialog {

    private static Log log = LogFactory.getLog( SelectLayerDialog.class );
    
    private static final IMessages  i18n = Messages.forPrefix( "SelectLayerDialog" );

    private static final String     PROP_HIDE_DIALOG = "hide_layer_dialog";
    
    public static ILayer maybeOpen( IMemento memento, List<ILayer> layers, ILayer selectedLayer ) {
        SelectLayerDialog dialog = new SelectLayerDialog( PolymapWorkbench.getShellToParentOn(),
                layers, selectedLayer, memento );
        String hideDialog = memento.getString( PROP_HIDE_DIALOG );
        return !"true".equals( hideDialog ) && dialog.open() == Dialog.OK
                ? dialog.getSelectedLayer()
                : selectedLayer;
    }

    
    // instance *******************************************
    
    private List<ILayer>        layers;
    
    private ILayer              selectedLayer;

    private IMemento            memento;

    public SelectLayerDialog( Shell parentShell, List<ILayer> layers, ILayer selectedLayer, IMemento memento ) {
        super( parentShell );
        setShellStyle( SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
        setBlockOnOpen( true );

        this.layers = layers;
        this.selectedLayer = selectedLayer;
        this.memento = memento;
    }

    
    public ILayer getSelectedLayer() {
       return selectedLayer;    
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        // hide Cancel button
        return id != IDialogConstants.CANCEL_ID
                ? super.createButton( parent, id, label, defaultButton )
                : null;
    }
    
    @Override
    protected Control createDialogArea( Composite parent ) {
        getShell().setText( i18n.get( "title" ) );
        getShell().setMinimumSize( 350, 230 );
        getShell().pack();
        
        // client
        Composite client = (Composite) super.createDialogArea( parent );
        client.setLayout( FormLayoutFactory.defaults().margins( 10 ).spacing( 10 ).create() );

        // msg
        Label msg = new Label( client, SWT.WRAP );
        msg.setLayoutData( FormDataFactory.filled().clearBottom().width( 400 ).create() );
        msg.setText( i18n.get( "msg" ) );
        
        // layerCombo
        final CCombo layerCombo = new CCombo( client, SWT.BORDER );
        layerCombo.setLayoutData( FormDataFactory.filled().top( msg ).clearBottom().create() );
        layerCombo.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
        layerCombo.setVisibleItemCount( 12 );
        layerCombo.setEditable( false );
        int i = 0;
        for (ILayer layer : layers) {
            layerCombo.add( layer.getLabel() );
            if (layer == selectedLayer) {
                layerCombo.select( i );
            }
            i++;
        }
        layerCombo.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                selectedLayer = layers.get( layerCombo.getSelectionIndex() );
            }
        });

//        // checkbox
//        final Button hideCheck = new Button( client, SWT.CHECK );
//        hideCheck.setLayoutData( FormDataFactory.filled().top( layerCombo ).left( 0, -5 ).clearBottom().create() );
//        hideCheck.setText( i18n.get( "hide" ) );
//        hideCheck.setSelection( "true".equals( memento.getString( PROP_HIDE_DIALOG ) ) );
//        hideCheck.addSelectionListener( new SelectionAdapter() {
//            @Override
//            public void widgetSelected( SelectionEvent ev ) {
//                memento.putString( PROP_HIDE_DIALOG, hideCheck.getSelection() ? "true" : "false" );
//            }
//        });
        
        // center dialog
        Rectangle shellBounds = getParentShell().getBounds();
        Point dialogSize = getShell().getSize();
        getShell().setLocation(
                shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
                shellBounds.y + (shellBounds.height - dialogSize.y) / 2 );
        
        return client;
    }
    
}
