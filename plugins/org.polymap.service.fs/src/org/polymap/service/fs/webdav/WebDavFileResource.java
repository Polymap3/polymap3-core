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
package org.polymap.service.fs.webdav;

import java.util.Map;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.spi.IContentFile;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class WebDavFileResource
        extends AbstractResource
        implements GetableResource {

    private static Log log = LogFactory.getLog( WebDavFileResource.class );
    

    public WebDavFileResource( ContentManager contentManager, IContentFile node,
            SecurityManager securityManager ) {
        super( contentManager, node, securityManager );
    }

    
    private IContentFile delegate() {
        return (IContentFile)node;
    }

    
    public Long getContentLength() {
        return delegate().getContentLength();
    }


    public String getContentType( String accepts ) {
        return delegate().getContentType( accepts );
    }


    public Long getMaxAgeSeconds( Auth auth ) {
        return delegate().getMaxAgeSeconds();
    }


    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException {
        try {
            org.polymap.service.fs.spi.Range fsRange = new org.polymap.service.fs.spi.Range( range.getStart(), range.getFinish() );
            delegate().sendContent( out, fsRange, params, contentType );
        }
        catch (IOException e) {
            throw e;
        }
        catch (org.polymap.service.fs.spi.BadRequestException e) {
            throw new BadRequestException( this, e.getMessage() );
        }
    }

}
