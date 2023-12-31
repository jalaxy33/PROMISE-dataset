
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
        

package org.apache.poi.poifs.property;

import java.util.*;

import java.io.IOException;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.storage.SmallDocumentBlock;

/**
 * Root property
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class RootProperty
    extends DirectoryProperty
{

    /**
     * Default constructor
     */

    RootProperty()
    {
        super("Root Entry");

        // overrides
        setNodeColor(_NODE_BLACK);
        setPropertyType(PropertyConstants.ROOT_TYPE);
        setStartBlock(POIFSConstants.END_OF_CHAIN);
    }

    /**
     * reader constructor
     *
     * @param index index number
     * @param array byte data
     * @param offset offset into byte data
     */

    protected RootProperty(final int index, final byte [] array,
                           final int offset)
    {
        super(index, array, offset);
    }

    /**
     * set size
     *
     * @param size size in terms of small blocks
     */

    public void setSize(int size)
    {
        super.setSize(SmallDocumentBlock.calcSize(size));
    }
}   // end public class RootProperty

