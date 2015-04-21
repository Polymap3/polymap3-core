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

import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
import org.polymap.openlayers.rap.widget.util.Stringer;

/**
 * The OpenLayers.Handler.RegularPolygon handler.
 * <p/>
 * Used and instantiated by a {@link DrawFeatureControl} for example.
 *
 * @see <a href="http://dev.openlayers.org/releases/OpenLayers-2.13.1/doc/apidocs/files/OpenLayers/Handler/RegularPolygone-js.html">OpenLayers Doc</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RegularPolygonHandler
        extends DrawFeatureHandler {

    private int         sides;
    
    private float       radius;
    
    private float       snapAngle;
    
    
    @Override
    public String jsClassName() {
        return "OpenLayers.Handler.RegularPolygon";
    }

    public int getSides() {
        return sides;
    }
    
    public RegularPolygonHandler setSides( int sides ) {
        this.sides = sides;
        setOptions( "sides", sides );
        return this;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public RegularPolygonHandler setRadius( float radius ) {
        this.radius = radius;
        setOptions( "radius", radius >= 0 ? radius : "null" );
        return this;
    }
    
    public float getSnapAngle() {
        return snapAngle;
    }

    public RegularPolygonHandler setSnapAngle( float snapAngle ) {
        this.snapAngle = snapAngle;
        setOptions( "snapAngle", snapAngle >= 0 ? snapAngle : "null" );
        return this;
    }

    protected void setOptions( String name, Object value ) {
        parent.addObjModCode( new Stringer( parent.getJSObjRef(), 
                ".handler.setOptions({", name, ":", value.toString(), "});" ).toString() );
    }
    
}
