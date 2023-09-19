
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


import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the TextRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestTextRecord
        extends TestCase
{
    byte[] data = new byte[] {
        (byte)0x02,                                          // horiz align
        (byte)0x02,                                          // vert align
        (byte)0x01,(byte)0x00,                               // display mode
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,         // rgb color
        (byte)0xD6,(byte)0xFF,(byte)0xFF,(byte)0xFF,         // x
        (byte)0xC4,(byte)0xFF,(byte)0xFF,(byte)0xFF,         // y
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,         // width
        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,         // height
        (byte)0xB1,(byte)0x00,                               // options 1
        (byte)0x4D,(byte)0x00,                               // index of color value
        (byte)0x50,(byte)0x2B,                               // options 2       -- strange upper bits supposed to be 0'd
        (byte)0x00,(byte)0x00                                // text rotation
    };

    public TestTextRecord(String name)
    {
        super(name);
    }

    public void testLoad()
            throws Exception
    {

        TextRecord record = new TextRecord((short)0x1025, (short)data.length, data);
        assertEquals( TextRecord.HORIZONTAL_ALIGNMENT_CENTER, record.getHorizontalAlignment());
        assertEquals( TextRecord.VERTICAL_ALIGNMENT_CENTER, record.getVerticalAlignment());
        assertEquals( TextRecord.DISPLAY_MODE_TRANSPARENT, record.getDisplayMode());
        assertEquals( 0, record.getRgbColor());
        assertEquals( -42, record.getX());
        assertEquals( -60, record.getY());
        assertEquals( 0, record.getWidth());
        assertEquals( 0, record.getHeight());
        assertEquals( 177, record.getOptions1());
        assertEquals( true, record.isAutoColor() );
        assertEquals( false, record.isShowKey() );
        assertEquals( false, record.isShowValue() );
        assertEquals( false, record.isVertical() );
        assertEquals( true, record.isAutoGeneratedText() );
        assertEquals( true, record.isGenerated() );
        assertEquals( false, record.isAutoLabelDeleted() );
        assertEquals( true, record.isAutoBackground() );
        assertEquals(  TextRecord.ROTATION_NONE, record.getRotation() );
        assertEquals( false, record.isShowCategoryLabelAsPercentage() );
        assertEquals( false, record.isShowValueAsPercentage() );
        assertEquals( false, record.isShowBubbleSizes() );
        assertEquals( false, record.isShowLabel() );
        assertEquals( 77, record.getIndexOfColorValue());
        assertEquals( 11088, record.getOptions2());
        assertEquals( 0, record.getDataLabelPlacement() );
        assertEquals( 0, record.getTextRotation());


        assertEquals( 36, record.getRecordSize() );

        record.validateSid((short)0x1025);

    }

    public void testStore()
    {
        TextRecord record = new TextRecord();
        record.setHorizontalAlignment( TextRecord.HORIZONTAL_ALIGNMENT_CENTER );
        record.setVerticalAlignment( TextRecord.VERTICAL_ALIGNMENT_CENTER );
        record.setDisplayMode( TextRecord.DISPLAY_MODE_TRANSPARENT );
        record.setRgbColor( 0 );
        record.setX( -42 );
        record.setY( -60 );
        record.setWidth( 0 );
        record.setHeight( 0 );
        record.setAutoColor( true );
        record.setShowKey( false );
        record.setShowValue( false );
        record.setVertical( false );
        record.setAutoGeneratedText( true );
        record.setGenerated( true );
        record.setAutoLabelDeleted( false );
        record.setAutoBackground( true );
        record.setRotation(  TextRecord.ROTATION_NONE );
        record.setShowCategoryLabelAsPercentage( false );
        record.setShowValueAsPercentage( false );
        record.setShowBubbleSizes( false );
        record.setShowLabel( false );
        record.setIndexOfColorValue( (short)77 );
        record.setOptions2( (short)0x2b50 );
//        record.setDataLabelPlacement( (short)0x2b50 );
        record.setTextRotation( (short)0 );


        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
}
