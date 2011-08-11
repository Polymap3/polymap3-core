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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id: $
 */

package org.polymap.core.project.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import org.polymap.core.project.ui.layer.LayerNavigator;
import org.polymap.core.project.ui.project.ProjectView;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>24.06.2009: created</li>
 * @version $Revision: $
 */
public class ProjectPerspective
        implements IPerspectiveFactory {

    private Log log = LogFactory.getLog( ProjectPerspective.class );


    public void createInitialLayout( final IPageLayout layout ) {
        log.debug( "..." );
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible( true );

        IFolderLayout topLeft = layout.createFolder(
                "topLeft", IPageLayout.LEFT, 0.23f, editorArea );
        IFolderLayout bottomLeft = layout.createFolder(
                "bottomLeft", IPageLayout.BOTTOM, 0.25f, "topLeft" );
        IFolderLayout topRight = layout.createFolder(
                "topRight", IPageLayout.RIGHT, 0.72f, editorArea );
        IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder(
                "bottom", IPageLayout.BOTTOM, 0.70f, editorArea );

        topLeft.addView( ProjectView.ID );

        bottomLeft.addView( LayerNavigator.ID );
//        bottomLeft.addView( "org.polymap.core.project.MapLayersView" );
//        bottomLeft.addPlaceholder( "org.polymap.core.project.MapLayersView" );
        bottomLeft.addPlaceholder( "org.polymap.rhei.FilterView:*" );

        bottom.addPlaceholder( "org.polymap.*:*" );
        bottom.addPlaceholder( "org.polymap.*" );
        bottom.addPlaceholder( "org.eclipse.*" );

//        bottomLeft.addView( CodeMirrorView.ID );

//        topRight.addPlaceholder( "net.refractions.udig.catalog.ui.CatalogView" );
        topRight.addView( "net.refractions.udig.catalog.ui.CatalogView" );
        topRight.addPlaceholder( "org.polymap.geocoder.*" );
//      topRight.addPlaceholder( GeoSelectionView.ID );


        bottom.addPlaceholder( "org.polymap.core.data.ui.featureTable.view:*" );
        bottom.addPlaceholder( "org.polymap.*" );
        bottom.addPlaceholder( "org.eclipse.*" );

        // add shortcuts to show view menu
        layout.addShowViewShortcut( "net.refractions.udig.catalog.ui.CatalogView" );
//      layout.addShowViewShortcut( "net.refractions.udig.project.ui.projectExplorer" );

      // add shortcut for other perspective
//      layout.addPerspectiveShortcut( "org.eclipse.rap.demo.perspective.planning" );

    }
}
