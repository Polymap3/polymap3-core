/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2.runtime;

import org.polymap.core.model2.Entity;

/**
 * The API to access the engine from within an {@link Entity}. Holds the
 * {@link EntityStatus status} of the entity.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface EntityRuntimeContext {

    /**
     * The status an Entity can have.
     */
    public enum EntityStatus {
        LOADED( 0 ), 
        CREATED( 1 ), 
        MODIFIED( 2 ),
        REMOVED( 3 );
        
        public int         status;
        
        EntityStatus( int status ) {
            this.status = status;    
        }
        
    }
    
    public Object id();

    public Object state();
    
    public EntityStatus status();
    
    public void raiseStatus( EntityStatus newStatus );
    
    public UnitOfWork unitOfWork();

    public <T> T createMixin( Class<T> mixinClass );

    public void methodProlog( String methodName, Object[] args );
    
}
