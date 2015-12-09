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
package org.polymap.core.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dummy für {@link SpnegoFilter}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SpnegoServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( SpnegoServlet.class );

    
    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        out.write( "<h3>Vielen Dank. Der Request war erfolgreich.</h3>" );
        out.flush();
        out.close();
    }

}
