/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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

package org.polymap.core.services;

import java.util.ResourceBundle;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import org.eclipse.rwt.RWT;

/**
 * The messages of the <code>org.polymap.core.services</code> plugin.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 * @version $Revision$
 */
public class Messages {

    private static final String BUNDLE_NAME = ServicesPlugin.PLUGIN_ID + ".messages"; //$NON-NLS-1$

    private static final String SEP = "_";


    private Messages() {
        // prevent instantiation
    }


    public static String get( String key, Object... args ) {
        try {
            ClassLoader cl = Messages.class.getClassLoader();
            // getBundle() caches the bundles
            ResourceBundle bundle = ResourceBundle.getBundle( BUNDLE_NAME, RWT.getLocale(), cl );
            
            String result = bundle.getString( key );
            if (args.length > 0) {
                result = MessageFormat.format( result, args );
            }
            return result;
        }
        catch (Exception e) {
            return StringUtils.substringAfterLast( key, "_" );
        }
    }
    

    public static String get( Class cl, String _key ) {
        StringBuffer key = new StringBuffer( 64 );
        key.append( StringUtils.substringAfterLast( cl.getName(), "." ) ) 
                .append( SEP )
                .append( _key );
        return get( key.toString() );
    }


//    public static String getForClass( String keySuffix ) {
//        
//        Exception e = new Exception();
//        e.fillInStackTrace();
//        StackTraceElement[] trace = e.getStackTrace();
//        StackTraceElement elm = trace[trace.length-1];
//        
//        StringBuffer key = new StringBuffer( 64 );
//        key.append( StringUtils.substringAfterLast( elm.getClassName(), "." ) ) 
//                .append( "_" )
//                .append( key );
//        
//        ClassLoader cl = Messages.class.getClassLoader();
//        // getBundle() caches the bundles
//        ResourceBundle bundle =
//                ResourceBundle.getBundle( BUNDLE_NAME, RWT.getLocale(), cl );
//        return bundle.getString( key.toString() );
//    }
    
    
    public static Messages get() {
        Class clazz = Messages.class;
        return (Messages)RWT.NLS.getISO8859_1Encoded( BUNDLE_NAME, clazz );
    }

}
