
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
        

package org.apache.poi.util;

import junit.framework.*;

import java.io.*;

/**
 * Title:        Unit test for ByteField class
 * Description:  Unit test for ByteField class
 * @author       Marc Johnson (mjohnson at apache dot org)
 */

public class TestByteField
    extends TestCase
{

    /**
     * Constructor
     *
     * @param name
     */

    public TestByteField(String name)
    {
        super(name);
    }

    static private final byte[] _test_array =
    {
        Byte.MIN_VALUE, ( byte ) -1, ( byte ) 0, ( byte ) 1, Byte.MAX_VALUE
    };

    /**
     * Test constructors.
     */

    public void testConstructors()
    {
        try
        {
            new ByteField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        ByteField field = new ByteField(2);

        assertEquals(( byte ) 0, field.get());
        try
        {
            new ByteField(-1, ( byte ) 1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ByteField(2, ( byte ) 3);
        assertEquals(( byte ) 3, field.get());
        byte[] array = new byte[ 3 ];

        try
        {
            new ByteField(-1, ( byte ) 1, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ByteField(2, ( byte ) 4, array);
        assertEquals(( byte ) 4, field.get());
        assertEquals(( byte ) 4, array[ 2 ]);
        array = new byte[ 2 ];
        try
        {
            new ByteField(2, ( byte ) 5, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 1 ];
            new ByteField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new ByteField(0, array).get());
        }
    }

    /**
     * Test set() methods
     */

    public void testSet()
    {
        ByteField field = new ByteField(0);
        byte[]    array = new byte[ 1 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new ByteField(0);
            field.set(_test_array[ j ], array);
            assertEquals("testing _2 ", _test_array[ j ], field.get());
            assertEquals("testing _3 ", _test_array[ j ], array[ 0 ]);
        }
    }

    /**
     * Test readFromBytes
     */

    public void testReadFromBytes()
    {
        ByteField field = new ByteField(1);
        byte[]    array = new byte[ 1 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ByteField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = _test_array[ j ];
            field.readFromBytes(array);
            assertEquals("testing " + j, _test_array[ j ], field.get());
        }
    }

    /**
     * Test readFromStream
     *
     * @exception IOException
     */

    public void testReadFromStream()
        throws IOException
    {
        ByteField field  = new ByteField(0);
        byte[]    buffer = new byte[ _test_array.length ];

        System.arraycopy(_test_array, 0, buffer, 0, buffer.length);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length; j++)
        {
            field.readFromStream(stream);
            assertEquals("Testing " + j, _test_array[ j ], field.get());
        }
    }

    /**
     * test writeToBytes
     */

    public void testWriteToBytes()
    {
        ByteField field = new ByteField(0);
        byte[]    array = new byte[ 1 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            assertEquals("testing ", _test_array[ j ], array[ 0 ]);
        }
    }

    /**
     * Main
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing util.ByteField functionality");
        junit.textui.TestRunner.run(TestByteField.class);
    }
}
