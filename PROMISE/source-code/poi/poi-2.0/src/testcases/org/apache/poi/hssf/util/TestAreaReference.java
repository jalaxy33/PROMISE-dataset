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

package org.apache.poi.hssf.util;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class TestAreaReference extends TestCase {
     public TestAreaReference(String s) {
        super(s);
    }
    public void testAreaRef1() {
        AreaReference ar = new AreaReference("$A$1:$B$2");
        assertTrue("Two cells expected",ar.getCells().length == 2);
        CellReference cf = ar.getCells()[0];
        assertTrue("row is 4",cf.getRow()==0);
        assertTrue("col is 1",cf.getCol()==0);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $A$1",cf.toString().equals("$A$1"));
        
        cf = ar.getCells()[1];
        assertTrue("row is 4",cf.getRow()==1);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $B$2",cf.toString().equals("$B$2"));
    }
    
    /**
     * References failed when sheet names were being used
     * Reported by Arne.Clauss@gedas.de
     */
    public void testReferenceWithSheet() {
    	String ref = "Tabelle1!$B$5";
		AreaReference myAreaReference = new AreaReference(ref);
		CellReference[] myCellReference = myAreaReference.getCells();
		
		assertNotNull("cell reference not null : "+myCellReference[0]);
    	assertEquals("Not Column B", (short)1,myCellReference[0].getCol());
		assertEquals("Not Row 5", 4,myCellReference[0].getRow());
    }
    
	public static void main(java.lang.String[] args) {        
		junit.textui.TestRunner.run(TestAreaReference.class);
	}
        
}