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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class DefaultContentFolder
        extends DefaultContentNode
        implements IContentFolder {

    private static Log log = LogFactory.getLog( DefaultContentFolder.class );

    protected Date              modified = new Date();
    

    public DefaultContentFolder( String name, IPath parentPath, IContentProvider provider, Object source ) {
        super( name, parentPath, provider, source );
    }


    /**
     * This default implementation always returns the creation date of the object
     * stored in {@link #modified}.
     */
    public Date getModifiedDate() {
        return modified;
    }


    /**
     * This default implementation always returns 60.
     */
    public Long getMaxAgeSeconds() {
        return (long)60;
    }


    public String getDescription( String contentType ) {
        return null;
    }

}
