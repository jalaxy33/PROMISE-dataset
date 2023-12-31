
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
 * The Tick record defines how tick marks and label positioning/formatting
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Andrew C. Oliver(acoliver at apache.org)
 */
public class TickRecord
    extends Record
{
    public final static short      sid                             = 0x101e;
    private  byte       field_1_majorTickType;
    private  byte       field_2_minorTickType;
    private  byte       field_3_labelPosition;
    private  byte       field_4_background;
    private  int        field_5_labelColorRgb;
    private  short field_6_zero1;
    private  short field_7_zero2;
    private  short      field_8_options;
    private  BitField   autoTextColor                               = new BitField(0x1);
    private  BitField   autoTextBackground                          = new BitField(0x2);
    private BitField   rotation                                   = new BitField(0x1c);
    private  BitField   autorotate                                  = new BitField(0x20);
    private  short      field_9_tickColor;
    private  short      field_10_zero3;


    public TickRecord()
    {

    }

    /**
     * Constructs a Tick record and sets its fields appropriately.
     *
     * @param id    id must be 0x101e or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public TickRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a Tick record and sets its fields appropriately.
     *
     * @param id    id must be 0x101e or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public TickRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a Tick record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_majorTickType          = data[ pos + 0x0 + offset ];
        field_2_minorTickType          = data[ pos + 0x1 + offset ];
        field_3_labelPosition          = data[ pos + 0x2 + offset ];
        field_4_background             = data[ pos + 0x3 + offset ];
        field_5_labelColorRgb          = LittleEndian.getInt(data, pos + 0x4 + offset);
        field_6_zero1                  = LittleEndian.getShort(data, pos + 0x8 + offset);
        field_7_zero2                  = LittleEndian.getShort(data, pos + 0x10 + offset);
        field_8_options                = LittleEndian.getShort(data, pos + 0x18 + offset);
        field_9_tickColor              = LittleEndian.getShort(data, pos + 0x1a + offset);
        field_10_zero3                 = LittleEndian.getShort(data, pos + 0x1c + offset);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[TICK]\n");
        buffer.append("    .majorTickType        = ")
            .append("0x").append(HexDump.toHex(  getMajorTickType ()))
            .append(" (").append( getMajorTickType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .minorTickType        = ")
            .append("0x").append(HexDump.toHex(  getMinorTickType ()))
            .append(" (").append( getMinorTickType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .labelPosition        = ")
            .append("0x").append(HexDump.toHex(  getLabelPosition ()))
            .append(" (").append( getLabelPosition() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .background           = ")
            .append("0x").append(HexDump.toHex(  getBackground ()))
            .append(" (").append( getBackground() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .labelColorRgb        = ")
            .append("0x").append(HexDump.toHex(  getLabelColorRgb ()))
            .append(" (").append( getLabelColorRgb() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .zero1                = ")
            .append("0x").append(HexDump.toHex(  getZero1 ()))
            .append(" (").append( getZero1() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .zero2                = ")
            .append("0x").append(HexDump.toHex(  getZero2 ()))
            .append(" (").append( getZero2() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .autoTextColor            = ").append(isAutoTextColor()).append('\n'); 
        buffer.append("         .autoTextBackground       = ").append(isAutoTextBackground()).append('\n'); 
            buffer.append("         .rotation                 = ").append(getRotation()).append('\n'); 
        buffer.append("         .autorotate               = ").append(isAutorotate()).append('\n'); 
        buffer.append("    .tickColor            = ")
            .append("0x").append(HexDump.toHex(  getTickColor ()))
            .append(" (").append( getTickColor() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .zero3                = ")
            .append("0x").append(HexDump.toHex(  getZero3 ()))
            .append(" (").append( getZero3() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/TICK]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        data[ 4 + offset + pos ] = field_1_majorTickType;
        data[ 5 + offset + pos ] = field_2_minorTickType;
        data[ 6 + offset + pos ] = field_3_labelPosition;
        data[ 7 + offset + pos ] = field_4_background;
        LittleEndian.putInt(data, 8 + offset + pos, field_5_labelColorRgb);
        LittleEndian.putShort(data, 12 + offset + pos, field_6_zero1);
        LittleEndian.putShort(data, 20 + offset + pos, field_7_zero2);
        LittleEndian.putShort(data, 28 + offset + pos, field_8_options);
        LittleEndian.putShort(data, 30 + offset + pos, field_9_tickColor);
        LittleEndian.putShort(data, 32 + offset + pos, field_10_zero3);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 1 + 1 + 1 + 1 + 4 + 8 + 8 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        TickRecord rec = new TickRecord();
    
        rec.field_1_majorTickType = field_1_majorTickType;
        rec.field_2_minorTickType = field_2_minorTickType;
        rec.field_3_labelPosition = field_3_labelPosition;
        rec.field_4_background = field_4_background;
        rec.field_5_labelColorRgb = field_5_labelColorRgb;
        rec.field_6_zero1 = field_6_zero1;
        rec.field_7_zero2 = field_7_zero2;
        rec.field_8_options = field_8_options;
        rec.field_9_tickColor = field_9_tickColor;
        rec.field_10_zero3 = field_10_zero3;
        return rec;
    }




    /**
     * Get the major tick type field for the Tick record.
     */
    public byte getMajorTickType()
    {
        return field_1_majorTickType;
    }

    /**
     * Set the major tick type field for the Tick record.
     */
    public void setMajorTickType(byte field_1_majorTickType)
    {
        this.field_1_majorTickType = field_1_majorTickType;
    }

    /**
     * Get the minor tick type field for the Tick record.
     */
    public byte getMinorTickType()
    {
        return field_2_minorTickType;
    }

    /**
     * Set the minor tick type field for the Tick record.
     */
    public void setMinorTickType(byte field_2_minorTickType)
    {
        this.field_2_minorTickType = field_2_minorTickType;
    }

    /**
     * Get the label position field for the Tick record.
     */
    public byte getLabelPosition()
    {
        return field_3_labelPosition;
    }

    /**
     * Set the label position field for the Tick record.
     */
    public void setLabelPosition(byte field_3_labelPosition)
    {
        this.field_3_labelPosition = field_3_labelPosition;
    }

    /**
     * Get the background field for the Tick record.
     */
    public byte getBackground()
    {
        return field_4_background;
    }

    /**
     * Set the background field for the Tick record.
     */
    public void setBackground(byte field_4_background)
    {
        this.field_4_background = field_4_background;
    }

    /**
     * Get the label color rgb field for the Tick record.
     */
    public int getLabelColorRgb()
    {
        return field_5_labelColorRgb;
    }

    /**
     * Set the label color rgb field for the Tick record.
     */
    public void setLabelColorRgb(int field_5_labelColorRgb)
    {
        this.field_5_labelColorRgb = field_5_labelColorRgb;
    }

    /**
     * Get the zero 1 field for the Tick record.
     */
    public short getZero1()
    {
        return field_6_zero1;
    }

    /**
     * Set the zero 1 field for the Tick record.
     */
    public void setZero1(short field_6_zero1)
    {
        this.field_6_zero1 = field_6_zero1;
    }

    /**
     * Get the zero 2 field for the Tick record.
     */
    public short getZero2()
    {
        return field_7_zero2;
    }

    /**
     * Set the zero 2 field for the Tick record.
     */
    public void setZero2(short field_7_zero2)
    {
        this.field_7_zero2 = field_7_zero2;
    }

    /**
     * Get the options field for the Tick record.
     */
    public short getOptions()
    {
        return field_8_options;
    }

    /**
     * Set the options field for the Tick record.
     */
    public void setOptions(short field_8_options)
    {
        this.field_8_options = field_8_options;
    }

    /**
     * Get the tick color field for the Tick record.
     */
    public short getTickColor()
    {
        return field_9_tickColor;
    }

    /**
     * Set the tick color field for the Tick record.
     */
    public void setTickColor(short field_9_tickColor)
    {
        this.field_9_tickColor = field_9_tickColor;
    }

    /**
     * Get the zero 3 field for the Tick record.
     */
    public short getZero3()
    {
        return field_10_zero3;
    }

    /**
     * Set the zero 3 field for the Tick record.
     */
    public void setZero3(short field_10_zero3)
    {
        this.field_10_zero3 = field_10_zero3;
    }

    /**
     * Sets the auto text color field value.
     * use the quote unquote automatic color for text
     */
    public void setAutoTextColor(boolean value)
    {
        field_8_options = autoTextColor.setShortBoolean(field_8_options, value);
    }

    /**
     * use the quote unquote automatic color for text
     * @return  the auto text color field value.
     */
    public boolean isAutoTextColor()
    {
        return autoTextColor.isSet(field_8_options);
    }

    /**
     * Sets the auto text background field value.
     * use the quote unquote automatic color for text background
     */
    public void setAutoTextBackground(boolean value)
    {
        field_8_options = autoTextBackground.setShortBoolean(field_8_options, value);
    }

    /**
     * use the quote unquote automatic color for text background
     * @return  the auto text background field value.
     */
    public boolean isAutoTextBackground()
    {
        return autoTextBackground.isSet(field_8_options);
    }

    /**
     * Sets the rotation field value.
     * rotate text (0=none, 1=normal, 2=90 degrees counterclockwise, 3=90 degrees clockwise)
     */
    public void setRotation(short value)
    {
        field_8_options = rotation.setShortValue(field_8_options, value);
    }

    /**
     * rotate text (0=none, 1=normal, 2=90 degrees counterclockwise, 3=90 degrees clockwise)
     * @return  the rotation field value.
     */
    public short getRotation()
    {
        return rotation.getShortValue(field_8_options);
    }

    /**
     * Sets the autorotate field value.
     * automatically rotate the text
     */
    public void setAutorotate(boolean value)
    {
        field_8_options = autorotate.setShortBoolean(field_8_options, value);
    }

    /**
     * automatically rotate the text
     * @return  the autorotate field value.
     */
    public boolean isAutorotate()
    {
        return autorotate.isSet(field_8_options);
    }


}  // END OF CLASS




