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

package org.apache.poi.hssf.record.formula;

import java.util.List;

import org.apache.poi.hssf.model.Workbook;

/**
 * Unary Plus operator
 * does not have any effect on the operand
 * @author Avik Sengupta
 */

public class UnaryMinusPtg extends OperationPtg
{
    public final static int  SIZE = 1;
    public final static byte sid  = 0x13;
    
    private final static String MINUS = "-";

    /** Creates new AddPtg */

    public UnaryMinusPtg()
    {
    }

    public UnaryMinusPtg(byte[] data, int offset)
    {

        // doesn't need anything
    }
    
   
    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
    }

    public int getSize()
    {
        return SIZE;
    }

    public int getType()
    {
        return this.TYPE_UNARY;
    }

    public int getNumberOfOperands()
    {
        return 1;
    }
    
    /** Implementation of method from Ptg */
    public String toFormulaString(Workbook book)
    {
        return "+";
    }
       
   /** implementation of method from OperationsPtg*/  
    public String toFormulaString(String[] operands) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(MINUS);
        buffer.append(operands[ 0]);
        return buffer.toString();
    }
    
    public byte getDefaultOperandClass() {return Ptg.CLASS_VALUE;}
           
    public Object clone() {
      return new UnaryPlusPtg();
    }

}
