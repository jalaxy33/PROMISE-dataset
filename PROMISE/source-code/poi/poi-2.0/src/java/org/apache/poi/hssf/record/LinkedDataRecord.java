
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
 * Describes a linked data record.  This record referes to the series data or text.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class LinkedDataRecord
    extends Record
{
    public final static short      sid                             = 0x1051;
    private  byte       field_1_linkType;
    public final static byte        LINK_TYPE_TITLE_OR_TEXT        = 0;
    public final static byte        LINK_TYPE_VALUES               = 1;
    public final static byte        LINK_TYPE_CATEGORIES           = 2;
    private  byte       field_2_referenceType;
    public final static byte        REFERENCE_TYPE_DEFAULT_CATEGORIES = 0;
    public final static byte        REFERENCE_TYPE_DIRECT          = 1;
    public final static byte        REFERENCE_TYPE_WORKSHEET       = 2;
    public final static byte        REFERENCE_TYPE_NOT_USED        = 3;
    public final static byte        REFERENCE_TYPE_ERROR_REPORTED  = 4;
    private  short      field_3_options;
    private  BitField   customNumberFormat                          = new BitField(0x1);
    private  short      field_4_indexNumberFmtRecord;
    private  LinkedDataFormulaField field_5_formulaOfLink;


    public LinkedDataRecord()
    {

    }

    /**
     * Constructs a LinkedData record and sets its fields appropriately.
     *
     * @param id    id must be 0x1051 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public LinkedDataRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a LinkedData record and sets its fields appropriately.
     *
     * @param id    id must be 0x1051 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public LinkedDataRecord(short id, short size, byte [] data, int offset)
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
            throw new RecordFormatException("Not a LinkedData record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_linkType               = data[ pos + 0x0 + offset ];
        field_2_referenceType          = data[ pos + 0x1 + offset ];
        field_3_options                = LittleEndian.getShort(data, pos + 0x2 + offset);
        field_4_indexNumberFmtRecord   = LittleEndian.getShort(data, pos + 0x4 + offset);
        field_5_formulaOfLink = new org.apache.poi.hssf.record.LinkedDataFormulaField();
        pos += field_5_formulaOfLink.fillField(data,size,pos + offset + 6);

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AI]\n");
        buffer.append("    .linkType             = ")
            .append("0x").append(HexDump.toHex(  getLinkType ()))
            .append(" (").append( getLinkType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .referenceType        = ")
            .append("0x").append(HexDump.toHex(  getReferenceType ()))
            .append(" (").append( getReferenceType() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .customNumberFormat       = ").append(isCustomNumberFormat()).append('\n'); 
        buffer.append("    .indexNumberFmtRecord = ")
            .append("0x").append(HexDump.toHex(  getIndexNumberFmtRecord ()))
            .append(" (").append( getIndexNumberFmtRecord() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .formulaOfLink        = ")
            .append(" (").append( getFormulaOfLink() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/AI]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        data[ 4 + offset + pos ] = field_1_linkType;
        data[ 5 + offset + pos ] = field_2_referenceType;
        LittleEndian.putShort(data, 6 + offset + pos, field_3_options);
        LittleEndian.putShort(data, 8 + offset + pos, field_4_indexNumberFmtRecord);
        pos += field_5_formulaOfLink.serializeField( pos + 10 + offset, data );

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 1 + 1 + 2 + 2 + field_5_formulaOfLink.getSize();
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
        LinkedDataRecord rec = new LinkedDataRecord();
    
        rec.field_1_linkType = field_1_linkType;
        rec.field_2_referenceType = field_2_referenceType;
        rec.field_3_options = field_3_options;
        rec.field_4_indexNumberFmtRecord = field_4_indexNumberFmtRecord;
        rec.field_5_formulaOfLink = ((org.apache.poi.hssf.record.LinkedDataFormulaField)field_5_formulaOfLink.clone());;
        return rec;
    }




    /**
     * Get the link type field for the LinkedData record.
     *
     * @return  One of 
     *        LINK_TYPE_TITLE_OR_TEXT
     *        LINK_TYPE_VALUES
     *        LINK_TYPE_CATEGORIES
     */
    public byte getLinkType()
    {
        return field_1_linkType;
    }

    /**
     * Set the link type field for the LinkedData record.
     *
     * @param field_1_linkType
     *        One of 
     *        LINK_TYPE_TITLE_OR_TEXT
     *        LINK_TYPE_VALUES
     *        LINK_TYPE_CATEGORIES
     */
    public void setLinkType(byte field_1_linkType)
    {
        this.field_1_linkType = field_1_linkType;
    }

    /**
     * Get the reference type field for the LinkedData record.
     *
     * @return  One of 
     *        REFERENCE_TYPE_DEFAULT_CATEGORIES
     *        REFERENCE_TYPE_DIRECT
     *        REFERENCE_TYPE_WORKSHEET
     *        REFERENCE_TYPE_NOT_USED
     *        REFERENCE_TYPE_ERROR_REPORTED
     */
    public byte getReferenceType()
    {
        return field_2_referenceType;
    }

    /**
     * Set the reference type field for the LinkedData record.
     *
     * @param field_2_referenceType
     *        One of 
     *        REFERENCE_TYPE_DEFAULT_CATEGORIES
     *        REFERENCE_TYPE_DIRECT
     *        REFERENCE_TYPE_WORKSHEET
     *        REFERENCE_TYPE_NOT_USED
     *        REFERENCE_TYPE_ERROR_REPORTED
     */
    public void setReferenceType(byte field_2_referenceType)
    {
        this.field_2_referenceType = field_2_referenceType;
    }

    /**
     * Get the options field for the LinkedData record.
     */
    public short getOptions()
    {
        return field_3_options;
    }

    /**
     * Set the options field for the LinkedData record.
     */
    public void setOptions(short field_3_options)
    {
        this.field_3_options = field_3_options;
    }

    /**
     * Get the index number fmt record field for the LinkedData record.
     */
    public short getIndexNumberFmtRecord()
    {
        return field_4_indexNumberFmtRecord;
    }

    /**
     * Set the index number fmt record field for the LinkedData record.
     */
    public void setIndexNumberFmtRecord(short field_4_indexNumberFmtRecord)
    {
        this.field_4_indexNumberFmtRecord = field_4_indexNumberFmtRecord;
    }

    /**
     * Get the formula of link field for the LinkedData record.
     */
    public LinkedDataFormulaField getFormulaOfLink()
    {
        return field_5_formulaOfLink;
    }

    /**
     * Set the formula of link field for the LinkedData record.
     */
    public void setFormulaOfLink(LinkedDataFormulaField field_5_formulaOfLink)
    {
        this.field_5_formulaOfLink = field_5_formulaOfLink;
    }

    /**
     * Sets the custom number format field value.
     * true if this object has a custom number format
     */
    public void setCustomNumberFormat(boolean value)
    {
        field_3_options = customNumberFormat.setShortBoolean(field_3_options, value);
    }

    /**
     * true if this object has a custom number format
     * @return  the custom number format field value.
     */
    public boolean isCustomNumberFormat()
    {
        return customNumberFormat.isSet(field_3_options);
    }


}  // END OF CLASS




