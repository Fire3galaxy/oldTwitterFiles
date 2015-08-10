package twitter;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Integer;

import au.com.bytecode.opencsv.CSVWriter;

// Will list number of accounts searched through and found
public class logAdmin {
	FileWriter logFW_count;
	CSVWriter logFile_count;
	
	FileWriter logFW_names;
	CSVWriter logFile_names;
	
	int searchedDoctors;	// For each name, a list of results is found. Its size is added here for a total.
	int foundDoctors;		// -possible idea: make function to write how many names searched for each individual person. 
							// (LOGwriteDoctorNumSearched(String name, int num))
	
	public logAdmin() {
		try {
			logFW_count = new FileWriter("AdminLogCount.csv");
			logFile_count = new CSVWriter(logFW_count);
			
			logFW_names = new FileWriter("AdminLogNames.csv");
			logFile_names = new CSVWriter(logFW_names);
			
			// Descriptions for each csv file at top.
			logFile_count.writeNext(new String[] {"This log records the number of "
					+ "Twitter accounts that were searched through during this session."});
			logFile_names.writeNext(new String[] {"This log records the doctors "
					+ "whose handles were found in this session."});
			
			searchedDoctors = 0;
			foundDoctors = 0;
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	// Writing to the files are done in FilterResults, publishing, near the bottom. Closing too.
	public void writeHandleLog(String name, String handle) { // First + last name, handle
		String[] doctor = new String[] {name, handle};
		logFile_names.writeNext(doctor);
		foundDoctors++;
	}
	public void writeNumLog() {
		LOGwriteNumSearched(searchedDoctors);
		LOGwriteNumFound(foundDoctors);
		
		// Testing.
//		LOGwriteNumSearched(num);
//		LOGwriteNumFound(num2);
	}
	public void incrementSearched(int num) {	// line 149 FilterResults (per page of search)
		searchedDoctors += num;
	}
	public void incrementFound() {	// line 678 FilterResults (at time of publishing results)
		foundDoctors++;
	}
	public void close() throws IOException {
		logFW_count.close();
		logFile_count.close();
		logFW_names.close();
		logFile_names.close();
	}
	
	void LOGwriteNumSearched(int num) {	// num of names searched total
		logFile_count.writeNext(new String[] {"num doctors searched:", Integer.toString(num)});
	}
	void LOGwriteNumFound(int num) {	// num of names found total
		logFile_count.writeNext(new String[] {"num doctors found:", Integer.toString(num)});
	}
	
	// unused
	void LOGwriteDoctorNumSearched(String name, int num) {	// num of names per individual doctor searched
		logFile_count.writeNext(new String[] {"num doctors searched:", Integer.toString(num), name});
	}
	void LOGwriteDoctorNumFound(String name, int num) {	// num of names per individual doctor found
		logFile_count.writeNext(new String[] {"num doctors found:", Integer.toString(num), name});
	}
}