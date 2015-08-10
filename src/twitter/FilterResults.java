package twitter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import keywords.*;
import twitter.logAdmin;
import excel.ExcelMethods;
import excel.IntHolder;
import excel.ProfileDr;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.*;
import au.com.bytecode.opencsv.CSVWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FilterResults {
	// Class intended to work like C++ pair, with User + Likelihood of being right handle
	public class UserAndGrade {
		User user;
		// Grade is determined by weight of keywords found in description
		IntHolder grade;
		UserAndGrade(User u, int g) {
			user = u;
			grade = new IntHolder(g);
		}
	}
	public UserAndGrade getUag() throws TwitterException { // Tests with uag needed
		ConfigurationBuilder cb = new ConfigurationBuilder();
//			cb.setApplicationOnlyAuthEnabled(true);
		Twitter twitter = new TwitterFactory(cb.build()).getInstance(); // Twitter4j Setup OAuth
		
		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
		AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
		twitter.setOAuthAccessToken(accessToken);
		
//			if ("bearer".equals(twitter.getOAuth2Token().getTokenType())) System.out.println("True");
//			Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("search");
//			RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");
//			if (searchTweetsRateLimit != null) System.out.println("Not Null");
//			if (searchTweetsRateLimit.getLimit() == 450) System.out.println("limit == 450");
		
		ResponseList<User> test = twitter.searchUsers("Daniel H", 0);
		System.out.println(test.get(0).getScreenName() + " " + test.get(0).getLocation());
		return new UserAndGrade(test.get(0), 10);
	}
	private class FinalHandlesPair {
//		String name;
		List<UserAndGrade> fh; // holds users with 5+ points
		FinalHandlesPair(String n, List<UserAndGrade> f_h) {
//			name = n;
			fh = f_h;
		}
	}

	// The Grading Rubric
	final int NAMEGRADE; // 10 for N. 5 middle N.
	final int TITLEGRADE; // 20
	final int SPECIALTYGRADE; // 30 for Spec., 5 for Keywords in tweets (just 35 for now for spec)
	final int LOCATIONGRADE; // 13 for state 17 for city 25 for just city
	
	int accountNum;
	
	List<FinalHandlesPair> listOfFinalHandles; // list of list of uag
	Keywords allKeywords;
	Names names;
	Places places;
	Specialties specialties;
	List<String[]> accessTokens;
	logAdmin adminlog;
	
	public FilterResults() {
		listOfFinalHandles = new LinkedList<FinalHandlesPair>();
		allKeywords = new Keywords();
		names = new Names();
		places = new Places();
		specialties = new Specialties();
		accessTokens = new ArrayList<String[]>();
		accountNum = 0;
		adminlog = new logAdmin();
		
		setAccessTokens();
		
		NAMEGRADE = 15;
		TITLEGRADE = 20;
		SPECIALTYGRADE = 35;
		LOCATIONGRADE = 30;
	}
	void setAccessTokens() { // Sets it here to keep keys secret
		String[] acc1 = {"53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
				"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH"}; 
		
		accessTokens.add(acc1);
	}
	public void scanAccount(User u, ProfileDr profile) {
		UserAndGrade uag = new UserAndGrade(u, 10); // Name already matched through nameslog: #/10
		
		checkLocation(uag, profile);	// Grades location: #/30
		containsKeyword(uag.user.getScreenName(), uag.user.getName(), profile, 
						uag.user.getDescription(), uag.grade); // Grades middle name, title, specialty: 5, 20, 30 (#/55)
		
		if (uag.grade.getNum() > 23) {
			profile.setHandle(uag.user.getScreenName(), uag.grade.getNum());
			System.out.println(uag.user.getScreenName() + " -> " + uag.user.getName() + ": " + uag.grade.getNum());
		}
	}
	public void filterResults(List<ResponseList<User>> searchResults, ProfileDr currentDr) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setHttpRetryCount(2);
		
		Twitter twitter = new TwitterFactory(cb.build()).getInstance(); // Twitter4j Setup OAuth: Retries twice if timeout
		
		twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
				"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
		
		OAuth2Token token = null;
		
		try {
			token = twitter.getOAuth2Token();
			if (!("bearer".equals(token.getTokenType()))) {
				System.out.println("At FilterResults: OAuthToken did not produce expected results");
				System.exit(-1);
			}
		} catch(TwitterException ie) {
			ie.printStackTrace();
			System.exit(-1);
		}
		
		String firstName = currentDr.getFirstName(), 
				lastName = currentDr.getLastName(); 
		// final list of possibilities
		/* the handle, or multiple handles I need to output to the 
		 * 		xlsx file
//		 */
		List<UserAndGrade> finalHandles = new ArrayList<UserAndGrade>();
//		JobCode jobCode = allKeywords.selectKeywordList(currentDr.getTitle(), currentDr.getSpecialty());
		
//		if (currentDr.getTitle() == "MD") System.out.println(currentDr.getFirstName() + " " + currentDr.getLastName());
		
		for (ResponseList<User> currentPage : searchResults) {
			adminlog.incrementSearched(currentPage.size()); // Logging how many names (total, not individ. names) checked
			
			// stores matched names (Grades set at 0, changes in keywords check)
			/* Going for exact first and last name, allowing for reverse order
			 *    e.g. "Lee, Jungmi" will still count
			 */
			List<UserAndGrade> usersWithMatchedNames = new ArrayList<UserAndGrade>();
			// Users with right name and location (or lack of location in account)
			List<UserAndGrade> usersInRightLocation = new ArrayList<UserAndGrade>();
			
//			System.out.println("-----------------------------------------"); // FIXME: Delete
			
			// Iterates through all results of searchUsers() to put in MATCHEDNAMES
			for (User user : currentPage) {
//				System.out.println("@" + user.getScreenName() + " " + user.getName() + " " 
//					+  user.getDescription());
				
				// If name is in user's name (despite order), add
				if (containsName(user.getName(), firstName, lastName)) {
					usersWithMatchedNames.add(new UserAndGrade(user, 10)); // 2/3 NAMEGRADE

//					System.out.println("@" + user.getScreenName() + " " + user.getName() + " " 
//							+  user.getDescription());
				}
			}
			
//			System.out.println("-----------------------------------------"); // FIXME: Delete
			
			// location searching FIXME Complete
			for (UserAndGrade uag : usersWithMatchedNames) {
				if (checkLocation(uag, currentDr)) usersInRightLocation.add(uag);
			}
			
			Paging pageOne = new Paging(1, 200); // For timeline. (1st page, 200 tweets)
			RateLimitStatus rateLimit = null; // What was this for...? I bet it was the 15 min limit...
			
			try {
				rateLimit = twitter.getRateLimitStatus("statuses").get("/statuses/user_timeline");
			} catch (TwitterException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			
			// to put into KEYWORDSLIST
			for (UserAndGrade uag : usersInRightLocation) {
				containsKeyword(uag.user.getScreenName(), uag.user.getName(), currentDr, 
						uag.user.getDescription(), uag.grade);
				
				if (!uag.user.isProtected()) {
					try {
						ResponseList<Status> timeline;
						
						// The Account Switch: If requests run out, this will switch credentials
//						if (rateLimit.getRemaining() == 0) {
//							if (accountNum < accessTokens.size()) {
//								AccessToken accessToken = new AccessToken(accessTokens.get(accountNum)[0], accessTokens.get(accountNum)[1]);
//								twitter.setOAuthAccessToken(accessToken);
//								rateLimit = twitter.getRateLimitStatus("statuses").get("/statuses/user_timeline");
//								
//								accountNum++;
//							}
//							else {
//								System.out.println("Out of Accounts");
//								System.exit(-1);
//							}
//						}
						
						timeline = twitter.getUserTimeline(uag.user.getScreenName(), pageOne);
					
						boolean done = false;
						
//						for (Status tweet : timeline) {
//							for (String key : specialties.getKeywords()) {
//								if (containsShortKeyword(tweet.getText(), key)) {
//									uag.grade.addNum(5); // SPECIALTYGRADE -> 5/35
//									done = true;
//									break;
//								}
//							}
//							if (done) break; // keyword found, finish loop
//						}
					} catch (TwitterException e) {
//						e.printStackTrace();
						//System.out.println("Timeline Error: " + uag.user.getScreenName() + " " + firstName + " " + lastName);
//						System.exit(-1);
					}
				}
//				HasKeywordList.add(uag);
			}
			
//			System.out.println("-----------------------------------------"); // FIXME: Delete
			
//			Paging pageOne = new Paging(1, 200); // For timeline. (1st page, 200 tweets) 
//			
//			// To add additional points to users in KEYWORDSLIST
//			for (UserAndGrade uag : HasKeywordList) {
//				
//				// The try/catch is intended to solve the timeout issue by retrying a query if a timeout occurs
//				try {
//					// Add points for keywords in 200 most recent tweets
//					if (!uag.user.isProtected()) {
//						ResponseList<Status> timeline = 
//								twitter.getUserTimeline(uag.user.getScreenName(), pageOne);
//						
//						for (Status tweet : timeline) {
//							gradeText(tweet.getText(), uag.grade, jobCode);
//						}
//					}
//				} catch (TwitterException te) {
//					if (te.getStatusCode() == -1)
//						if (tryAgain(uag, jobCode)) System.out.println("It worked!"); // FIXME Delete
//					else {
//						te.printStackTrace();
//						System.exit(-1);
//					}
//				}
//				
//			}
			
			// to put into FINALHANDLES
			for (UserAndGrade uag : usersInRightLocation) {
//				System.out.println('@' + uag.user.getScreenName() + " - " + uag.user.getDescription() + " : " + uag.grade.getNum());
				if (uag.grade.getNum() >= 23) finalHandles.add(uag); // filter
			}
		}
		
		// to put into LISTOFFINALHANDLES for writing to csv later
		listOfFinalHandles.add(new FinalHandlesPair(firstName + " " + lastName, finalHandles));
	}
	private boolean containsName(String user, String fn, String ln) {
		String u = user.toLowerCase();

	    String[] searchName = {fn, ln};
	    int[] indicesOfNames = {u.indexOf(searchName[0]), u.indexOf(searchName[1])};
	    
	    Set<String> similarNames = names.getNames(fn);
	    Iterator<String> it = null;
	    if (similarNames != null) it = similarNames.iterator();
	    
	    String currentName = searchName[0];
	    
	    for (int i = 0; i < 2;) {
	    	// if either first or last name is null, skips
	    	if (currentName.length() == 0) {
	    		i++;
	    		if (i < 2) currentName = searchName[i];
	    	}
	    	else if (indicesOfNames[i] != -1) {
		    	// Check 1 char before first letter for space(non-letter)/front
		    	if (indicesOfNames[i] == 0 || !Character.isLetter(u.charAt(indicesOfNames[i] - 1))) {
		    		// Check 1 char after last letter for space/end
		    		if (!(indicesOfNames[i] + currentName.length() < u.length()) || !Character.isLetter(u.charAt(indicesOfNames[i] + currentName.length()))) {
//		    			System.out.println(currentName);
		    		    i++;
		    		    if (i < 2) currentName = searchName[i];
		    		}
		    		else indicesOfNames[i] = u.indexOf(currentName, indicesOfNames[i] + 1);
		    	}
		    	  
		    	else indicesOfNames[i] = u.indexOf(currentName, indicesOfNames[i] + 1);
		    }
		    else {
		    	if (i == 0) {
		    		if (similarNames == null) return false;
		    		if (it == null) return false;
		    		
		    		if (it.hasNext()) {
		    			currentName = it.next();
		    			indicesOfNames[i] = u.indexOf(currentName);
		    			
//		    			System.out.println("\t" + currentName);
		    		}
		    		else return false;
		    	}
//		    	System.out.println(i + " Failed");
		    	else return false;
		    }
	    }
	    return true;
	}
	public boolean checkLocation(UserAndGrade uag, ProfileDr currentDr) {
		// FIXME: Delete all outputs when code is finished
		String loc = uag.user.getLocation().toLowerCase();
		int num = currentDr.getStateNum();
		int stateIndexFront = -1, stateIndexBack = -1; // If City search finds within state name, do not trigger false.
		boolean stateFound = false;
		
		if (currentDr.getCity().equals("") && currentDr.getStateNum() == -1) {
//			System.out.println(uag.user.getScreenName() + " Blank ProfileDr");
			
			return true; // excel read failed
		}
		else if (loc.equals("")) {
//			System.out.println(uag.user.getScreenName() + " Blank Location");
			
			return true; // No location included
		}
		
		ArrayList<String> states = places.stateList(), statesAbbr = places.statesAbbrList();
		
		// Search for State!
		if (containsShortKeyword(loc, statesAbbr.get(num)) || loc.contains(states.get(num))) { 
			System.out.println(uag.user.getScreenName() + " State Match");
			
			// Some exceptions will trip up city search's method: e.g. State matches that are ACTUALLY the city of another state
			// e.g. California, Kentucky
			if (places.exceptionList().containsKey(states.get(num)))
				for (String stAbbr : places.exceptionList().get(states.get(num)))
					if (containsShortKeyword(loc, stAbbr) || loc.contains(places.fullNameOfState(stAbbr))) return false;
			
			if (loc.contains(states.get(num))) {
				stateIndexFront = loc.indexOf(states.get(num)); // for ensuring false city search doesn't trigger from state
				stateIndexBack = stateIndexFront + states.get(num).length() - 1;
			}
			else {
				stateIndexFront = indexOfShort(loc, statesAbbr.get(num));
				stateIndexBack = stateIndexFront + statesAbbr.get(num).length() - 1;
			}
			
			uag.grade.addNum(13); // LOCATION GRADE 13/30
			stateFound = true;
		}
		else for (int i = 0; i < states.size(); i++) // If different state is found
			if (containsShortKeyword(loc, statesAbbr.get(i)) || loc.contains(states.get(i))) {
//				System.out.println("Here");
				
				if (loc.contains(states.get(i))) { 
					if (!states.get(i).equals(currentDr.getCity()) && 
							!containsShortKeyword(states.get(i), currentDr.getCity()) && 
							!containsShortKeyword(currentDr.getCity(), states.get(i))) 
						return false; // If "wrong state" is actually "right city", don't return!
				}
				else return false;
			}
		
		// Search for City!
		if (containsShortKeyword(loc, currentDr.getCity())) {
			if (stateFound) {
				uag.grade.addNum(17); // LOCATIONGRADE 17/30
				
//				System.out.println(uag.user.getScreenName() + " City Match: " + 17);
			}
			else {
				uag.grade.addNum(25); // LOCATIONGRADE 25/30 (State not found, but b/c city found, it's really likely)
				
//				System.out.println(uag.user.getScreenName() + " City Match: " + 25);
			}
		}
		
		else {
//			System.out.println("Wrong City");
			
			for (String sa : statesAbbr) {
				for (String city : places.cityList().get(sa)) {
					// Wrong city
					if (containsShortKeyword(loc, city)) {
						if (city.length() <= 5) {
							// System.out.println(city + " " + sa + " City Mismatch: " + -1);
							
							if (indexOfShort(loc, city) < stateIndexFront || indexOfShort(loc, city) > stateIndexBack) 
								if (containsShortKeyword(loc, sa) || loc.contains(places.fullNameOfState(sa))) return false;
						}
						else if (indexOfShort(loc, city) < stateIndexFront || indexOfShort(loc, city) > stateIndexBack) {
//							System.out.println(city + " " + sa + " City Mismatch: " + -1);
							
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
	private void containsKeyword(String handle, String name, ProfileDr currentDr, String description, IntHolder grade) {
		String h = handle.toLowerCase(), n = name.toLowerCase(), d = description.toLowerCase();
		
		// check handles for keywords like drRick
        // gradeHandle(h, grade); // may not be good indicator
		
		gradeName(n, currentDr.getMiddleName(), grade, 5); // checks names for keywords and middle name (NAMEGRADE (5/15))
		gradeTitle(description, currentDr.getTitle(), grade);
		gradeText(d, currentDr.getSpecialty(), grade, SPECIALTYGRADE); // checks description for keywords
		
		return;
	}
	// used in both containsKeyword() and keywordGrading(). Meant to find EXACT word in
	// list, not words containing it. e.g. " rn " vs. "born" 
	private boolean containsShortKeyword(String s, String q) {
		if (s.length() >= q.length()) {
			int index = s.indexOf(q);
			while (index != -1) {
				// if the word is at the beginning OR if the character before the keyword is NOT a letter
				if (index == 0 || !Character.isLetter(s.charAt(index - 1))) {
					// if the one-past-last char (of the keyword) does not exist OR is not a letter  
					if (!(index + q.length() < s.length()) || !Character.isLetter(s.charAt(index + q.length())) ) {
						return true;
					}
				}
				
				index = s.indexOf(q, index + 1);
			}
		}
		
		return false;
	}
	private int indexOfShort(String s, String q) {
		if (s.length() >= q.length()) {
			int index = s.indexOf(q);
			while (index != -1) {
				// if the word is at the beginning OR if the character before the keyword is NOT a letter
				if (index == 0 || !Character.isLetter(s.charAt(index - 1))) {
					// if the one-past-last char (of the keyword) does not exist OR is not a letter  
					if (!(index + q.length() < s.length()) || !Character.isLetter(s.charAt(index + q.length())) ) {
						return index;
					}
				}
				
				index = s.indexOf(q, index + 1);
			}
		}
		
		return -1;
	}
	// add points for middle name, then searches for keywords
	private void gradeName(String name, String middleName, IntHolder grade, final int GRADEVALUE) {
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
		
//		gradeText(name, grade, jobCode);
	}
	private void gradeTitle(String description, String title, IntHolder grade) {
		String t = title.toLowerCase();
		
		if (t != "" && description.contains(t)) grade.addNum(TITLEGRADE);
		else {
			Credentials c = new Credentials();
			String fullTitle = c.getTitle(t);
			
			if (fullTitle != null && description.toLowerCase().contains(fullTitle)) grade.addNum(TITLEGRADE);
		}
		
		return;
	}
	public boolean gradeText(String description, List<String> spec, IntHolder grade, final int GRADEVALUE) {
//		String s2 = s1.toLowerCase();
		
		for (int i = 0; i < spec.size(); i++) {
			Set<String> keywordSet = specialties.getSynonyms(spec.get(i));
			
			if (keywordSet != null) {
				for (String keyword : keywordSet) {
					if (description.contains(keyword)) {
						grade.addNum(GRADEVALUE);
						return true;
						
		//				System.out.println(s2 + " Keyword: " + keywords[i]); // FIXME: Delete				
					}
				}
			}
		}
		
//		gradeShortKeyword(s1, grade, jobCode);
		
		return false;
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
						adminlog.incrementFound();
						adminlog.writeHandleLog(firstName + " " + middleName + " " + lastName, twitterHandle);
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
			
			adminlog.writeNumLog();
			
			writer.close();
			fis.close();
			adminlog.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void publishTopHandles2(ExcelMethods exl) {
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
				
				// gets top twitter handle from profileDr
				
				
				if (exl.getNamesLog().get(firstName.toLowerCase()).get(lastName.toLowerCase()).isEmpty()) { 
					twitterHandle = "";
					grade = "";
				}
				else {
					// ProfileDrs added to arraylist of profiledr in namesLog by order of occurrence, so 
					// assuming first element is safe for rewrite
					twitterHandle = exl.getNamesLog().get(firstName.toLowerCase()).get(lastName.toLowerCase()).get(0).getHandle();
					grade = exl.getNamesLog().get(firstName.toLowerCase()).get(lastName.toLowerCase()).get(0).getGrade() + "";
					exl.getNamesLog().get(firstName.toLowerCase()).get(lastName.toLowerCase()).remove(0);
				}
				
				
				line = new String[] {provider, firstName, middleName, lastName, 
						gender, title, school, gradYear, specialties, address1, 
						address2, address3, address4, hospital1, twitterHandle, 
						grade};
				writer.writeNext(line);
			}
			
			adminlog.writeNumLog();
			
			writer.close();
			fis.close();
			adminlog.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}