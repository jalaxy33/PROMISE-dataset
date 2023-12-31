
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.poifs.storage;

import org.apache.poi.poifs.property.Property;

class LocalProperty
    extends Property
{

    /**
     * Constructor TestProperty
     *
     * @param name name of the property
     */

    LocalProperty(String name)
    {
        super();
        setName(name);
    }

    /**
     * do nothing
     */

    protected void preWrite()
    {
    }

    /**
     * @return false
     */

    public boolean isDirectory()
    {
        return false;
    }
}   // end package scope class LocalProperty

