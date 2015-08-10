package backup;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import excel.ExcelMethods;
import excel.IntHolder;
import excel.ProfileDr;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.*;
import au.com.bytecode.opencsv.CSVWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TwitterSearch {
	// Class intended to work like C++ pair, with User + Likelihood of being right handle
	private class UserAndGrade {
		User user;
		// Grade is determined by weight of keywords found in description
		IntHolder grade;
		UserAndGrade(User u) {
			user = u;
			grade = new IntHolder(0);
		}
	}
	
	private class FinalHandlesPair {
		String name;
		List<UserAndGrade> fh; // holds users with 5+ points
		FinalHandlesPair(String n, List<UserAndGrade> f_h) {
			name = n;
			fh = f_h;
		}
	}
	private class JobCode {
		int KEYNUM;
		int SHORTNUM;
		int SPECNUM;
		JobCode(int key, int shrt, int spec) {
			KEYNUM = key;
			SHORTNUM = shrt;
			SPECNUM = spec;
		}
//		JobCode subCode(JobCode parent) {
//			if (parent == new JobCode(1, 2, 3)) {
//				return new JobCode(3, 2, 1);
//			}
//			
//			return new JobCode(-1, -1, -1);
//		}
	}
	
	public TwitterSearch() {
		allSearchResults = new LinkedList<LinkedList<ResponseList<User>>>();
		listOfFinalHandles = new LinkedList<FinalHandlesPair>();
		
		keywords1 = new LinkedList<ArrayList<String>>();
		shortKeywords1 = new LinkedList<ArrayList<String>>();
		specialKeywords1 = new LinkedList<ArrayList<String>>();
		
		keywordValues1 = new LinkedList<ArrayList<Integer>>();
		shortKeywordValues1 = new LinkedList<ArrayList<Integer>>();
		specialKeywordValues1 = new LinkedList<ArrayList<Integer>>();
		
		int NULL = 0;
		int MD = 1;
		int NURSE = 2;
		
		String[] blank = {}; // JobCode: 0, 0, 0 is blank
		int[] blankVal = {};
		insertKeywordsAndValues("keywords1", blank, blankVal, NULL);
		insertKeywordsAndValues("shortKeywords1", blank, blankVal, NULL);
		insertKeywordsAndValues("specialKeywords1", blank, blankVal, NULL);
		
		// list of arrays
		// MD JobCode: 1, 1, 1
		final String[] MDKeywords = {"doctor", "dr.", "medication", "physician"};
		final String[] MDShortKeywords = {"dr"};
		final String[] MDSpecial = {"MD"};
		
		final int[] MDKeyValues = {2, 3, 1, 3};
		final int[] MDShortKeyValues = {1};
		final int[] MDSpecialValues = {4};
		
		// Nurse JobCode: 2, 0, 0
		final String[] NurseKeywords = {"nurse", "nursing"};
		final int[] NurseKeyValues = {5, 4};
		
		// Adding MD Keywords and Values
		insertKeywordsAndValues("keywords1", MDKeywords, MDKeyValues, MD);
		// Adding MD Short Keywords/Val
		insertKeywordsAndValues("shortKeywords1", MDShortKeywords, MDShortKeyValues, MD);
		// Adding MD Special Keywords
		insertKeywordsAndValues("specialKeywords1", MDSpecial, MDSpecialValues, MD);
		
		// Nurse Keywords
		insertKeywordsAndValues("keywords1", NurseKeywords, NurseKeyValues, NURSE);
		
		states.put("AL", "alabama");
		states.put("AK", "alaska");
		states.put("AZ", "arizona");
	}
	void insertKeywordsAndValues(String whichOne, final String[] keywords, final int[] values, int code) {
		if (whichOne == "keywords1") {
			keywords1.add(new ArrayList<String>());
			keywordValues1.add(new ArrayList<Integer>());
			
			for (int i = 0; i < keywords.length; i++) {
				keywords1.get(code).add(keywords[i]);
				keywordValues1.get(code).add(values[i]);
			}
		}
		else if (whichOne == "shortKeywords1") {
			shortKeywords1.add(new ArrayList<String>());
			shortKeywordValues1.add(new ArrayList<Integer>());
			
			for (int i = 0; i < keywords.length; i++) {
				shortKeywords1.get(code).add(keywords[i]);
				shortKeywordValues1.get(code).add(values[i]);
			}
		}
		else if (whichOne == "specialKeywords1") {
			specialKeywords1.add(new ArrayList<String>());
			specialKeywordValues1.add(new ArrayList<Integer>());
			
			for (int i = 0; i < keywords.length; i++) {
				specialKeywords1.get(code).add(keywords[i]);
				specialKeywordValues1.get(code).add(values[i]);
			}
		}
		
		return;
	}
	public void printKeywordLists() {
		for (ArrayList<String> keywordList : keywords1) {
			for (String s : keywordList) {
				System.out.print(s + " ");
			}
			
			System.out.print('\n');
		}
		
		for (ArrayList<String> shortKeywordList : shortKeywords1) {
			for (String s : shortKeywordList) {
				System.out.print(s + " ");
			}
			
			System.out.print('\n');
		}
		
		for (ArrayList<String> specialKeywordList : specialKeywords1) {
			for (String s : specialKeywordList) {
				System.out.print(s + " ");
			}
			
			System.out.print('\n');
		}
		
		for (ArrayList<Integer> keywordValueList : keywordValues1) {
			for (Integer s : keywordValueList) {
				System.out.print(s + " ");
			}
			
			System.out.print('\n');
		}
		
		for (ArrayList<Integer> shortKeywordValueList : shortKeywordValues1) {
			for (Integer s : shortKeywordValueList) {
				System.out.print(s + " ");
			}
			
			System.out.print('\n');
		}
		
		for (ArrayList<Integer> specialKeywordValueList : specialKeywordValues1) {
			for (Integer s : specialKeywordValueList) {
				System.out.print(s + " ");
			}
			
			System.out.print('\n');
		}
		
		JobCode jobCode = selectKeywordList("MD", "pizza");
		JobCode jobCode2 = selectKeywordList("pie", "nurse");
		JobCode jobCode3 = selectKeywordList("", "");
		System.out.println(jobCode.KEYNUM + " " + jobCode.SHORTNUM + " " + jobCode.SPECNUM);
		System.out.println(jobCode2.KEYNUM + " " + jobCode2.SHORTNUM + " " + jobCode2.SPECNUM);
		System.out.println(jobCode3.KEYNUM + " " + jobCode3.SHORTNUM + " " + jobCode3.SPECNUM);
	}
	
	public void searchForDoctors(ExcelMethods excelList) {
		// Rate limits dictate that the max query count is 180 per 15 minutes for users\search
		//     or 350 per hour for my oAuth key
		
		final int START = 0, MAX = 50;
		for (int i = START; i < excelList.getFile().size() && i < MAX; i++) {
			// Sets number of pages to search for doctor in results
			int nameCount = excelList.getFreqOfName().get(excelList.getFile().get(i).getFirstName() 
				+ " " + excelList.getFile().get(i).getLastName()).getNum();
			int pageLimit = 3;
			if (nameCount > 2) {
				pageLimit = 10;
			}
			
			search(excelList.getFile().get(i), pageLimit);
			System.out.print(i);
		}
		
		System.out.println();
		
		// Filter results of search results.
		int i = START;
		for (LinkedList<ResponseList<User>> searchResultsForDoctor : allSearchResults) {
			filterResults(searchResultsForDoctor, excelList.getFile().get(i));
			i++;
			
			System.out.print(i);
		}
		
//		publishTopHandles();
	}
	
	// DATA FIELDS!!!!!!
	// List of keywords and associated weights can be found in separate document
	private static String[] keywords = {"m.d.", "dr.", "doctor", "nurse", "lcsw", "dpt", "crna", "aprn", "arnp", 
			"cna", "o.d.", "optometrist", "d.d.s", "dentist", "chiropractic"};
	private static int[] keywordValues = {3, 2, 3, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
	
	// These keywords often found in other words: boRN, etc. So uses diff function: gradeShortKeyword()
	private static String[] shortKeywords = {"md", "dr", "rn", "od", "dds"};
	private static int[] shortKeywordValues = {2, 1, 2, 1, 3};
	
	private static List<ArrayList<String>> keywords1;
	private static List<ArrayList<String>> shortKeywords1;
	private static List<ArrayList<String>> specialKeywords1;
	
	private static List<ArrayList<Integer>> keywordValues1;
	private static List<ArrayList<Integer>> shortKeywordValues1;
	private static List<ArrayList<Integer>> specialKeywordValues1;
	
	private static Map<String, String> states = new HashMap<String, String>();
	
	JobCode selectKeywordList(String title, String specialty) {
		// include title and specialty
		
		if (title == "MD") {
			return new JobCode(1, 1, 1);
//			if (specialty.contains("Obstetrics") || specialty.contains("Gynecology")) {
//				return new JobCode(1, 1, 1);
//			}
		}
		
		if (specialty.equalsIgnoreCase("nurse")) {
			return new JobCode(2, 0, 0);
		}
		
		return new JobCode(0, 0, 0); // NULL JobCode
	}
	
	private List<LinkedList<ResponseList<User>>> allSearchResults;
	private List<FinalHandlesPair> listOfFinalHandles;
	
	public void search(ProfileDr query, int pageLimit) { 
		try {
			Twitter twitter = new TwitterFactory().getInstance(); // Twitter4j Setup OAuth
			
			twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
					"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
			AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
					"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
			twitter.setOAuthAccessToken(accessToken);
			
			/* Current name being searched
			 * Tried "Jungmi L" instead of "Jungmi Lee", but results proved unreliable
			 */
			String firstName = query.getFirstName();
			String lastName = query.getLastName();
			
			// stores results of user search (3 pages)
			LinkedList<ResponseList<User> > searchResults = new LinkedList<ResponseList<User>>();
			
			for (int page = 1; page <= pageLimit; page++) {
				ResponseList<User> users = twitter.searchUsers(firstName + " " + lastName, page);
				searchResults.add(users);
				
				if (users.size() < 20) page = pageLimit + 1; // search results were less than pageLimit's setting
			}
			
			allSearchResults.add(searchResults);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.exit(-1);
		}
	}
	void filterResults(List<ResponseList<User>> searchResults, ProfileDr currentDr) {
		Twitter twitter = new TwitterFactory().getInstance(); // Twitter4j Setup OAuth
		
		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
		AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
		twitter.setOAuthAccessToken(accessToken);
		
		String firstName = currentDr.getFirstName(), 
				lastName = currentDr.getLastName(), 
				middleName = currentDr.getMiddleName();
		// final list of possibilities
		/* the handle, or multiple handles I need to output to the 
		 * 		xlsx file
		 */
		List<UserAndGrade> finalHandles = new ArrayList<UserAndGrade>();
		JobCode jobCode = selectKeywordList(currentDr.getTitle(), currentDr.getSpecialty());
		
		if (currentDr.getTitle() == "MD") System.out.println(currentDr.getFirstName() + " " + currentDr.getLastName());
		
		for (ResponseList<User> currentPage : searchResults) {
			// stores matched names (Grades set at 0, changes in keywords check)
			/* Going for exact first and last name, allowing for reverse order
			 *    e.g. "Lee, Jungmi" will still count
			 */
			List<UserAndGrade> usersWithMatchedNames = new ArrayList<UserAndGrade>();
			// stores users with at least one keyword in description 
			/* this seems to cut down the list pretty quickly (Sean Lynch),
			 * 		but I wanted to add weights to each keyword. This may be
			 * 		ultimately unnecessary though, but I'll keep it in mind
			 */
			List<UserAndGrade> HasKeywordList = new ArrayList<UserAndGrade>();
			
//			System.out.println("-----------------------------------------"); // FIXME: Delete
			
			// Iterates through all results of searchUsers() to put in MATCHEDNAMES
			for (User user : currentPage) {
//				System.out.println("@" + user.getScreenName() + " " + user.getName() + " " 
//					+  user.getDescription());
				
				// If name is in user's name (despite order), add
				if (containsName(user.getName(), firstName, lastName)) {
					usersWithMatchedNames.add(new UserAndGrade(user));
//					System.out.println("@" + user.getScreenName() + " " + user.getName() + " " 
//							+  user.getDescription());
				}
			}
			
//			System.out.println("-----------------------------------------"); // FIXME: Delete
			
			// to put into KEYWORDSLIST
			for (UserAndGrade uag : usersWithMatchedNames) {
//				System.out.println('@' + uag.user.getScreenName() + " " + uag.user.getName());
				
				// If user in array has keyword in screen name, name, or description, add to 2nd array
				if ( containsKeyword(uag.user.getScreenName(), uag.user.getName(), middleName, 
						uag.user.getDescription(), uag.grade, jobCode) ) {
				
					HasKeywordList.add(uag);
//					System.out.println('@' + uag.user.getScreenName() + " " + uag.user.getName());
				}
			}
			
//			System.out.println("-----------------------------------------"); // FIXME: Delete
			
			Paging pageOne = new Paging(1, 200); // For timeline. (1st page, 200 tweets) 
			
			try {
				// To add additional points to users in KEYWORDSLIST
				for (UserAndGrade uag : HasKeywordList) {
					// Add points for keywords in 200 most recent tweets
					if (!uag.user.isProtected()) {
						ResponseList<Status> timeline = 
								twitter.getUserTimeline(uag.user.getScreenName(), pageOne);
						
						for (Status tweet : timeline) {
							gradeText(tweet.getText(), uag.grade, jobCode);
						}
					}
				}
			}
			catch (TwitterException te) {
				te.printStackTrace();
				System.exit(-1);
			}
			// to put into FINALHANDLES
			for (UserAndGrade uag : HasKeywordList) {
//				System.out.println('@' + uag.user.getScreenName() + " - " + uag.user.getDescription() + " : " + uag.grade.getNum());
				if (uag.grade.getNum() > 4) finalHandles.add(uag); // filter
			}
		}
		
		// to put into LISTOFFINALHANDLES for writing to csv later
		listOfFinalHandles.add(new FinalHandlesPair(firstName + " " + lastName, finalHandles));
	}
	boolean containsName(String user, String fn, String ln) {
		String u = user.toLowerCase();

	    String[] searchName = {fn, ln};
	    int[] indicesOfNames = {u.indexOf(searchName[0]), u.indexOf(searchName[1])};
	    
	    for (int i = 0; i < 2;) {
	    	// if either first or last name is null, skips
	    	if (searchName[i].length() == 0) i++;
	    	else if (indicesOfNames[i] != -1) {
		    	// Check 1 char before first letter for space(non-letter)/front
		    	if (indicesOfNames[i] == 0 || !Character.isLetter(u.charAt(indicesOfNames[i] - 1))) {
		    		// Check 1 char after last letter for space/end
		    		if (!(indicesOfNames[i] + searchName[i].length() < u.length()) || !Character.isLetter(u.charAt(indicesOfNames[i] + searchName[i].length()))) {
//		    			System.out.println(indicesOfNames[i] + " good");
		    		    i++;
		    		}
		    		else indicesOfNames[i] = u.indexOf(searchName[i], indicesOfNames[i] + 1);
		    	}
		    	  
		    	else indicesOfNames[i] = u.indexOf(searchName[i], indicesOfNames[i] + 1);
		    }
		    else {
//		    	System.out.println(i + " Failed");
		    	return false;
		    }
	    }
	    return true;
	}
	private boolean containsKeyword(String handle, String name, String middleName, String description, IntHolder grade, JobCode jobCode) {
		String h = handle.toLowerCase(), n = name.toLowerCase(), d = description.toLowerCase();
		
		// check handles for keywords like drRick
        // gradeHandle(h, grade); // may not be good indicator
		
		gradeName(n, middleName, grade, jobCode); // checks names for keywords and middle name
		gradeText(d, grade, jobCode); // checks description for keywords
		
//		System.out.println("Grade: " + grade.getNum()); // FIXME: delete
		
		if (grade.getNum() > 0) return true;
		
		return false;
	}
	// used in both containsKeyword() and keywordGrading(). Meant to find EXACT word in
	// list, not words containing it. e.g. " rn " vs. "born" 
	private void gradeShortKeyword(String s, IntHolder grade, JobCode jobCode) {
		// for loop is split into 3 scenarios: 
		// word is at beginning ("rn "), word is in middle (" rn "), word is at end (" rn") 
		for (int i = 0; i < shortKeywords.length; i++) {
			// if statement avoids out-of-bounds
			if (s.length() > shortKeywords[i].length()) {
				int index = s.indexOf(shortKeywords[i]);
				while (index != -1) {
					// if the word is at the beginning OR if the character before the keyword is NOT a letter
					if (index == 0 || !Character.isLetter(s.charAt(index - 1))) {
						// if the one-past-last char (of the keyword) does not exist OR is not a letter  
						if (!(index + shortKeywords[i].length() < s.length()) || !Character.isLetter(s.charAt(index + shortKeywords[i].length())) ) {
							grade.addNum(shortKeywordValues[i]);
						}
					}
					
					index = s.indexOf(shortKeywords[i], index + 1);
				}
			}
		}
	}
	// add points for middle name, then searches for keywords
	private void gradeName(String name, String middleName, IntHolder grade, JobCode jobCode) {
		if (middleName != "" && name.length() > middleName.length()) {
			int index = name.indexOf(middleName);
			while (index != -1) {
				// if the word is at the beginning OR if the character before the keyword is NOT a letter
				if (index == 0 || !Character.isLetter(name.charAt(index - 1))) {
					// if the one-past-last char (of the keyword) does not exist OR is not a letter  
					if (!(index + middleName.length() < name.length()) || 
							!Character.isLetter(name.charAt(index + middleName.length())) ) {
						grade.addNum(5);
						index = -1;
					}
					else index = name.indexOf(middleName, index + 1);
				}
				else index = name.indexOf(middleName, index + 1);
			}
		}
		
		gradeText(name, grade, jobCode);
	}
	private void gradeHandle(String s, IntHolder grade) {
		String handle = s.toLowerCase();
		
		for (int i = 0; i < keywords.length; i++) {
			if (handle.contains(keywords[i])) {
				grade.addNum(keywordValues[i]);
//				System.out.println(handle + " Keyword: " + keywords[i]); // FIXME: Delete				
			}
		}
		
		for (int i = 0; i < shortKeywords.length; i++) {
			if (handle.contains(shortKeywords[i])) {
				grade.addNum(shortKeywordValues[i]);
//				System.out.println("Handle contains " + shortKeywords[i]);
			}
		}
		
		return;
	}
	private void gradeText(String s1, IntHolder grade, JobCode jobCode) {
//		String s2 = s1.toLowerCase();
		
		for (int i = 0; i < keywords.length; i++) {
			if (s1.contains(keywords[i])) {
				grade.addNum(keywordValues[i]);
//				System.out.println(s2 + " Keyword: " + keywords[i]); // FIXME: Delete				
			}
		}
		
		gradeShortKeyword(s1, grade, jobCode);
		
		return;
	}
	public void publishTopHandles() {
		// for top 3 loop, look in CountLives.java
		
		// publish to csv file here
		try {
			FileInputStream fis = new FileInputStream("doctors (1).xlsx");
			Workbook workbook = new XSSFWorkbook(fis);
			CSVWriter writer = new CSVWriter(new FileWriter("doctors(1).csv"));
			
			String provider, firstName, middleName, lastName, gender, title, school; 
			String gradYear, specialties, address1, address2, address3, address4; 
			String hospital1, twitterHandle, grade;
			DecimalFormat toDouble = new DecimalFormat("###########");
			
			Sheet sheet = workbook.getSheetAt(0);
			Row row = sheet.getRow(0);
			
			Cell cell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
			provider = cell.getStringCellValue();
			
			cell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
			firstName = cell.getStringCellValue(); 
			
			cell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
			middleName = cell.getStringCellValue();
			
			cell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
			lastName = cell.getStringCellValue();
			
			cell = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
			gender = cell.getStringCellValue();
			
			cell = row.getCell(5, Row.RETURN_BLANK_AS_NULL);
			title = cell.getStringCellValue();
			
			cell = row.getCell(6, Row.RETURN_BLANK_AS_NULL);
			school = cell.getStringCellValue();
			
			cell = row.getCell(7, Row.RETURN_BLANK_AS_NULL);
			gradYear = cell.getStringCellValue();
			
			cell = row.getCell(8, Row.RETURN_BLANK_AS_NULL);
			specialties = cell.getStringCellValue();
			
			cell = row.getCell(9, Row.RETURN_BLANK_AS_NULL);
			address1 = cell.getStringCellValue();
			
			cell = row.getCell(10, Row.RETURN_BLANK_AS_NULL);
			address2 = cell.getStringCellValue();
			
			cell = row.getCell(11, Row.RETURN_BLANK_AS_NULL);
			address3 = cell.getStringCellValue();
			
			cell = row.getCell(12, Row.RETURN_BLANK_AS_NULL);
			address4 = cell.getStringCellValue();
			
			cell = row.getCell(13, Row.RETURN_BLANK_AS_NULL);
			hospital1 = cell.getStringCellValue();
			
			cell = row.getCell(14, Row.RETURN_BLANK_AS_NULL);
			twitterHandle = cell.getStringCellValue();
			
			grade = "Grade";
			
			String[] line = new String[] {provider, firstName, middleName, lastName, gender, 
					title, school, gradYear, specialties, address1, address2, address3, address4, 
					hospital1, twitterHandle, grade};
			writer.writeNext(line);
			
			for (int i = 1; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				
				cell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
				if (cell != null)
					provider = toDouble.format(cell.getNumericCellValue());
				else provider = "";
				
				cell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) firstName = cell.getStringCellValue(); 
				else firstName = "";
				
				cell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) middleName = cell.getStringCellValue();
				else middleName = "";
				
				cell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) lastName = cell.getStringCellValue();
				else lastName = "";
				
				cell = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) gender = cell.getStringCellValue();
				else gender = "";
				
				cell = row.getCell(5, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) title = cell.getStringCellValue();
				else title = "";
				
				cell = row.getCell(6, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) school = cell.getStringCellValue();
				else school = "";
				
				cell = row.getCell(7, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) gradYear = cell.getNumericCellValue() + "";
				else gradYear = "";
				
				cell = row.getCell(8, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) specialties = cell.getStringCellValue();
				else specialties = "";
				
				cell = row.getCell(9, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) address1 = cell.getStringCellValue();
				else address1 = "";
				
				cell = row.getCell(10, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) address2 = cell.getStringCellValue();
				else address2 = "";
				
				cell = row.getCell(11, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) address3 = cell.getStringCellValue();
				else address3 = "";
				
				cell = row.getCell(12, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) address4 = cell.getStringCellValue();
				else address4 = "";
				
				cell = row.getCell(13, Row.RETURN_BLANK_AS_NULL);
				if (cell != null) hospital1 = cell.getStringCellValue();
				else hospital1 = "";
				
				// gets top twitter handle from listOfFinalHandles
				int index = -1;
				int gradeNum = 0;
				
				if (i < listOfFinalHandles.size()) {
					FinalHandlesPair fhp = listOfFinalHandles.get(i - 1);
					for (int p = 0; p < fhp.fh.size(); p++) {
						if (fhp.fh.get(p).grade.getNum() > gradeNum) {
							index = p;
							gradeNum = fhp.fh.get(p).grade.getNum();
						}
					}
					
					if (index == -1) {
						twitterHandle = "";
						grade = "";
					}
					else {
						twitterHandle = "@" + fhp.fh.get(index).user.getScreenName();
						grade = gradeNum + "";
					}
				}
				else {
					twitterHandle = "";
					grade = "";
				}
				
				line = new String[] {provider, firstName, middleName, lastName, 
						gender, title, school, gradYear, specialties, address1, 
						address2, address3, address4, hospital1, twitterHandle, 
						grade};
				writer.writeNext(line);
			}
			
			writer.close();
			fis.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}