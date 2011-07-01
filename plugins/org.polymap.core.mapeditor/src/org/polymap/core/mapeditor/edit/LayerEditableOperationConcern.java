/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.mapeditor.IEditFeatureSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorInput;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.layer.LayerEditableOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Hooks up the {@link LayerSelectableOperation} and adds
 * {@link IEditFeatureSupport} to the {@link MapEditor} of the layer.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.1
 */
public class LayerEditableOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( LayerEditableOperationConcern.class );


    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof LayerEditableOperation) {
            final LayerEditableOperation lsop = (LayerEditableOperation)op;

            /*
             * 
             */
            return new OperationConcernAdapter() {
                
                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                throws ExecutionException {
                    IStatus result = info.next().execute( monitor, _info );
                    
                    Display display = (Display)_info.getAdapter( Display.class );
                    display.asyncExec( new Runnable() {
                        public void run() {
                            try {
                                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                                
                                for (ILayer layer : lsop.getLayers()) {                                
                                    // FIXME search for the associated editor vie EditorInput
                                    MapEditor mapEditor = (MapEditor)page.getActiveEditor();
                                    if (!mapEditor.getEditorInput().equals( new MapEditorInput( layer.getMap() ) )) {
                                        PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "Active editor is not associated with this map.", new Exception() );
                                    }
                                    
                                    if (layer.isEditable()) {
                                        EditFeatureSupport support = (EditFeatureSupport)mapEditor.findSupport( IEditFeatureSupport.class );
                                        assert support == null;
                                        support = new EditFeatureSupport( mapEditor, layer );
                                        mapEditor.addSupport( support );
                                        mapEditor.activateSupport( support, true );
                                    }
                                    else {
                                        EditFeatureSupport support = (EditFeatureSupport)mapEditor.findSupport( IEditFeatureSupport.class );
                                        if (support != null) {
                                            mapEditor.removeSupport( support );
                                            support.dispose();
                                        }
                                    }
                                }
                            }
                            catch (Exception e) {
                                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                            }
                        }
                    });
                    return result;
                }

                public IStatus redo( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {
                    log.info( "Operation : " + op.getClass().getName() );
                    return info.next().redo( monitor, info );
                }

                public IStatus undo( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {
                    log.info( "Operation : " + op.getClass().getName() );
                    return info.next().undo( monitor, info );
                }

                protected OperationInfo getInfo() {
                    return info;
                }
                
            };
        }
        return null;
    }
    
}
