
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;

import java.io.FileInputStream;

public class MuckingAround
{
    public static void main(String[] args)
        throws Exception
    {
        POIFSFileSystem fs      =
                new POIFSFileSystem(new FileInputStream("workbook.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        System.out.println(row.getCell((short)0).getDateCellValue());
        System.out.println(row.getCell((short)0).getCellStyle().getDataFormat());
         row = sheet.getRow(1);
        System.out.println(row.getCell((short)0).getNumericCellValue());
    }
}
