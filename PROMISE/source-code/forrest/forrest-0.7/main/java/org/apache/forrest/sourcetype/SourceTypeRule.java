/*
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.forrest.sourcetype;

import org.apache.avalon.framework.configuration.Configurable;

/**
 * Interface to be implemented by all soucetype-defining rules.
 */
public interface SourceTypeRule extends Configurable
{
    /**
     * Returns true if this rule is satisfied by the given SourceInfo.
     */
    public boolean matches(SourceInfo sourceInfo);
}
