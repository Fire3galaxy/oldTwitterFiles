package keywords;

import graph_programs.Followers;

import java.util.ArrayList;

public class StringParse {
	public static void main(String[] args) {
		Followers guid = new Followers();
	}
	
	public static ArrayList<String> parseNames(String fullName) {
		ArrayList<String> result = new ArrayList<String>();
		String s = new String();
		
		for (int i = 0; i < fullName.length(); i++) {
			if (Character.isAlphabetic(fullName.charAt(i)) || fullName.charAt(i) == '-')
				s += fullName.charAt(i);
			else if (!s.isEmpty()) {
				result.add(s);
				s = new String();
			}
		}
		
		if (!s.isEmpty()) result.add(s);	// Case: strings that end in letter.
		
		return result;
	}
}
