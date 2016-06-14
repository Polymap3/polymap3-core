/* 
 * polymap.org
 * Copyright 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.catalog.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.internal.dialogs.PropertyDialog;

import org.polymap.core.catalog.model.CatalogImpl;
import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.model.security.ACL;
import org.polymap.core.workbench.PolymapWorkbench;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class PropertyDialogAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( PropertyDialogAction.class );
    
    private IWorkbenchPart          part;
    
    private IStructuredSelection    selection;


    @Override
    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
        this.part = targetPart;
    }


    /**
     * Create the dialog for the receiver. If no pages are found, an informative
     * message dialog is presented instead.
     * 
     * @return <code>null</code> if no applicable pages are found.
     */
    protected PropertyDialog openDialog() {
        Object elm = selection.getFirstElement();
        if (elm == null) {
            log.warn( "No selection!" );
            return null;
        }

        // XXX I don't seem to be able to figure out how to adapt IService to
        // ACL without this hack
        ACL acl = null;
        if (elm instanceof CatalogImpl) {
            acl = (ACL)((CatalogImpl)elm).getAdapter( ACL.class );
        }
        else if (elm instanceof IService) {
//          acl = (ACL)((IService)elm).getAdapter( ACL.class );

            CatalogRepository module = CatalogRepository.instance();
            acl = (ACL)module.getCatalog().findServiceEntity( (IService)elm );
        }
        else {
            throw new IllegalStateException( "Unknow element type: " + elm.getClass() );
        }
        
        if (acl == null) {
            log.warn( "Selection has no ACL: " + elm.getClass().getSimpleName() );
            PolymapWorkbench.handleError( CatalogPlugin.ID, this, "Dialog kann nicht geöffnet werden.",
                    new Exception( "Der Eintrag besitzt keine Zugriffsrechte: " + elm.getClass().getSimpleName() ) );
            return null;
        }
        else {
            Shell shell = part.getSite().getShell();
            PropertyDialog dialog = PropertyDialog.createDialogOn( shell, null, acl );
            if (dialog != null) {
                dialog.open();
            }
            else {
                PolymapWorkbench.handleError( CatalogPlugin.ID, this, "Dialog kann nicht geöffnet werden.", 
                        new Exception( "Keine Informationen zu: " + acl.getClass().getSimpleName() ) );
            }
            return dialog;
        }
    }
    

    @Override
    public void runWithEvent( IAction action, Event ev ) {
        openDialog();
        
//        Display.getCurrent().asyncExec( new Runnable() {
//
//            public void run() {
//                try {
//                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//                            .getActivePage();
//                    page.showView( IPageLayout.ID_PROP_SHEET );
//                }
//                catch (PartInitException e) {
//                    throw new RuntimeException( e.getMessage(), e );
//                }
//            }
//        } );
    }


    @Override
    public void selectionChanged( IAction action, ISelection s ) {
        if (s instanceof IStructuredSelection) {
            selection = (IStructuredSelection)s;
            action.setEnabled( selection.size() == 1 && selection.getFirstElement() != null);
        }
    }

}