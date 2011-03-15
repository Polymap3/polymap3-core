/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.services.geoserver.spring;

import java.io.IOException;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.util.ProgressListener;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MyDataStoreInfoImpl
        extends DataStoreInfoImpl
        implements DataStoreInfo {

    DataStore ds;
    
    protected MyDataStoreInfoImpl( Catalog catalog, DataStore ds ) {
        super( catalog );
        this.ds = ds;
    }

    protected MyDataStoreInfoImpl( Catalog catalog ) {
        super( catalog );
    }

    public DataAccess<? extends FeatureType, ? extends Feature> getDataStore(
            ProgressListener listener )
            throws IOException {
        return ds;
    }

}
