<%@page contentType="text/html" 
import="java.io.*,org.apache.poi.poifs.filesystem.POIFSFileSystem,org.apache.poi
.hssf.record.*,org.apache.poi.hssf.model.*,org.apache.poi.hssf.usermodel.*,org.a
pache.poi.hssf.util.*" %>
<html>
<head><title>Read Excel file </title>
</head>
<body>
An example of using Jakarta POI's HSSF package to read an excel spreadsheet: 


<form name="form1" method="get" action="">
Select an Excel file to read. 
  <input type="file" name="xls_filename" onChange="form1.submit()">
</form>

<%
	String filename = request.getParameter("xls_filename"); 
	if (filename != null && !filename.equals("")) {
%>	
	<br>You chose the file <%= filename %>. 
	<br><br>It's contents are: 	
<%	
            try
            {

                // create a poi workbook from the excel spreadsheet file
                POIFSFileSystem fs =
                    new POIFSFileSystem(new FileInputStream(filename));
                HSSFWorkbook wb = new HSSFWorkbook(fs);

                for (int k = 0; k < wb.getNumberOfSheets(); k++)
                {
%>				
                    <br><br>Sheet  <%= k %> <br>
<%					
					
                    HSSFSheet sheet = wb.getSheetAt(k);
                    int       rows  = sheet.getPhysicalNumberOfRows();

                    for (int r = 0; r < rows; r++)
                    {
                        HSSFRow row   = sheet.getRow(r);
                        if (row != null) { 
                            int     cells = row.getPhysicalNumberOfCells();
%>
							<br><b>ROW  <%= 
row.getRowNum() %> </b>
<%
                            for (short c = 0; c < cells; c++) 
                            { 
                                HSSFCell cell  = row.getCell(c);
                                if (cell != null) { 
                                    String   value = null;

                                    switch (cell.getCellType())
                                    {

                                        case HSSFCell.CELL_TYPE_FORMULA :
                                            value = "FORMULA ";
                                            break;

                                        case HSSFCell.CELL_TYPE_NUMERIC :
                                            value = "NUMERIC value="
                                                    + cell.getNumericCellValue
();
                                            break;

                                        case HSSFCell.CELL_TYPE_STRING :
                                            value = "STRING value="
                                                    + cell.getStringCellValue();
                                            break;

                                        default :
                                    }
%>									
                                    <%= "CELL col=" 
									
	+ cell.getCellNum()
                                        + " VALUE=" + value %>
<%
                                } 
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
%>
	Error occurred:  <%= e.getMessage() %>
<%			
                e.printStackTrace();
            }

	} 
%> 
</body>
</html>

