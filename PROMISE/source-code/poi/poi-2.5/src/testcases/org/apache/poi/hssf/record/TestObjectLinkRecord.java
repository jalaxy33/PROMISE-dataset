
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
 * Tests the serialization and deserialization of the ObjectLinkRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public class TestObjectLinkRecord
        extends TestCase
{
    byte[] data = new byte[] {
	(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00
    };

    public TestObjectLinkRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {
        ObjectLinkRecord record = new ObjectLinkRecord((short)0x1027, (short)data.length, data);
        

        assertEquals( (short)3, record.getAnchorId());

        assertEquals( (short)0x00, record.getLink1());

        assertEquals( (short)0x00, record.getLink2());


        assertEquals( 10, record.getRecordSize() );

        record.validateSid((short)0x1027);
    }

    public void testStore()
    {
        ObjectLinkRecord record = new ObjectLinkRecord();



        record.setAnchorId( (short)3 );

        record.setLink1( (short)0x00 );

        record.setLink2( (short)0x00 );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
