package keywords;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVReader;

public class Places {
	Map<String, Set<String>> citiesWStNms; // With State Names (EXCEPTION)
	
	ArrayList<String> statesAbbr;
	ArrayList<String> states;
	
	Map<String, Set<String>> cities;
	Map<String, Set<String>> alternateCityNames; // Acceptable cities
	
	public Places() {
		citiesWStNms = new HashMap<String, Set<String>>(); // An EXCEPTION list
		alternateCityNames = new HashMap<String,Set<String>>(); // A Synonym list
		statesAbbr = new ArrayList<String>();
		states = new ArrayList<String>();
		cities = new HashMap<String, Set<String>>();
		
		inputStates("list of states.txt");
		setupCityStates();
		inputCities("zip_code_database.csv");
//		inputCities("Untitled spreadsheet - Sheet1.csv");
	}
	// receives filename from constructor to load states and abbreviations
	void inputStates(final String filename) {
		FileInputStream ip = null;
		
		try {
			File file = new File(filename);
			
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error-places: could not open file");
				System.exit(-1);
			}
			
//			int i = 0;
			char c = 0;
			
			while (ip.available() != 0) {
				String state = new String(), stateAbbr = new String();
				String line = new String();
				
				while ( (c = (char) ip.read()) != '\n') line += c;
				
				state = line.substring(0, line.length() - 5).toLowerCase();
				stateAbbr = line.substring(line.length() - 3, line.length() - 1).toLowerCase();
				
				states.add(state);
				statesAbbr.add(stateAbbr);
				
//				i++;
			}
			
			ip.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	void inputCities(final String filename) {
		try {
			Reader reader = new FileReader(filename);
			
			CSVReader csvreader = new CSVReader(reader);
			
			csvreader.readNext();
			String[] line = csvreader.readNext(); // first line is ignored
			
			while (line != null) {
				cities.get( line[5].toLowerCase() ).add( line[2].toLowerCase() );
				
				for (String state : states) // Some cities have state names, which will need to be recognized for location grading
					if (state.equals(line[2].toLowerCase())) {
						if (!citiesWStNms.containsKey(line[2].toLowerCase())) citiesWStNms.put(line[2].toLowerCase(), new TreeSet<String>());
						citiesWStNms.get(line[2].toLowerCase()).add(line[5].toLowerCase());
					}
//				for (String s : line) System.out.print(Arrays.toString(s.getBytes()) + " | ");
				
				// Other "Acceptable Cities" for a particular zip code
				// 	some are actually other cities in the region while other...
				// 	are simply synonyms for those cities (this is because of data from usps)
				if (!line[3].equals("")) {
					int start = 0, end = 0;
					
					// Acceptable cities in the csv typically represent nearby cities or synonyms of the city. Either way, this ensures that doctors in the vicinity
					// are still recorded.
					if (!alternateCityNames.containsKey(line[2].toLowerCase() + "," + line[5].toLowerCase()))
						alternateCityNames.put(line[2].toLowerCase() + "," + line[5].toLowerCase(), new TreeSet<String>());
					
					while (start < line[3].length()) {
						for (; end < line[3].length() && line[3].charAt(end) != ','; end++);
						String city = line[3].substring(start, end);
						start = end + 2;
						end = start;
						
						cities.get( line[5].toLowerCase() ).add( city.toLowerCase() );
						
						for (String state : states) // Some cities have state names, which will need to be recognized for location grading
							if (state.equals(city.toLowerCase())) {
								if (!citiesWStNms.containsKey(city.toLowerCase())) citiesWStNms.put(city.toLowerCase(), new TreeSet<String>());
								citiesWStNms.get(city.toLowerCase()).add(line[5].toLowerCase());
							}
						
						alternateCityNames.get(line[2].toLowerCase() + "," + line[5].toLowerCase()).add(city.toLowerCase());
					}
				}
				
				line = csvreader.readNext();
			}
			
			csvreader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch(IOException ie) {
			ie.printStackTrace();
			System.exit(-1);
		}
		
//		printCities("ca");
	}
	void setupCityStates() {
		for (String abbr : statesAbbr) cities.put(abbr, new TreeSet<String>());
	}
	void printCities(String stateAbbr) {
		for (String s : cities.get(stateAbbr)) System.out.println(s);
	}
	
	public int getStateNum(String stateAbbr) {
		return statesAbbr.indexOf(stateAbbr);
	}
	public String getState(int stateNum) {
		return states.get(stateNum);
	}
	public String getStateAbbr(int stateNum) {
		return states.get(stateNum);
	}
	public String fullNameOfState(String stateAbbr) {
		int i = statesAbbr.indexOf(stateAbbr);
		if (i != -1) return states.get(i);
		else return null;
	}
	boolean searchPlaces(String city, String stateAbbr) {
		if (cities.containsKey(stateAbbr))
			if (cities.get(stateAbbr).contains(city)) return true;
		return false; 
	}
	// Finds exact city match from state or returns null
	public String findCity(String address, String stateAbbr) {
		int lastLetter = address.length() - 4; // the last letter of the first word before state abbr. 
		String query1 = "", query2 = "", query3 = ""; // query has 1 word, 2 words, then 3 words total. longest matching wins
		int i = lastLetter;
		
		for (i = lastLetter; i - 1 >= 0 && address.charAt(i - 1) != ' '; i--);
		query1 = address.substring(i, lastLetter + 1);
		
		i--; // 1 before query1
		for (; i - 1 >= 0 && address.charAt(i - 1) != ' '; i--);
		query2 = address.substring(i, lastLetter + 1);
		
		i--;
		for (; i - 1 >= 0 && address.charAt(i - 1) != ' '; i--);
		query3 = address.substring(i, lastLetter + 1);
		
		if (searchPlaces(query3, stateAbbr)) return query3;
		if (searchPlaces(query2, stateAbbr)) return query2;
		if (searchPlaces(query1, stateAbbr)) return query1;
		
		return ""; // Make sure excelMethods has condition to ignore city parameter if string is null
	}

	public ArrayList<String> stateList() {
		return states;
	}
	public ArrayList<String> statesAbbrList() {
		return statesAbbr;
	}
	public Map<String, Set<String>> cityList() {
		return cities;
	}
	public Map<String, Set<String>> exceptionList() {
		return citiesWStNms;
	}
	public void printStates() {
//		for (int i = 0; i < states.size(); i++) {
//			System.out.println(states.get(i) + " " + statesAbbr.get(i));
//		}
		
//		for (String city : states) 
//			if (citiesWStNms.containsKey(city)) {
//				System.out.print(city + ":");
//				for (String statesAbbr : citiesWStNms.get(city)) System.out.print(statesAbbr + " ");
//				System.out.println();
//			}
		
		for (String stateAbbr : statesAbbr)
			for (String city : cities.get(stateAbbr))
				if (alternateCityNames.containsKey(city + "," + stateAbbr)) {
					System.out.print(city + "," + stateAbbr + ": ");
					for (String altCity : alternateCityNames.get(city + "," + stateAbbr)) System.out.print(altCity + " | ");
					System.out.println();
				}
	}
}