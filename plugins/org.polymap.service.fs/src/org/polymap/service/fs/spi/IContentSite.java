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
package org.polymap.service.fs.spi;

import org.eclipse.core.runtime.IPath;

/**
 * Provides the context of an {@link IContentProvider}.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IContentSite {

    public IContentFolder parentFolder( IPath path );
    
    public Object put( String key, Object value );
    
    public Object get( String key );
    
}