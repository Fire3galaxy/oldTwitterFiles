/* ReadFile is responsible for reading the information from the xlsx file of doctors and
 * storing the information into a list of ProfileDr objects so that the program can search
 * for similar profiles on twitter.
 */
package excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import keywords.Places;

public class ExcelMethods implements Serializable {
	/**
	 * De-serializing safety.
	 */
	private static final long serialVersionUID = 1L;
	public List<ProfileDr> file; // Array of Doctors from excel file
	Map<String, IntHolder> freqOfName; // frequency of names
	transient Places places;
	Map<String, TreeMap<String, List<ProfileDr>>> namesLog; // first name, last name, matching profiles
	
	public ExcelMethods() {
		file = new ArrayList<ProfileDr>();
		freqOfName = new TreeMap<String, IntHolder>();
		places = new Places();
		namesLog = new TreeMap<String, TreeMap<String, List<ProfileDr>>>();
	}
	public List<ProfileDr> getFile() {
		return file;
	}
	public Map<String, IntHolder> getFreqOfName() {
		return freqOfName;
	}
	public Map<String, TreeMap<String, List<ProfileDr>>> getNamesLog() {
		return namesLog;
	}
	public void readFile(String filename) {
		// I assume the xlsx file follows the structure set in "doctors.xlsx"
		// with first and last name in the 2nd and 4th column.
		// this could be changed to look for "First Name" and "Last Name"
		// in the first row then use a for loop to always look in those columns...
		try {
			FileInputStream fis = new FileInputStream(filename);
			Workbook workbook = new XSSFWorkbook(fis);
			
			int numberOfSheets = workbook.getNumberOfSheets();
			// System.out.println("Number of Sheets: " + numberOfSheets); // Fix why statement not showing
			
			for (int i = 0; i < numberOfSheets; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				
				Iterator<Row> rowIterator = sheet.iterator();
				rowIterator.next(); // skips first row
				
				while (rowIterator.hasNext()) {
					// FMU (For my understanding) .next() returns the current row AND advances the iterator
					Row row = rowIterator.next();
					
//					Iterator<Cell> cellIterator = row.cellIterator();
					String firstName = "", 
							middleName = "", 
							lastName = "",
							title = "",
							city = "",
							location = "";
					List<String> specialty = new ArrayList<String>();
					int stateNum = -1;
					
					
					Cell cell = row.getCell(1, Row.RETURN_BLANK_AS_NULL); // first name
//					cell = cellIterator.next(); // skip to 2nd cell
					firstName = cell.getStringCellValue().trim().toLowerCase();
					
					cell = row.getCell(2, Row.RETURN_BLANK_AS_NULL); // middle name
					if (cell != null) 
						middleName = cell.getStringCellValue().trim().toLowerCase();
					
					cell = row.getCell(3, Row.RETURN_BLANK_AS_NULL); // last name
//					cell = cellIterator.next();
//					cell = cellIterator.next(); // skip to 4th cell
					lastName = cell.getStringCellValue().toLowerCase().trim();
					
					cell = row.getCell(5, Row.RETURN_BLANK_AS_NULL);
					if (cell != null) title = cell.getStringCellValue().trim();
					
					cell = row.getCell(8, Row.RETURN_BLANK_AS_NULL);
					String s = cell.getStringCellValue().toLowerCase().trim();
					int start = 0, end = 0;
					for (end = 0; end < s.length(); end++) {
						if (s.charAt(end) == ';') {
//							System.out.print(s.substring(start, end) + " | ");
							
							specialty.add(s.substring(start, end));
							start = end + 1; // first letter of next word
						}
					}
//					System.out.println(s.substring(start, end));
					specialty.add(s.substring(start, end)); // last specialty/only specialty
					
					cell = row.getCell(9, Row.RETURN_BLANK_AS_NULL);
					if (cell != null) {
						// If location IS NULL, get program to recognize that and NOT do location search
						location = cell.getStringCellValue();
//						System.out.println(location);
						
						int index = location.length() - 1;
						
						// later addresses have numbers, so this statement ignores it
						if ( Character.isDigit(location.charAt(index)) ) { 
							while ( location.charAt(index) != ' ' )
								index--;
							index -= 2; // backtracks 2 spaces to be at the first letter of abbr.
						}
						else index--; // basic format for earlier addresses: address, city, state abbr.

						// state
						String state = location.substring(index, index + 2).toLowerCase();
						stateNum = places.getStateNum(state); // this is where Keywords is used to find the city and state
						
						// city
						String address = location.substring(0, index + 2).toLowerCase();
						city = places.findCity(address, state);
						
//						System.out.println(city + " " + state);
						
						// equals issue RESOLVED (array index limited by accident so later states weren't IN THE ARRAY!
//						if (state == "wi") System.out.print(Arrays.toString(state.getBytes()) + " ");
//						System.out.print(Arrays.toString("wi".getBytes()) + " ");
						
//						System.out.println(stateNum);
					}
					
					/*
					 * FIXME
					 * The really FAR addresses have numbers at the end instead of states,
					 * so I need a parsing function in Keywords to ignore the number and get the state
					 * abbreviation anyway. This will be good because I can then combine them into
					 * a single function for city and state
					 * 
					 * Keywords also needs a proper constructor that will construct the states, 
					 * names, and cities into their lists
					 */
					
					ProfileDr newProfile = new ProfileDr(firstName, middleName, lastName, title, specialty, city, stateNum);
					file.add(newProfile); // add to file
					
					if (freqOfName.containsKey(firstName + " " + lastName)) // add to map or increment count
						freqOfName.get(firstName + " " + lastName).increment();
					else freqOfName.put(firstName + " " + lastName, new IntHolder(1));
					
					// Considered if first name already in map, if last name already in map
					if (!namesLog.containsKey(firstName)) namesLog.put(firstName, new TreeMap<String,List<ProfileDr>>());
					if (!namesLog.get(firstName).containsKey(lastName)) 
							namesLog.get(firstName).put(lastName, new ArrayList<ProfileDr>());
					namesLog.get(firstName).get(lastName).add(newProfile); // 2D Map.
				} //rowIterator end
			}
			
			fis.close();
		} catch (IOException p) {
			p.printStackTrace();
			System.exit(-1);
		}
	}
	public void printTest() {
		System.out.println("File size: " + Integer.toString(file.size()));
		System.out.println("Map size: " + Integer.toString(namesLog.size()));
		System.out.println("Testing now.");
		
		for (int i = 0; i < file.size(); i++) {
			String fn = file.get(i).getFirstName().toLowerCase();
			String ln = file.get(i).getLastName().toLowerCase();
			
			if (!namesLog.containsKey(fn)) {
				System.err.println("MISSING " + fn);
				System.exit(-1);
			} else if (!namesLog.get(fn).containsKey(ln)) {
				System.err.println("MISSING " + ln);
				System.exit(-1);
			}
		}
	}
}
