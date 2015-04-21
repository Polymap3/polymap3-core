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
package org.polymap.openlayers.rap.widget.handler;

/**
 * The OpenLayers.Handler.Polygone handler. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PolygonHandler
        extends DrawFeatureHandler {

    @Override
    public String jsClassName() {
        return "OpenLayers.Handler.Polygon";
    }
    
}
