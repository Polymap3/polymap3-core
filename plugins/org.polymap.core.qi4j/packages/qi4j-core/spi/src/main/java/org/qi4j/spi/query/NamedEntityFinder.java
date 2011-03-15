/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.query;

import java.util.Map;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.query.grammar.OrderBy;

public interface NamedEntityFinder
{
    Iterable<EntityReference> findEntities( NamedQueryDescriptor descriptor,
                                            String resultType,
                                            @Optional Map<String, Object> variables,
                                            @Optional OrderBy[] orderBySegments,
                                            @Optional Integer firstResult,
                                            @Optional Integer maxResults
    )
        throws EntityFinderException;

    EntityReference findEntity( NamedQueryDescriptor descriptor, String resultType, Map<String, Object> variables )
        throws EntityFinderException;

    long countEntities( NamedQueryDescriptor descriptor, String resultType, @Optional Map<String, Object> variables )
        throws EntityFinderException;

    String showQuery( NamedQueryDescriptor descriptor );
}
