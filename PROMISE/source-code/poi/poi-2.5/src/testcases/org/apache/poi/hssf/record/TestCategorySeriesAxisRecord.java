
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
 * Tests the serialization and deserialization of the CategorySeriesAxisRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestCategorySeriesAxisRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x01,(byte)0x00,    // crossing point
        (byte)0x01,(byte)0x00,    // label frequency
        (byte)0x01,(byte)0x00,    // tick mark frequency
        (byte)0x01,(byte)0x00     // options
    };

    public TestCategorySeriesAxisRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        CategorySeriesAxisRecord record = new CategorySeriesAxisRecord((short)0x1020, (short)data.length, data);
        assertEquals( 1, record.getCrossingPoint());
        assertEquals( 1, record.getLabelFrequency());
        assertEquals( 1, record.getTickMarkFrequency());
        assertEquals( 1, record.getOptions());
        assertEquals( true, record.isValueAxisCrossing() );
        assertEquals( false, record.isCrossesFarRight() );
        assertEquals( false, record.isReversed() );


        assertEquals( 4 + 8, record.getRecordSize() );

        record.validateSid((short)0x1020);
    }

    public void testStore()
    {
        CategorySeriesAxisRecord record = new CategorySeriesAxisRecord();
        record.setCrossingPoint( (short)1 );
        record.setLabelFrequency( (short)1 );
        record.setTickMarkFrequency( (short)1 );
        record.setValueAxisCrossing( true );
        record.setCrossesFarRight( false );
        record.setReversed( false );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
