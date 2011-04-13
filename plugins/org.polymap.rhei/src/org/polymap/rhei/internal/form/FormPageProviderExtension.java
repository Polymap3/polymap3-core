/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
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
package org.polymap.rhei.internal.form;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.form.IFormPageProvider;

/**
 * Provides access to the extensions of extension point
 * {@link #EXTENSION_POINT_NAME}.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FormPageProviderExtension {

    public static final String          EXTENSION_POINT_NAME = "form.pageProviders";


    public static FormPageProviderExtension[] allExtensions() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( RheiPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        FormPageProviderExtension[] result = new FormPageProviderExtension[ elms.length ];
        for (int i=0; i<elms.length; i++) {
            result[i] = new FormPageProviderExtension( elms[i] );
        }
        return result;
    }
    
    
    public static FormPageProviderExtension forExtensionId( String id ) {
        IConfigurationElement[] elms = Platform.getExtensionRegistry().getConfigurationElementsFor(
                RheiPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        
        List<FormPageProviderExtension> result = new ArrayList( elms.length );
        for (int i=0; i<elms.length; i++) {
            FormPageProviderExtension ext = new FormPageProviderExtension( elms[i] );
            if (ext.getId().equals( id )) {
                result.add( ext );
            }
        }

        if (result.size() > 1) {
            throw new IllegalStateException( "More than 1 extension: " + elms );
        }
        return !result.isEmpty() ? result.get( 0 ) : null;
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       ext;

    
    public FormPageProviderExtension( IConfigurationElement ext ) {
        this.ext = ext;
    }
    
    public String getId() {
        return ext.getAttribute( "id" );
    }

    public String getName() {
        return ext.getAttribute( "name" );
    }
    
    public boolean isStandard() {
        String attr = ext.getAttribute( "isStandard" );
        return attr != null && attr.equalsIgnoreCase( "true" );
    }

    public IFormPageProvider newPageProvider()
    throws CoreException {
        return (IFormPageProvider)ext.createExecutableExtension( "class" );
    }

}
