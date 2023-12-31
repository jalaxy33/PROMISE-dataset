
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
 * Tests the serialization and deserialization of the LineFormatRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestLineFormatRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,    // colour
        (byte)0x00,(byte)0x00,                          // pattern
        (byte)0x00,(byte)0x00,                          // weight
        (byte)0x01,(byte)0x00,                          // format
        (byte)0x4D,(byte)0x00                           // index
    };

    public TestLineFormatRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {
        LineFormatRecord record = new LineFormatRecord((short)0x1007, (short)data.length, data);
        assertEquals( 0, record.getLineColor());
        assertEquals( 0, record.getLinePattern());
        assertEquals( 0, record.getWeight());
        assertEquals( 1, record.getFormat());
        assertEquals( true, record.isAuto() );
        assertEquals( false, record.isDrawTicks() );
        assertEquals( 0x4d, record.getColourPaletteIndex());


        assertEquals( 16, record.getRecordSize() );

        record.validateSid((short)0x1007);
    }

    public void testStore()
    {
        LineFormatRecord record = new LineFormatRecord();
        record.setLineColor( 0 );
        record.setLinePattern( (short)0 );
        record.setWeight( (short)0 );
        record.setAuto( true );
        record.setDrawTicks( false );
        record.setColourPaletteIndex( (short)0x4d );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
