/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class Timer {

    private static Log log = LogFactory.getLog( Timer.class );
    
    private long        start;
    
    public Timer() {
        start();
    }
    
    public Timer start() {
        start = System.currentTimeMillis();
        return this;
    }
    
    public long elapsedTime() {
        return System.currentTimeMillis() - start;
    }
    
    public void print() {
        System.out.println( "Time: " + elapsedTime() + "ms" );
    }
    
}
