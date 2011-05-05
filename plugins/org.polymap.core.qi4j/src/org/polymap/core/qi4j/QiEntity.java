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
package org.polymap.core.qi4j;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * Interface and Mixin that all Qi4j entities should extend. The {@link Mixin}
 * provides implementations for the basic {@link Entity} methods.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface QiEntity
        extends Entity {

    public Class<? extends EntityComposite> getCompositeType();

    
    /**
     * 
     */
    public abstract class Mixin
            implements QiEntity {

        @This 
        private EntityComposite         composite;


        public String id() {
            return composite.identity().get();
        }

        public Class<? extends EntityComposite> getCompositeType() {
            return (Class<? extends EntityComposite>)composite.type();
        }

        public EntityType getEntityType() {
            return EntityTypeImpl.forClass( (Class<? extends Entity>)composite.type() );
        }

    }

}
