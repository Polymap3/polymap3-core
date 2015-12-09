/* 
 * polymap.org
 * Copyright 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.catalog.model;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveAdapterFactory;
import net.refractions.udig.catalog.IService;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.model.security.ACL;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class ServiceAdapterFactory
        implements IAdapterFactory, IResolveAdapterFactory {

    private static Log log = LogFactory.getLog( ServiceAdapterFactory.class );

    private static final Class[]    adapterTypes = { ACL.class }; 

    
    @Override
    public Class[] getAdapterList() {
        return adapterTypes;
    }


    @Override
    public Object getAdapter( Object adaptable, Class adapterType ) {
        log.debug( "getAdapter(): type= " + adapterType + ", adaptable= " + adaptable );
        IService service = (IService)adaptable;
        
        CatalogRepository module = CatalogRepository.instance();
        Object result = module.getCatalog().findServiceEntity( service );
        return adapterType.cast( result );
    }


    @Override
    public Object adapt( IResolve resolve, Class<? extends Object> adapter, IProgressMonitor monitor )
            throws IOException {
        return getAdapter( resolve, adapter );
    }


    @Override
    public boolean canAdapt( IResolve resolve, Class<? extends Object> adapter ) {
        return getAdapter( resolve, adapter ) != null;
    }
    
}
