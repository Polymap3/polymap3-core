/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.ui.featureTable;

import org.opengis.filter.Filter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class FeatureTableAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private IWorkbenchPart      activePart;
    
    private ILayer              selectedLayer;
    

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
        this.activePart = targetPart;
    }


    public void runWithEvent( IAction action, Event ev ) {
        Display.getCurrent().asyncExec( new Runnable() {
            public void run() {
                try {
                    // ensure that the view is shown
                    FeatureSelectionView view = FeatureSelectionView.open( selectedLayer );

                    if (MessageDialog.openQuestion( PolymapWorkbench.getShellToParentOn(),
                            "Achtung", "Mit dieser Operation werden alle Objekte geladen.\n" + 
                            "Das kann unter Umst�nden einige Zeit dauern. Um eine Auswahl von\n" + 
                            "Objekten zu selektieren, nutzen Sie bitte die Funktion Abfragen/Suche.\n\n" + 
                            "Achtung: mit dieser Operation wird eine eventuell gemachte Auswahl �berschrieben.")) {
                        
                        view.loadTable( Filter.INCLUDE );
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim �ffnen der Attributtabelle.", e );
                }
            }
        });
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            selectedLayer = (elm != null && elm instanceof ILayer)
                    ? (ILayer)elm : null;
        }
        else {
            selectedLayer = null;
        }
    }

}
