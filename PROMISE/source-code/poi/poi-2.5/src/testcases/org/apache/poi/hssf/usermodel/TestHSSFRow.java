
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
        

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Test HSSFRow is okay.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestHSSFRow
        extends TestCase
{
    public TestHSSFRow(String s)
    {
        super(s);
    }

    public void testLastAndFirstColumns()
            throws Exception
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow((short) 0);
        assertEquals(-1, row.getFirstCellNum());
        assertEquals(-1, row.getLastCellNum());

        row.createCell((short) 2);
        assertEquals(2, row.getFirstCellNum());
        assertEquals(2, row.getLastCellNum());

        row.createCell((short) 1);
        assertEquals(1, row.getFirstCellNum());
        assertEquals(2, row.getLastCellNum());

    }

    public void testRemoveCell()
            throws Exception
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow((short) 0);
        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());
        row.createCell((short) 1);
        assertEquals(1, row.getLastCellNum());
        assertEquals(1, row.getFirstCellNum());
        row.createCell((short) 3);
        assertEquals(3, row.getLastCellNum());
        assertEquals(1, row.getFirstCellNum());
        row.removeCell(row.getCell((short) 3));
        assertEquals(1, row.getLastCellNum());
        assertEquals(1, row.getFirstCellNum());
        row.removeCell(row.getCell((short) 1));
        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());

        // check the row record actually writes it out as 0's
        byte[] data = new byte[100];
        row.getRowRecord().serialize(0, data);
        assertEquals(0, data[6]);
        assertEquals(0, data[8]);

        File file = File.createTempFile("XXX", "XLS");
        FileOutputStream stream = new FileOutputStream(file);
        workbook.write(stream);
        stream.close();
        FileInputStream inputStream = new FileInputStream(file);
        workbook = new HSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);
        stream.close();
        file.delete();
        assertEquals(-1, sheet.getRow((short) 0).getLastCellNum());
        assertEquals(-1, sheet.getRow((short) 0).getFirstCellNum());


    }
}
