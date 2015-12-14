/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.preference.StringFieldEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StringFieldEditor2
        extends StringFieldEditor {

    private static Log log = LogFactory.getLog( StringFieldEditor2.class );

    
    public StringFieldEditor2( String name, String labelText, String tooltip, Composite parent ) {
        super( name, labelText, parent );
        getTextControl().setToolTipText( tooltip );
    }
    
    
}
