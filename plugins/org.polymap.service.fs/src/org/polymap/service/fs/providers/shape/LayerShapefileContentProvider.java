/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.service.fs.providers.shape;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;

import org.polymap.service.fs.providers.ProjectContentProvider;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;

/**
 * Provides a shapefile folder for every parent folder that exposes an {@link ILayer}
 * as source.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerShapefileContentProvider
        extends ShapefileContentProvider {

    private static Log log = LogFactory.getLog( LayerShapefileContentProvider.class );
    
    public List<? extends IContentNode> getChildren( IPath path ) {
        IContentFolder parent = getSite().getFolder( path );
        
        // files
        if (parent instanceof ShapefileFolder) {
            List<IContentNode> result = new ArrayList();
            
            // shapefile
            ILayer layer = ((ShapefileFolder)parent).getLayer();
            ShapefileContainer container = ((ShapefileFolder)parent).container;
            for (String fileSuffix : ShapefileGenerator.FILE_SUFFIXES) {
                result.add( new ShapefileFile( path, this, (ILayer)parent.getSource(),
                        container, fileSuffix ) );
            }
            // snapshot.txt
            result.add( new SnapshotFile( path, this, (ILayer)parent.getSource(), container, getSite() ) );
            // shape-zip
            result.add( new ShapeZipFile( path, this, (ILayer)parent.getSource(), container ) );
            return result;
        }

        // folder
        else if (parent instanceof ProjectContentProvider.LayerFolder) {
            ILayer layer = (ILayer)parent.getSource();
            try {
                // try to build a FeatureSource for it
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, true );
                if (fs != null) {
                    return singletonList( new ShapefileFolder( path, this, layer ) );
                }
            }
            catch (Exception e) {
                log.warn( "Layer has no feature source: " + layer.getLabel(), e );
            }
        }
        return null;
    }

}
