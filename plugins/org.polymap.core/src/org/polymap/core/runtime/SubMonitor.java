/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Extends {@link SubProgressMonitor} as follows:
 * <ul>
 * <li></li>
 * </ul> 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class SubMonitor
        extends SubProgressMonitor {

    private static final Log log = LogFactory.getLog( SubMonitor.class );

    private String      mainTaskName;
    
    private Timer       timer;

    private int         totalWork;
    
    private int         pendingWork;
    
    
    public SubMonitor( IProgressMonitor monitor, int ticks ) {
        super( monitor, ticks );
    }


    @SuppressWarnings("hiding")
    public void beginTask( String name, int totalWork ) {
        super.beginTask( name, totalWork );
        this.mainTaskName = name;
        this.totalWork = totalWork;
        
        super.subTask( mainTaskName );
        timer = new Timer();
    }


    public void subTask( String name ) {
        super.subTask( mainTaskName + " - " + name );
    }


    @Override
    public void worked( int work ) {
        if (timer.elapsedTime() > 1000) {
            super.worked( pendingWork + work );
            pendingWork = 0;
            timer.start();
        }
        else {
            pendingWork += work;
        }
    }
    
}
