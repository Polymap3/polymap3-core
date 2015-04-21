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
 * Base class to construct a higher-level handler for event sequences. All handlers
 * have activate and deactivate methods. In addition, they have methods named like
 * browser events. When a handler is activated, any additional methods named like a
 * browser event is registered as a listener for the corresponding event. When a
 * handler is deactivated, those same methods are unregistered as event listeners.
 * <p/>
 * Handlers also typically have a callbacks object with keys named like the
 * abstracted events or event sequences that they are in charge of handling. The
 * controls that wrap handlers define the methods that correspond to these abstract
 * events - so instead of listening for individual browser events, they only listen
 * for the abstract events defined by the handler.
 * <p/>
 * Handlers are created by controls, which ultimately have the responsibility of
 * making changes to the the state of the application. Handlers themselves may make
 * temporary changes, but in general are expected to return the application in the
 * same state that they found it.
 * 
 * @see <a href="http://dev.openlayers.org/releases/OpenLayers-2.13.1/doc/apidocs/files/OpenLayers/Handler-js.html">OpenLayers Doc</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Handler {

    public abstract String jsClassName();

    public abstract void activate();

    public abstract void deactivate();

    public abstract void destroy();
    
}
