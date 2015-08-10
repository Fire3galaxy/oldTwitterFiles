package backup;

import java.io.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class testingexcel {
	public static void main(String[] args) {
		try {
			FileInputStream fis = new FileInputStream("doctors (1).xlsx");
			Workbook workbook = new XSSFWorkbook(fis);
			
			Sheet sheet = workbook.getSheetAt(0);
			Row row = sheet.getRow(1);
			Cell cell = row.getCell(9, Row.RETURN_BLANK_AS_NULL);
			
			String s = "";
			
			if (cell != null) s = cell.getStringCellValue();
			
//			for (int i = 0; i < s.length(); i++) {
//				if ( Character.isAlphabetic(s.charAt(i)) ) System.out.print( s.charAt(i) );
//				else if ( Character.isDigit(s.charAt(i)) ) System.out.print( s.charAt(i) ); 
//				else if ( s.charAt(i) == ' ' ) System.out.print(i);
//				else System.out.print("_");
//			}
			
//			System.out.print(s.substring(s.length() - 2));
			
//			System.out.print(s);
			
		} catch (IOException ie){
			ie.printStackTrace();
		}
		
		return;
	}
}