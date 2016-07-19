/* 
 * polymap.org
 * Copyright (C) 2011-2016, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.providers.shape;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.FileItem;
import org.eclipse.core.runtime.IPath;

import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

import org.polymap.service.fs.Messages;
import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ShapefileContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( ShapefileContentProvider.class );

    
    protected void checkPermission( ACL entity, AclPermission permission )
    throws NotAuthorizedException {
        assert entity != null;
        Principal user = Polymap.instance().getUser();
        if (!ACLUtils.checkPermission( entity, permission, false )) {
            throw new NotAuthorizedException( "Permission denied" );
        }
    }
    
    
    /**
     * 
     */
    public class ShapefileFolder
            extends DefaultContentFolder
            implements IContentWriteable {

        protected ShapefileContainer        container;

        public ShapefileFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( "shapefile", parentPath, provider, layer );
            container = new ShapefileContainer( layer, getSite() );
        }
        
        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält die <b>Shapefile-Daten</b> der Ebene \"" + getLayer().getLabel() + "\".";
        }

        @Override
        public String processForm( Map<String,String> params, Map<String,FileItem> files )
                throws IOException, BadRequestException, NotAuthorizedException {
            FileItem item = getOnlyElement( files.values() );
            log.info( "ZIP: " + item.getContentType() + ": " + item.getSize() );
            //assert item.getContentType().contains( "zip" );
            
            ZipInputStream zip = new ZipInputStream( item.getInputStream() );
            try {
                ZipEntry entry = null;
                while ((entry = zip.getNextEntry()) != null) {
                    String ext = getExtension( entry.getName() );
                    log.info( "     " + entry.getName() + ", ext: " + ext + ", size:" + entry.getSize() );
                    OutputStream out = container.getOutputStream( ext );
                    try { IOUtils.copy( zip, out ); } finally { IOUtils.closeQuietly( out ); }
                }
            }
            catch (Throwable e) {
                log.warn( "", e );
                throw new RuntimeException( e );
            }
            finally {
                IOUtils.closeQuietly( zip );
            }
            return null;
        }

        @Override
        public void replaceContent( InputStream in, Long length )
                throws IOException, BadRequestException, NotAuthorizedException {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    }

    
    /*
     * 
     */
    public class ShapefileFile
            extends DefaultContentNode
            implements IContentFile, IContentWriteable, IContentDeletable {

        private ShapefileContainer  container;
        
        private String              fileSuffix;
        
        
        public ShapefileFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container, String fileSuffix ) {
            super( layer.getLabel() + "." + fileSuffix, parentPath, provider, layer );
            this.container = container;
            this.fileSuffix = fileSuffix;
        }

        public void delete()
        throws BadRequestException, NotAuthorizedException {
            checkPermission( (ILayer)getSource(), AclPermission.WRITE );
            
            // QGIS deletes files before writing, so support delete but do nothing.
            log.debug( "delete: " + fileSuffix + " (skipping)" );
        }

        @Override
        public Long getContentLength() {
            if (container.exception != null) {
                log.warn( "", container.exception );
                return null;
            }
            else {
                return container.getFileSize( fileSuffix );                
            }
        }

        @Override
        public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
        throws IOException, BadRequestException {
            log.debug( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            
            if (container.exception != null) {
                log.warn( "", container.exception );
            }
            else {
                InputStream in = container.getInputStream( fileSuffix );                
                try {
                    IOUtils.copy( in, out );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
        }

        @Override
        public void replaceContent( InputStream in, Long length )
        throws IOException, BadRequestException, NotAuthorizedException {
            log.debug( "replace: " + fileSuffix + " : " + length );
            checkPermission( (ILayer)getSource(), AclPermission.WRITE );
            
            OutputStream out = container.getOutputStream( fileSuffix );
            try {
                IOUtils.copy( in, out );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
        }

        @Override
        public String processForm( Map<String, String> params, Map<String, FileItem> files )
        throws IOException, BadRequestException, NotAuthorizedException {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public String getContentType( String accepts ) {
            return "application/octec-stream";
        }

        @Override
        public Long getMaxAgeSeconds() {
            return (long)60;
        }

        @Override
        public Date getModifiedDate() {
            return container.lastModified;
        }
    }


    /*
     * 
     */
    public static class SnapshotFile
            extends DefaultContentNode
            implements IContentFile, IContentDeletable {

        private static final FastDateFormat df = FastDateFormat.getInstance( "yyyy-MM-dd@HH-mm-ss" );
        
        private ShapefileContainer          container;
        
        
        public SnapshotFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container, IContentSite site ) {
            super( "snapshot.txt", parentPath, provider, layer );
            this.container = container;
        }


        public void delete()
        throws BadRequestException {
            container.flush();   
        }


        public byte[] content() {
            try {
                String modified = df.format( container.lastModified );
                return Messages.get( getSite().getLocale(), "SnapshotFile_content", 
                        ((ILayer)getSource()).getLabel(), modified ).getBytes( "UTF-8" );
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException( e );
            }
        }


        public Long getContentLength() {
            return (long)content().length;
        }


        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
        throws IOException, BadRequestException {
            out.write( content() );
        }


        public String getContentType( String accepts ) {
            return "text/txt";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return container.lastModified;
        }
    }
    

    /*
     * 
     */
    public class ShapeZipFile
            extends DefaultContentNode
            implements IContentFile {

        private ShapefileContainer      container;
        
        private byte[]                  content;
        
        private Date                    modified = new Date();
        
        
        public ShapeZipFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container ) {
            super( layer.getLabel() + ".shp.zip", parentPath, provider, layer );
            this.container = container;
        }

        @Override
        public int getSizeInMemory() {
            try {
                // initializing the zip just to calculate the cache size should no problem
                // here since probable getContentLength() is called by the client side anyway
                return getContentLength().intValue() + super.getSizeInMemory();
            }
            catch (Exception e) {
                return super.getSizeInMemory();
            }
        }

        @Override
        public String getContentType( String accepts ) {
            return "application/zip";
        }

        @Override
        public Long getMaxAgeSeconds() {
            return (long)60;
        }

        @Override
        public Date getModifiedDate() {
            return modified;
        }

        @Override
        public Long getContentLength() {
            try {
                return (long)checkInitContent().length;
            }
            catch (Exception e) {
                throw new RuntimeException( "", e );
            }
        }

        @Override
        public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
        throws IOException, BadRequestException {
            log.debug( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            try {
                checkInitContent();
                IOUtils.copy( new ByteArrayInputStream( content ), out );
            }
            catch (Exception e) {
                throw new RuntimeException( "", e );
            }
        }

        protected synchronized byte[] checkInitContent() 
        throws Exception {
            if (container.exception != null) {
                log.warn( "Last exception from container: ", container.exception );
                throw container.exception;
            }

            if (content == null || container.lastModified.after( modified )) {
                // generate shapefile
                container.getFileSize( "shp" );

                if (container.exception != null) {
                    log.warn( "Last exception from container: ", container.exception );
                    throw container.exception;
                }

                InputStream in = null;
                try {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream( 1024 * 1024 );
                    ZipOutputStream zipOut = new ZipOutputStream( bout );

                    String basename = container.layer.getLabel();

                    for (String fileSuffix : ShapefileGenerator.FILE_SUFFIXES) {
                        zipOut.putNextEntry( new ZipEntry( basename + "." + fileSuffix ) );
                        in = container.getInputStream( fileSuffix );
                        IOUtils.copy( in, zipOut );
                        in.close();
                    }
                    zipOut.close();
                    
                    content = bout.toByteArray();
                    modified = new Date();
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
            return content;
        }
    }

}
