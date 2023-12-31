
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hssf.record;



import org.apache.poi.util.*;

/**
 * The axis size and location
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class AxisParentRecord
    extends Record
{
    public final static short      sid                             = 0x1041;
    private  short      field_1_axisType;
    public final static short       AXIS_TYPE_MAIN                 = 0;
    public final static short       AXIS_TYPE_SECONDARY            = 1;
    private  int        field_2_x;
    private  int        field_3_y;
    private  int        field_4_width;
    private  int        field_5_height;


    public AxisParentRecord()
    {

    }

    /**
     * Constructs a AxisParent record and sets its fields appropriately.
     *
     * @param id    id must be 0x1041 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public AxisParentRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a AxisParent record and sets its fields appropriately.
     *
     * @param id    id must be 0x1041 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public AxisParentRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a AxisParent record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_axisType               = LittleEndian.getShort(data, pos + 0x0 + offset);
        field_2_x                      = LittleEndian.getInt(data, pos + 0x2 + offset);
        field_3_y                      = LittleEndian.getInt(data, pos + 0x6 + offset);
        field_4_width                  = LittleEndian.getInt(data, pos + 0xa + offset);
        field_5_height                 = LittleEndian.getInt(data, pos + 0xe + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AXISPARENT]\n");
        buffer.append("    .axisType             = ")
            .append("0x").append(HexDump.toHex(  getAxisType ()))
            .append(" (").append( getAxisType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .x                    = ")
            .append("0x").append(HexDump.toHex(  getX ()))
            .append(" (").append( getX() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .y                    = ")
            .append("0x").append(HexDump.toHex(  getY ()))
            .append(" (").append( getY() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .width                = ")
            .append("0x").append(HexDump.toHex(  getWidth ()))
            .append(" (").append( getWidth() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .height               = ")
            .append("0x").append(HexDump.toHex(  getHeight ()))
            .append(" (").append( getHeight() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/AXISPARENT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_axisType);
        LittleEndian.putInt(data, 6 + offset + pos, field_2_x);
        LittleEndian.putInt(data, 10 + offset + pos, field_3_y);
        LittleEndian.putInt(data, 14 + offset + pos, field_4_width);
        LittleEndian.putInt(data, 18 + offset + pos, field_5_height);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 4 + 4 + 4 + 4;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        AxisParentRecord rec = new AxisParentRecord();
    
        rec.field_1_axisType = field_1_axisType;
        rec.field_2_x = field_2_x;
        rec.field_3_y = field_3_y;
        rec.field_4_width = field_4_width;
        rec.field_5_height = field_5_height;
        return rec;
    }




    /**
     * Get the axis type field for the AxisParent record.
     *
     * @return  One of 
     *        AXIS_TYPE_MAIN
     *        AXIS_TYPE_SECONDARY
     */
    public short getAxisType()
    {
        return field_1_axisType;
    }

    /**
     * Set the axis type field for the AxisParent record.
     *
     * @param field_1_axisType
     *        One of 
     *        AXIS_TYPE_MAIN
     *        AXIS_TYPE_SECONDARY
     */
    public void setAxisType(short field_1_axisType)
    {
        this.field_1_axisType = field_1_axisType;
    }

    /**
     * Get the x field for the AxisParent record.
     */
    public int getX()
    {
        return field_2_x;
    }

    /**
     * Set the x field for the AxisParent record.
     */
    public void setX(int field_2_x)
    {
        this.field_2_x = field_2_x;
    }

    /**
     * Get the y field for the AxisParent record.
     */
    public int getY()
    {
        return field_3_y;
    }

    /**
     * Set the y field for the AxisParent record.
     */
    public void setY(int field_3_y)
    {
        this.field_3_y = field_3_y;
    }

    /**
     * Get the width field for the AxisParent record.
     */
    public int getWidth()
    {
        return field_4_width;
    }

    /**
     * Set the width field for the AxisParent record.
     */
    public void setWidth(int field_4_width)
    {
        this.field_4_width = field_4_width;
    }

    /**
     * Get the height field for the AxisParent record.
     */
    public int getHeight()
    {
        return field_5_height;
    }

    /**
     * Set the height field for the AxisParent record.
     */
    public void setHeight(int field_5_height)
    {
        this.field_5_height = field_5_height;
    }


}  // END OF CLASS




