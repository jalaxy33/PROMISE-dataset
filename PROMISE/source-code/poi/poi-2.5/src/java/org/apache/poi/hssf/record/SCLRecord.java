
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



import org.apache.poi.util.*;

/**
 * Specifies the window's zoom magnification.  If this record isn't present then the windows zoom is 100%. see p384 Excel Dev Kit
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public class SCLRecord
    extends Record
{
    public final static short      sid                             = 0xa0;
    private  short      field_1_numerator;
    private  short      field_2_denominator;


    public SCLRecord()
    {

    }

    /**
     * Constructs a SCL record and sets its fields appropriately.
     *
     * @param id    id must be 0xa0 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public SCLRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a SCL record and sets its fields appropriately.
     *
     * @param id    id must be 0xa0 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SCLRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a SCL record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_numerator              = LittleEndian.getShort(data, pos + 0x0 + offset);
        field_2_denominator            = LittleEndian.getShort(data, pos + 0x2 + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SCL]\n");
        buffer.append("    .numerator            = ")
            .append("0x").append(HexDump.toHex(  getNumerator ()))
            .append(" (").append( getNumerator() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .denominator          = ")
            .append("0x").append(HexDump.toHex(  getDenominator ()))
            .append(" (").append( getDenominator() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/SCL]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_numerator);
        LittleEndian.putShort(data, 6 + offset + pos, field_2_denominator);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        SCLRecord rec = new SCLRecord();
    
        rec.field_1_numerator = field_1_numerator;
        rec.field_2_denominator = field_2_denominator;
        return rec;
    }




    /**
     * Get the numerator field for the SCL record.
     */
    public short getNumerator()
    {
        return field_1_numerator;
    }

    /**
     * Set the numerator field for the SCL record.
     */
    public void setNumerator(short field_1_numerator)
    {
        this.field_1_numerator = field_1_numerator;
    }

    /**
     * Get the denominator field for the SCL record.
     */
    public short getDenominator()
    {
        return field_2_denominator;
    }

    /**
     * Set the denominator field for the SCL record.
     */
    public void setDenominator(short field_2_denominator)
    {
        this.field_2_denominator = field_2_denominator;
    }


}  // END OF CLASS




