/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest.client;

import org.qi4j.spi.query.EntityFinderException;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * JAVADOC
 */
public class SPARQLEntityFinderException
    extends EntityFinderException
{
    private Reference ref;
    private Status status;

    public SPARQLEntityFinderException( Reference reference, Status status )
    {
        super( "Could not perform query to " + reference + ": " + status.getDescription() + "(" + status.getName() + ")" );
        this.ref = reference;
        this.status = status;
    }

    public Status status()
    {
        return status;
    }

    public Reference reference()
    {
        return ref;
    }
}
