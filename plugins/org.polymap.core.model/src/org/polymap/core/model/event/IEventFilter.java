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
package org.polymap.core.model.event;

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.1
 */
public interface IEventFilter<T extends EventObject> {
    
    /**
     * 
     */
    public static IEventFilter ALL = new IEventFilter() {
        public boolean accept( EventObject ev ) {
            return true;
        }
    };
    
    /**
     * Checks if <b>all</b> the given filters accept an event. 
     */
    public class And
            implements IEventFilter {
        
        IEventFilter[]   children;
        
        public And( IEventFilter... children ) {
            assert children != null;
            this.children = children;
        }

        public boolean accept( EventObject ev ) {
            for (IEventFilter filter : children) {
                if (filter.accept( ev ) == false) {
                    return false;
                }
            }
            return true;
        }
        
    }

    
    // interface ******************************************
    
    boolean accept( T ev );

}
