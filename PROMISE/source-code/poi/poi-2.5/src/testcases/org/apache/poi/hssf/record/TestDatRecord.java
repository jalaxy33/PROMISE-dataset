
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
 * Tests the serialization and deserialization of the DatRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestDatRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x0D,(byte)0x00   // options
    };

    public TestDatRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        DatRecord record = new DatRecord((short)0x1063, (short)data.length, data);
        assertEquals( 0xD, record.getOptions());
        assertEquals( true, record.isHorizontalBorder() );
        assertEquals( false, record.isVerticalBorder() );
        assertEquals( true, record.isBorder() );
        assertEquals( true, record.isShowSeriesKey() );


        assertEquals( 6, record.getRecordSize() );

        record.validateSid((short)0x1063);
    }

    public void testStore()
    {
        DatRecord record = new DatRecord();
        record.setHorizontalBorder( true );
        record.setVerticalBorder( false );
        record.setBorder( true );
        record.setShowSeriesKey( true );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
