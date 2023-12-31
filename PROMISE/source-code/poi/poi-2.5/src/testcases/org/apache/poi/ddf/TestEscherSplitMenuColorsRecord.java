/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

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
package org.apache.poi.ddf;

import junit.framework.TestCase;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public class TestEscherSplitMenuColorsRecord extends TestCase
{
    public void testSerialize() throws Exception
    {
        EscherSplitMenuColorsRecord r = createRecord();

        byte[] data = new byte[24];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 24, bytesWritten );
        assertEquals( "[40, 00, " +
                "1E, F1, " +
                "10, 00, 00, 00, " +
                "02, 04, 00, 00, " +
                "02, 00, 00, 00, " +
                "02, 00, 00, 00, " +
                "01, 00, 00, 00, ]",
                HexDump.toHex( data ) );
    }

    public void testFillFields() throws Exception
    {
        String hexData = "40 00 " +
                "1E F1 " +
                "10 00 00 00 " +
                "02 04 00 00 " +
                "02 00 00 00 " +
                "02 00 00 00 " +
                "01 00 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherSplitMenuColorsRecord r = new EscherSplitMenuColorsRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 24, bytesWritten );
        assertEquals( 0x0402, r.getColor1() );
        assertEquals( 0x02, r.getColor2() );
        assertEquals( 0x02, r.getColor3() );
        assertEquals( 0x01, r.getColor4() );
    }

    public void testToString() throws Exception
    {
        String nl = System.getProperty("line.separator");

        String expected = "org.apache.poi.ddf.EscherSplitMenuColorsRecord:" + nl +
                "  RecordId: 0xF11E" + nl +
                "  Options: 0x0040" + nl +
                "  Color1: 0x00000402" + nl +
                "  Color2: 0x00000002" + nl +
                "  Color3: 0x00000002" + nl +
                "  Color4: 0x00000001" + nl +
                "";
        assertEquals( expected, createRecord().toString() );
    }

    private EscherSplitMenuColorsRecord createRecord()
    {
        EscherSplitMenuColorsRecord r = new EscherSplitMenuColorsRecord();
        r.setOptions( (short) 0x0040 );
        r.setRecordId( EscherSplitMenuColorsRecord.RECORD_ID );
        r.setColor1( 0x402  );
        r.setColor2( 0x2 );
        r.setColor3( 0x2 );
        r.setColor4( 0x1 );
        return r;
    }

}
