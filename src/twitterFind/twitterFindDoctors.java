package twitterFind;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import keywords.StringParse;
import excel.*;
import graph_programs.Followers;
import twitter.*;
import twitter4j.ResponseList;
import twitter4j.User;

public class twitterFindDoctors {	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Incorrect number of arguments-(What is file name?)");
			return;
		}
//		if (args.length == 2 && args[1].equals("continue")) {
//			continueProgram();
//			return;
//		}

		ExcelMethods excelHandle = new ExcelMethods();
		excelHandle.readFile(args[0]);
		
		FilterResults f = new FilterResults();
		List<ProfileDr> file = excelHandle.file;
		IntHolder g = new IntHolder(0);
		
		// Does matching specialties synonym work? Also, forgot to use keywords set too in Specialties. TESTING
		for (int i = 0; i < file.size(); i++) {
			f.gradeText("blank description", file.get(i).getSpecialty(), g, 5);
		}
		
		searchThroughFollowers(excelHandle, 119);
		System.out.println("Eureka");
	}
	static void searchForDoctors(ExcelMethods excelList) {
		SearchTwitter enterQueryHere = new SearchTwitter();
		FilterResults enterHandles = new FilterResults();
		
		// Rate limits dictate that the max query count is 180 per 15 minutes for users\search
		//     or 300 per 15 minutes per application (consumer Key)
		final int START = 0, MAX = 74;
		for (int i = START; i < excelList.getFile().size() && i < MAX; i++) {
			// Sets number of pages to search for doctor in results
			int nameCount = excelList.getFreqOfName().get(excelList.getFile().get(i).getFirstName() 
				+ " " + excelList.getFile().get(i).getLastName()).getNum();
			int pageLimit = 3;
			
			if (nameCount > 2) {
				pageLimit = 10;
			}
			
			enterQueryHere.search(excelList.getFile().get(i), pageLimit);
			System.out.print(i);
		}
		
		System.out.println();
		
		// Filter results of search results.
		int i = START;
		for (LinkedList<ResponseList<User>> searchResultsForDoctor : enterQueryHere.getSearchResults()) {
			enterHandles.filterResults(searchResultsForDoctor, excelList.getFile().get(i));
			i++;
			
			System.out.print(i);
		}
		
		enterHandles.publishTopHandles();
	}
	static ExcelMethods continueProgram() {
		long[] ids = {-5}; int id_index = -1; // Continue ids loop
		long[] idlist = {-5}; int users_index = -1; // Continue users loop
		Followers loadFollow = null; // Reload seed list and place in list
		ExcelMethods loadExcel = null; // Reload save data of doctors
		
		final String IDs_arr = "IDs_array.ser", 
				     userID_arr = "userID_array.ser",
				     followers_obj = "followers_obj.ser",
				     excel_obj = "excelmethods_obj.ser";
			
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(IDs_arr);
			ois = new ObjectInputStream(fis);
			ids = (long[]) ois.readObject();
			id_index = (int) ois.readObject();
			fis.close();
			ois.close();
			
			fis = new FileInputStream(userID_arr);
			ois = new ObjectInputStream(fis);
			idlist = (long[]) ois.readObject();
			users_index = (int) ois.readObject();
			fis.close();
			ois.close();
			
			fis = new FileInputStream(followers_obj);
			ois = new ObjectInputStream(fis);
			loadFollow = (Followers) ois.readObject();
			loadFollow.initializeRates();
			
			fis = new FileInputStream(excel_obj);
			ois = new ObjectInputStream(fis);
			loadExcel = (ExcelMethods) ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) fis.close();
				if (ois != null) ois.close();
			} catch(IOException ie) {
				ie.printStackTrace();
			}
		}
		
		if (ids[0] == -5 || id_index == -1) {
			System.err.println("Error: Opening file " + userID_arr);
			System.exit(-1);
		} if (idlist[0] == -5 || users_index == -1) {
			System.err.println("Error: Opening file " + userID_arr);
			System.exit(-1);
		} if (loadFollow == null || loadExcel == null) {
			System.err.println("Error: Opening file " + excel_obj);
			System.exit(-1);
		}
		
		FilterResults f = new FilterResults();
		
