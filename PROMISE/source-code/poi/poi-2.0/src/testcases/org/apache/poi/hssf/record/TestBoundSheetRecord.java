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
 * Tests BoundSheetRecord.
 *
 * @see BoundSheetRecord
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestBoundSheetRecord
        extends TestCase
{
    public TestBoundSheetRecord( String s )
    {
        super( s );
    }

    public void testRecordLength()
            throws Exception
    {
        BoundSheetRecord record = new BoundSheetRecord();
        record.setCompressedUnicodeFlag((byte)0x00);
        record.setSheetname("Sheet1");
        record.setSheetnameLength((byte)6);

        assertEquals(" 2  +  2  +  4  +   2   +    1     +    1    + len(str)", 18, record.getRecordSize());
    }

    public void testWideRecordLength()
            throws Exception
    {
        BoundSheetRecord record = new BoundSheetRecord();
        record.setCompressedUnicodeFlag((byte)0x01);
        record.setSheetname("Sheet1");
        record.setSheetnameLength((byte)6);

        assertEquals(" 2  +  2  +  4  +   2   +    1     +    1    + len(str) * 2", 24, record.getRecordSize());
    }
    
    public void testName() {
        BoundSheetRecord record = new BoundSheetRecord();
        record.setSheetname("1234567890223456789032345678904");
        assertTrue("Success", true);
        try {
            record.setSheetname("12345678902234567890323456789042");
            assertTrue("Should have thrown IllegalArgumentException, but didnt", false);
        } catch (IllegalArgumentException e) {
            assertTrue("succefully threw exception",true);
        }
        
        try {
            record.setSheetname("s//*s");
            assertTrue("Should have thrown IllegalArgumentException, but didnt", false);
        } catch (IllegalArgumentException e) {
            assertTrue("succefully threw exception",true);
        }
            
    }

}
