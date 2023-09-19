
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2003 The Apache Software Foundation.  All rights
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
 * Tests the serialization and deserialization of the FormulaRecord
 * class works correctly.  
 *
 * @author Andrew C. Oliver 
 */
public class TestFormulaRecord
        extends TestCase
{

    public TestFormulaRecord(String name)
    {
        super(name);
    }

    public void testCreateFormulaRecord () {
        FormulaRecord record = new FormulaRecord();
        record.setColumn((short)0);
        //record.setRow((short)1);
        record.setRow(1);
        record.setXFIndex((short)4);
        
        assertEquals(record.getColumn(),(short)0);
        //assertEquals(record.getRow(),(short)1);
        assertEquals((short)record.getRow(),(short)1);
        assertEquals(record.getXFIndex(),(short)4);
    }
    
    /**
     * Make sure a NAN value is preserved
     * This formula record is a representation of =1/0 at row 0, column 0 
     */
    public void testCheckNanPreserve() {
    	byte[] formulaByte = new byte[29];
    	for (int i = 0; i < formulaByte.length; i++) formulaByte[i] = (byte)0;
    	formulaByte[4] = (byte)0x0F;
		formulaByte[6] = (byte)0x02;
		formulaByte[8] = (byte)0x07;
		formulaByte[12] = (byte)0xFF;
		formulaByte[13] = (byte)0xFF;
		formulaByte[18] = (byte)0xE0;
		formulaByte[19] = (byte)0xFC;
		formulaByte[20] = (byte)0x07;
		formulaByte[22] = (byte)0x1E;
		formulaByte[23] = (byte)0x01;
		formulaByte[25] = (byte)0x1E;
		formulaByte[28] = (byte)0x06;
    	
		FormulaRecord record = new FormulaRecord(FormulaRecord.sid, (short)29, formulaByte);
		assertEquals("Row", 0, record.getRow());
		assertEquals("Column", 0, record.getColumn());		
		assertTrue("Value is not NaN", Double.isNaN(record.getValue()));
		
		byte[] output = record.serialize();
		assertEquals("Output size", 33, output.length); //includes sid+recordlength
		
		for (int i = 5; i < 13;i++) {
			assertEquals("FormulaByte NaN doesn't match", formulaByte[i], output[i+4]);
		}
		
    }
    
    /**
     * Tests to see if the shared formula cells properly reserialize the expPtg
     *
     */
    public void testExpFormula() {
    	byte[] formulaByte = new byte[27];
    	
		for (int i = 0; i < formulaByte.length; i++) formulaByte[i] = (byte)0;
    	
    	formulaByte[4] =(byte)0x0F;
		formulaByte[14]=(byte)0x08;
		formulaByte[18]=(byte)0xE0;
		formulaByte[19]=(byte)0xFD;
		formulaByte[20]=(byte)0x05;
		formulaByte[22]=(byte)0x01;
		FormulaRecord record = new FormulaRecord(FormulaRecord.sid, (short)27, formulaByte);
		assertEquals("Row", 0, record.getRow());
		assertEquals("Column", 0, record.getColumn());
		byte[] output = record.serialize();
		assertEquals("Output size", 31, output.length); //includes sid+recordlength
    	assertEquals("Offset 22", 1, output[26]);
    }
    
    
    public static void main(String [] ignored_args)
    {
        String filename = System.getProperty("HSSF.testdata.path");

        System.out
            .println("Testing org.apache.poi.hssf.record.FormulaRecord");
        junit.textui.TestRunner.run(TestFormulaRecord.class);
    }
    
    
}