//		userLoop(idlist, users_index, loadFollow, loadExcel, f);
		idsLoop(ids, id_index, loadFollow, loadExcel, f);
		
		return loadExcel;
	}
	static void userLoop(long[] idlist, int users_index, Followers loadFollow, ExcelMethods loadExcel, FilterResults f) {
		// Continue: Users Loop
		// go through list of users gained through lookupUsers (size 100 max)
		ResponseList<User> usersArray = loadFollow.getUsersArray(idlist);
		
		System.err.println("Users #: " + Integer.toString(usersArray.size()));
		System.err.println("Index: " + Integer.toString(users_index));
		System.err.println("Number of users in loop: " + Integer.toString(usersArray.size() - users_index));
		
		for (int h = users_index; h < usersArray.size(); h++) {
			User u = usersArray.get(h);

			// First, middle, last name 1 USER
			// Made special parse to parse by non-alpha, except '-' b/c of certain names
			ArrayList<String> names = StringParse.parseNames(u.getName().toLowerCase()); 
			// go through each user and look for name in directory
			for (int i = 0; i < names.size(); i++) {
				System.out.print(names.get(i) + " ");
				
				Map<String, TreeMap<String, List<ProfileDr>>> m = loadExcel.getNamesLog();
				// first name
				if (m.get(names.get(i)) != null)
					// if first name found, find last name in map<string, profileDr>
					for (int j = 0; j < names.size(); j++)
//						// if not first name && last name is found, scan user (get(~~(j)) == null if fails)
						if (j != i && m.get(names.get(i)).get(names.get(j)) != null) {
//							System.out.println(names.get(i) + " " + names.get(j) + " " + u.getScreenName());
							
							// scan user against all matching profileDrs
							for (int k = 0; k < m.get(names.get(i)).get(names.get(j)).size(); k++)
								f.scanAccount(u, m.get(names.get(i)).get(names.get(j)).get(k));
						}
			}
			System.out.println();
		}
	}
	static void idsLoop(long[] ids, int id_index, Followers loadFollow, ExcelMethods loadExcel, FilterResults f) {
		long[] idlist;
		
		// go through list of ids by size 100 (or less), hence parsing
		LinkedList<long[]> arrayIDs = loadFollow.parseIDMass(ids);
		
		System.err.println("Index: " + Integer.toString(id_index));
		
		for (int i = id_index; i < arrayIDs.size(); i++) {
			idlist = arrayIDs.get(i);
			
			userLoop(idlist, 0, loadFollow, loadExcel, f);
		}	
	}
	static void searchThroughFollowers(ExcelMethods excelList, int searchLimit) {
		FilterResults filter = new FilterResults();
		Followers guid = new Followers();
		long[] ids; long[] idlist;
		int count = 0;
		
		while ((ids = guid.getIDs()) != null) {
			// go through list of ids by size 100 (or less), hence parsing
			LinkedList<long[]> arrayIDs = guid.parseIDMass(ids);
			for (int id_index = 0; id_index < arrayIDs.size(); id_index++) {
				idlist = arrayIDs.get(id_index);
				
				// go through list of users gained through lookupUsers (size 100 max)
				ResponseList<User> usersArray = guid.getUsersArray(idlist);
				for (int users_index = 0; users_index < usersArray.size(); users_index++) {
					User u = usersArray.get(users_index);

					// First, middle, last name 1 USER
					// Made special parse to parse by non-alpha, except '-' b/c of certain names
					ArrayList<String> names = StringParse.parseNames(u.getName().toLowerCase()); 
					// go through each user and look for name in directory
					for (int i = 0; i < names.size(); i++) {
						System.out.print(names.get(i) + " ");
						
						Map<String, TreeMap<String, List<ProfileDr>>> m = excelList.getNamesLog();
						// first name
						if (m.get(names.get(i)) != null)
							// if first name found, find last name in map<string, profileDr>
							for (int j = 0; j < names.size(); j++)
//								// if not first name && last name is found, scan user (get(~~(j)) == null if fails)
								if (j != i && m.get(names.get(i)).get(names.get(j)) != null) {
//									System.out.println(names.get(i) + " " + names.get(j) + " " + u.getScreenName());
									
									// scan user against all matching profileDrs
									for (int k = 0; k < m.get(names.get(i)).get(names.get(j)).size(); k++)
										filter.scanAccount(u, m.get(names.get(i)).get(names.get(j)).get(k));
								}
					}
					System.out.println();
					
					// Count limit: at limit of users searched, serialize ids array/index, current user id array/index, followers,
					//    excel methods. 
					count++; 
					if (count >= searchLimit) {
						FileOutputStream outputToFile = null;
						ObjectOutputStream objectStream = null;
						try {
							outputToFile = new FileOutputStream("IDs_array.ser");
							objectStream = new ObjectOutputStream(outputToFile);
							objectStream.writeObject(ids);
							objectStream.writeObject(id_index + 1);
							outputToFile.close();
							objectStream.close();
							
							outputToFile = new FileOutputStream("userID_array.ser");
							objectStream = new ObjectOutputStream(outputToFile);
							objectStream.writeObject(idlist);
							objectStream.writeObject(users_index);
							outputToFile.close();
							objectStream.close();
							
							outputToFile = new FileOutputStream("followers_obj.ser");
							objectStream = new ObjectOutputStream(outputToFile);
							objectStream.writeObject(guid);
							outputToFile.close();
							objectStream.close();
							
							outputToFile = new FileOutputStream("excelmethods_obj.ser");
							objectStream = new ObjectOutputStream(outputToFile);
							objectStream.writeObject(excelList);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (outputToFile != null) outputToFile.close();
								if (objectStream != null) objectStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}	
						}
						
						return;
					}
				}
			}
		}
		
		filter.publishTopHandles2(excelList);
	}
}
