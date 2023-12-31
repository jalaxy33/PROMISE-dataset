
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
 * Tests the serialization and deserialization of the LegendRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public class TestLegendRecord
        extends TestCase
{
    byte[] data = new byte[] {
	(byte)0x76,(byte)0x0E,(byte)0x00,(byte)0x00,(byte)0x86,(byte)0x07,(byte)0x00,(byte)0x00,(byte)0x19,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x8B,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x1F,(byte)0x00
    };

    public TestLegendRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {
        LegendRecord record = new LegendRecord((short)0x1015, (short)data.length, data);
        

        assertEquals( (int)0xe76, record.getXAxisUpperLeft());

        assertEquals( (int)0x786, record.getYAxisUpperLeft());

        assertEquals( (int)0x119, record.getXSize());

        assertEquals( (int)0x8b, record.getYSize());

        assertEquals( (byte)0x3, record.getType());

        assertEquals( (byte)0x1, record.getSpacing());

        assertEquals( (short)0x1f, record.getOptions());
        assertEquals( true, record.isAutoPosition() );
        assertEquals( true, record.isAutoSeries() );
        assertEquals( true, record.isAutoXPositioning() );
        assertEquals( true, record.isAutoYPositioning() );
        assertEquals( true, record.isVertical() );
        assertEquals( false, record.isDataTable() );


        assertEquals( 24, record.getRecordSize() );

        record.validateSid((short)0x1015);
    }

    public void testStore()
    {
        LegendRecord record = new LegendRecord();



        record.setXAxisUpperLeft( (int)0xe76 );

        record.setYAxisUpperLeft( (int)0x786 );

        record.setXSize( (int)0x119 );

        record.setYSize( (int)0x8b );

        record.setType( (byte)0x3 );

        record.setSpacing( (byte)0x1 );

        record.setOptions( (short)0x1f );
        record.setAutoPosition( true );
        record.setAutoSeries( true );
        record.setAutoXPositioning( true );
        record.setAutoYPositioning( true );
        record.setVertical( true );
        record.setDataTable( false );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
