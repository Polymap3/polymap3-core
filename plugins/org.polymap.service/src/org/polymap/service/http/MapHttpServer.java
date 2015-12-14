/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.service.http;

import javax.servlet.http.HttpServlet;

import org.polymap.core.CorePlugin;
import org.polymap.core.project.IMap;

import org.polymap.service.IProvidedService;

/**
 * Base class for {@link IMap} based services, such as WMS or WFS.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class MapHttpServer
        extends HttpServlet {

    protected IMap              map;
    
    protected IProvidedService  service;
    
    
    public MapHttpServer() {
    }

    protected void init( IMap _map, IProvidedService _service ) {
        assert _map != null;
        this.map = _map;
        this.service = _service;
    }

    public String getPathSpec() {
        return CorePlugin.servletAlias( this );
    }
    
    public IMap getMap() {
        return map;
    }

    public IProvidedService getService() {
        return service;
    }

}
