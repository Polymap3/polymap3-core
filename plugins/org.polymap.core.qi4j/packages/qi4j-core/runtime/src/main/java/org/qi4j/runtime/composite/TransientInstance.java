/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.AbstractCompositeDescriptor;
import org.qi4j.spi.composite.CompositeInstance;

/**
 * InvocationHandler for proxy objects.
 */
public class TransientInstance
    implements CompositeInstance, MixinsInstance
{
    public static TransientInstance getCompositeInstance( Composite composite )
    {
        return (TransientInstance) Proxy.getInvocationHandler( composite );
    }

    private final Composite proxy;
    protected final Object[] mixins;
    protected StateHolder state;
    protected final AbstractCompositeModel compositeModel;
    private final ModuleInstance moduleInstance;

    public TransientInstance( AbstractCompositeModel compositeModel,
                              ModuleInstance moduleInstance,
                              Object[] mixins,
                              StateHolder state
    )
    {
        this.compositeModel = compositeModel;
        this.moduleInstance = moduleInstance;
        this.mixins = mixins;
        this.state = state;

        proxy = compositeModel.newProxy( this );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args, moduleInstance );
    }

    public <T> T proxy()
    {
        return (T) proxy;
    }

    public <T> T newProxy( Class<T> mixinType )
    {
        return compositeModel.newProxy( this, mixinType );
    }

    public Object invokeProxy( Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args, moduleInstance );
    }

    public AbstractCompositeDescriptor descriptor()
    {
        return (AbstractCompositeDescriptor) compositeModel;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return compositeModel.metaInfo( infoType );
    }

    public Class<? extends Composite> type()
    {
        return compositeModel.type();
    }

    public ModuleInstance module()
    {
        return moduleInstance;
    }

    public AbstractCompositeModel compositeModel()
    {
        return compositeModel;
    }

    public StateHolder state()
    {
        return state;
    }

    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        Object mixin = methodInstance.getMixin( mixins );
        return methodInstance.invoke( proxy, params, mixin );
    }

    public Object invokeObject( Object proxy, Object[] args, Method method )
        throws Throwable
    {
        // XXX _p3: methods declared by Object (hashCode(), equals(), toString() are handled here;
        // seems that implementations that are provided by mixins are ignored
        for (Object mixin : mixins) {
            //System.out.println( "Mixin: " + mixin.getClass().getSimpleName() + ", for method: " + method.getName() );
            // check if this mixin provides an implementation
            try {
                Method mixinMethod = mixin.getClass().getDeclaredMethod( method.getName(), method.getParameterTypes() );
                //System.out.println( "    *** found method in mixin: " + mixin.getClass().getSimpleName() );
                
                return mixinMethod.invoke( mixin, args );
            }
            catch (Exception e) {
                // method not found in mixin
            }
        }

        return method.invoke( this, args );
    }
}