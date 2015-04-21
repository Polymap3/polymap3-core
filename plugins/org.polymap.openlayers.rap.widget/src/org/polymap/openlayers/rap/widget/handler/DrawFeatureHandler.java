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

import org.polymap.openlayers.rap.widget.controls.Control;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;

/**
 * Handlers used by {@link DrawFeatureControl}.
 * 
 * @see DrawFeatureControl
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DrawFeatureHandler
        extends Handler {

    protected Control           parent;

    public void init( @SuppressWarnings("hiding") Control parent ) {
        this.parent = parent;
    }

    @Override
    public void deactivate() {
        parent.addObjModCode( parent.getJSObjRef() + ".handler.deactivate();" );
    }

    @Override
    public void activate() {
        parent.addObjModCode( parent.getJSObjRef() + ".handler.activate();" );
    }

    @Override
    public void destroy() {
        parent.addObjModCode( parent.getJSObjRef() + ".handler.destroy();" );
    }
    
}
