package backup;

import java.util.List;
import excel.*;
import twitter.*;

public class twitterFindDoctors {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Incorrect number of arguments-(What is file name?)");
			return;
		}
		
		ExcelMethods excelHandle = new ExcelMethods();
		
		excelHandle.readFile(args[0]);
//		excelHandle.printFile();
		
		TwitterSearch twitterHandle = new TwitterSearch();
//		twitterHandle.printKeywordLists();
//		twitterHandle.search(new ProfileDr("Sean", "Lynch"));
		twitterHandle.searchForDoctors(excelHandle);
	}
}
