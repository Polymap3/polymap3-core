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
package org.polymap.core.data.pipeline;

import java.util.Properties;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.refractions.udig.catalog.IService;

import org.polymap.core.project.LayerUseCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class ProcessorDescription {

    private Class<? extends PipelineProcessor> cl;

    private ProcessorSignature      signature;
    
    private Properties              props;
    
    private LayerUseCase            usecase;


    /**
     * 
     * @param cl
     * @param props The properties that are given to the
     *        {@link PipelineProcessor#init(Properties)} method.
     * @param usecase 
     */
    public ProcessorDescription( Class<? extends PipelineProcessor> cl, 
            Properties props, LayerUseCase usecase ) {
        this.cl = cl;
        this.props = props;
        this.usecase = usecase;
    }


    public ProcessorDescription( ProcessorSignature signature ) {
        this.signature = signature;
    }


    public String toString() {
        return getClass().getSimpleName() + "[" 
                + (cl != null ? cl.getSimpleName() : "null")
                + "]";
    }


    public ProcessorSignature getSignature() {
        if (signature == null) {
            try {
                Method m = cl.getMethod( "signature", LayerUseCase.class );
                if (Modifier.isStatic( m.getModifiers() )) {
                    signature = (ProcessorSignature)m.invoke( cl, usecase );
                }
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return signature;
    }

    
    public Properties getProps() {
        return props;
    }


    public PipelineProcessor newProcessor() {
        assert cl != null : "This ProcessorDescription was initialized without a processor class - it can only be used as the start of a chain.";
        try {
            PipelineProcessor result = cl.newInstance();
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    /**
     * In case this processor is an {@link ITerminalPipelineProcessor}, check
     * if it can handle the given service.
     * 
     * @param service
     * @throws IllegalArgumentException If this processor is not a terminal.
     */
    public boolean isCompatible( IService service ) {
        assert cl != null : "This ProcessorDescription was initialized without a processor class - it can only be used as the start of a chain.";
        if (! ITerminalPipelineProcessor.class.isAssignableFrom( cl )) {
            throw new IllegalArgumentException( "Processor is not a terminal: " + cl.getName() );
        }
        try {
            Method m = cl.getMethod( "isCompatible", IService.class );
            if (Modifier.isStatic( m.getModifiers() )) {
                return (Boolean)m.invoke( cl, service );
            }
            else {
                throw new IllegalStateException( "Method isCompatible() must be static." );
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

}
