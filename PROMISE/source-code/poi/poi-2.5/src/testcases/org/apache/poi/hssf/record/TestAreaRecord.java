
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
 * Tests the serialization and deserialization of the AreaRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestAreaRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x02,(byte)0x00       // format flags
    };

    public TestAreaRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        AreaRecord record = new AreaRecord((short)0x101A, (short)data.length, data);
        assertEquals( 2, record.getFormatFlags());
        assertEquals( false, record.isStacked() );
        assertEquals( true, record.isDisplayAsPercentage() );
        assertEquals( false, record.isShadow() );


        assertEquals( 6, record.getRecordSize() );

        record.validateSid((short)0x101A);
    }

    public void testStore()
    {
        AreaRecord record = new AreaRecord();
        record.setStacked( false );
        record.setDisplayAsPercentage( true );
        record.setShadow( false );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
