package twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import twitter.JobCode;

public class Keywords {
	List<ArrayList<String>> keywords1;
	List<ArrayList<String>> shortKeywords1;
	List<ArrayList<String>> specialKeywords1;
	
	List<ArrayList<Integer>> keywordValues1;
	List<ArrayList<Integer>> shortKeywordValues1;
	List<ArrayList<Integer>> specialKeywordValues1;
	
	public Keywords() {
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
	}
	void insertKeywordsAndValues(String whichOne, final String[] keywords, final int[] values, int code) {
		if (whichOne.equals("keywords1")) {
			keywords1.add(new ArrayList<String>());
			keywordValues1.add(new ArrayList<Integer>());
			
			for (int i = 0; i < keywords.length; i++) {
				keywords1.get(code).add(keywords[i]);
				keywordValues1.get(code).add(values[i]);
			}
		}
		else if (whichOne.equals("shortKeywords1")) {
			shortKeywords1.add(new ArrayList<String>());
			shortKeywordValues1.add(new ArrayList<Integer>());
			
			for (int i = 0; i < keywords.length; i++) {
				shortKeywords1.get(code).add(keywords[i]);
				shortKeywordValues1.get(code).add(values[i]);
			}
		}
		else if (whichOne.equals("specialKeywords1")) {
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
	JobCode selectKeywordList(String title, String specialty) {
		// include title and specialty
		
		if (title.equals("MD")) {
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
}