package backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import twitter.FilterResults;
import au.com.bytecode.opencsv.CSVReader;
import keywords.Names;
import keywords.Places;
import keywords.Specialties;

class input {
	ArrayList<String> statesAbbr;
	ArrayList<String> states;
	
	input() {
		statesAbbr = new ArrayList<String>(50);
		states = new ArrayList<String>(50);
		
		inputStates("list of states.txt");
	}
	void inputStates(final String filename) {
		FileInputStream ip = null;
		
		try {
			File file = new File(filename);
			
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error: could not open file");
				System.exit(-1);
			}
			
			int i = 0;
			char c = 0;
			
//			for (; i < 905; i++) {
//				if ( Character.isAlphabetic((c = (char) ip.read())) ) System.out.print(c);
//				else if (c == '\n') System.out.println();
//				else if (c == '\t') System.out.print(" tab ");
//				else if (c == ' ') System.out.print("-");
//				else System.out.print('9');
//			}
			
			while (ip.available() != 0 && i < 59) {
				String state = new String(), stateAbbr = new String();
				String line = new String();
				
				while ( (c = (char) ip.read()) != '\n') line += c;
				
				state = line.substring(0, line.length() - 5).toLowerCase();
				stateAbbr = line.substring(line.length() - 3, line.length() - 1).toLowerCase();
				
//				while (Character.isAlphabetic( (c = (char) ip.read()) )) state += Character.toLowerCase(c);
//				ip.skip(1); // tab
//				for (int f = 0; f < 2; f++) stateAbbr += Character.toLowerCase( (c = (char) ip.read()) );
//				ip.skip(2);
				
				states.add(state);
				statesAbbr.add(stateAbbr);
//				System.out.println(state + " " + stateAbbr);
				
				i++;
			}
			
			ip.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		} 
	}
	void printStates() {
		for (int i = 0; i < states.size(); i++) {
			System.out.println(states.get(i) + " " + statesAbbr.get(i));
			System.out.println('\t' + Arrays.toString(states.get(i).getBytes()) + " " + Arrays.toString(statesAbbr.get(i).getBytes()));
		}
		
		System.out.println(states.size() + " " + statesAbbr.size());
		
		System.out.print(Arrays.toString("wi".getBytes()));
		
//		// If location IS NULL, get program to recognize that and NOT do location search
//		String location = "1000 N OAK AVE MARSHFIELD WI 544495703";
//		System.out.println(location);
//		
//		int place = location.length() - 1;
//		
//		// later addresses have numbers, so this statement ignores it
//		if ( Character.isDigit(location.charAt(place)) ) { 
//			while ( location.charAt(place) != ' ' )
//				place--;
//			place -= 2; // backtracks 2 spaces to be at the first letter of abbr.
//		}
//		else place--; // basic format for earlier addresses: address, city, state abbr.
//			// 2 places from the end
//		
//		String state = location.substring(place, place + 2).toLowerCase();
//		System.out.println(state);
	}
	void testCSV() {
		try {
			Reader reader = new FileReader("Untitled spreadsheet - Sheet1.csv");
			CSVReader csvRead = new CSVReader(reader);
			
			for (int i = 0; i < 3; i++) {
				String[] line = csvRead.readNext();
				
				if (line != null) for (String p : line) System.out.print(p + ' ');
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	public static void main(String[] args) {
//		input testStates = new input();
//		testStates.printStates();
//		testStates.testCSV();
		
		Places key = new Places();
		key.printStates();
//		key.searchPlaces("smackover", "ar");
		
//		Specialties sp = new Specialties();
		
//		Names names = new Names();
//		for (String s : names.getNames("kit")) System.out.print(s + " ");
		
//		FilterResults fr = new FilterResults();
//		if (fr.containsName("monkeypal al handojo", "jeremiah", "handojo")) System.out.println("Nice");
	}
}