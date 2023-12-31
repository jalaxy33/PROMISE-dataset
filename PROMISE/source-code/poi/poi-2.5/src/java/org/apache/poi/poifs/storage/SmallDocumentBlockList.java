
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

import java.util.*;

/**
 * A list of SmallDocumentBlocks instances, and methods to manage the list
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class SmallDocumentBlockList
    extends BlockListImpl
{

    /**
     * Constructor SmallDocumentBlockList
     *
     * @param blocks a list of SmallDocumentBlock instances
     */

    public SmallDocumentBlockList(final List blocks)
    {
        setBlocks(( SmallDocumentBlock [] ) blocks
            .toArray(new SmallDocumentBlock[ 0 ]));
    }
}   // end public class SmallDocumentBlockList

