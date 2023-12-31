
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
        


package org.apache.poi.hssf.record;


import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the BarRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestBarRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x00,(byte)0x00,   // bar space
        (byte)0x96,(byte)0x00,   // category space
        (byte)0x00,(byte)0x00    // format flags
    };

    public TestBarRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        BarRecord record = new BarRecord((short)0x1017, (short)data.length, data);
        assertEquals( 0, record.getBarSpace());
        assertEquals( 0x96, record.getCategorySpace());
        assertEquals( 0, record.getFormatFlags());
        assertEquals( false, record.isHorizontal() );
        assertEquals( false, record.isStacked() );
        assertEquals( false, record.isDisplayAsPercentage() );
        assertEquals( false, record.isShadow() );


        assertEquals( 10, record.getRecordSize() );

        record.validateSid((short)0x1017);
    }

    public void testStore()
    {
        BarRecord record = new BarRecord();
        record.setBarSpace( (short)0 );
        record.setCategorySpace( (short)0x96 );
        record.setHorizontal( false );
        record.setStacked( false );
        record.setDisplayAsPercentage( false );
        record.setShadow( false );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
