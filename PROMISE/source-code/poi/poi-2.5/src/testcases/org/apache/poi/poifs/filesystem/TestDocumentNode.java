
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
        

package org.apache.poi.poifs.filesystem;

import java.io.*;

import junit.framework.*;

import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.RawDataBlock;

/**
 * Class to test DocumentNode functionality
 *
 * @author Marc Johnson
 */

public class TestDocumentNode
    extends TestCase
{

    /**
     * Constructor TestDocumentNode
     *
     * @param name
     */

    public TestDocumentNode(String name)
    {
        super(name);
    }

    /**
     * test constructor
     *
     * @exception IOException
     */

    public void testConstructor()
        throws IOException
    {
        DirectoryProperty    property1 = new DirectoryProperty("directory");
        RawDataBlock[]       rawBlocks = new RawDataBlock[ 4 ];
        ByteArrayInputStream stream    =
            new ByteArrayInputStream(new byte[ 2048 ]);

        for (int j = 0; j < 4; j++)
        {
            rawBlocks[ j ] = new RawDataBlock(stream);
        }
        POIFSDocument    document  = new POIFSDocument("document", rawBlocks,
                                         2000);
        DocumentProperty property2 = document.getDocumentProperty();
        DirectoryNode    parent    = new DirectoryNode(property1, null, null);
        DocumentNode     node      = new DocumentNode(property2, parent);

        // verify we can retrieve the document
        assertEquals(property2.getDocument(), node.getDocument());

        // verify we can get the size
        assertEquals(property2.getSize(), node.getSize());

        // verify isDocumentEntry returns true
        assertTrue(node.isDocumentEntry());

        // verify isDirectoryEntry returns false
        assertTrue(!node.isDirectoryEntry());

        // verify getName behaves correctly
        assertEquals(property2.getName(), node.getName());

        // verify getParent behaves correctly
        assertEquals(parent, node.getParent());
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.filesystem.DocumentNode");
        junit.textui.TestRunner.run(TestDocumentNode.class);
    }
}
