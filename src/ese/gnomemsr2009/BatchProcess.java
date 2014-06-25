package ese.gnomemsr2009;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class BatchProcess {

	private static String dirName = "";
	private String legalVariables[] = {"owner", "elapsed-time", "component", "version", "rep-platform", "op-sys",
			"bug-status", "resolution", "priority", "severity", "target-milestone", 
			"duplicate", "activity-level", "number-of-comments", "number-of-comments-by-owner",
			"number-of-commenters", "interest-span", "owner-workload", "owner-comment-arc", "bugs-owned",
			"bugs-commented", "comment-span", "comments-on-owned", "comments-on-nonowned", "noof-activities",
			"average-elapsed-time", "median-elapsed-time", "average-interest-span", "median-interest-span",
			"degree", "betweenness"
			};
	private ArrayList<String> productNames = new ArrayList<String>();
	private ArrayList<String> startDates = new ArrayList<String>();
	private ArrayList<String> endDates = new ArrayList<String>();
	private ArrayList<String> independentVars = new ArrayList<String>();
	private String dependentVar = "";
	/*
	 * Input: Location of 'product-names.csv'
	 * Output: Creates a folder for each product listed in the csv file.
	 */
	
	public void createDir(String s) throws IOException
	{
		dirName = s;
		
		CSVReader reader = new CSVReader(new FileReader(dirName+"/product-names.csv"), ',', '\"', 1);
		String [] nextLine;
		
		
		while ((nextLine = reader.readNext()) != null) 
		{
			if(nextLine[1].isEmpty()||nextLine[1].trim().isEmpty()||nextLine[1]==null)
			{
				nextLine[1] = "0000-00-00";
			}
			if(nextLine[2].isEmpty()||nextLine[2].trim().isEmpty()||nextLine[2]==null)
			{
				nextLine[2] = "9999-01-01";
			}
			
			productNames.add(nextLine[0].trim());
			startDates.add(nextLine[1].trim());
			endDates.add(nextLine[2]);
			
			File theDir = new File(dirName+"/"+nextLine[0].trim()	);
			if (!theDir.exists()) theDir.mkdir();
			//System.out.println("ProductName: " + nextLine[0] + "\nStartDate: " + nextLine[1] + "\nEndDate: " + nextLine[2] + "");	
		}
	}
	
	/* Input: Location of dependent.csv, independent.csv and model-type.csv
	 * Output: True if all the values in all three files are legal
	 */
	@SuppressWarnings("resource")
	public boolean checkVars(String s) throws Exception
	{
		dirName = s;
		
		String [] nextLine;
		Boolean isTrue1 = false;
		Boolean isTrue2 = false;
		Boolean isTrue3 = false;
		Boolean areTheyTrue = false;
		
		CSVReader reader = new CSVReader(new FileReader(dirName+"/model-type.csv"));
		
		nextLine = reader.readNext();
		
		isTrue1 = checkModelType(nextLine[0].trim());
		
		reader = new CSVReader(new FileReader(dirName+"/dependent.csv"));
		
		nextLine = reader.readNext();
		dependentVar = nextLine[0].trim();
		isTrue2 = checkVariables(dependentVar);
		
		reader = new CSVReader(new FileReader(dirName+"/independent.csv"));
		
		while ((nextLine = reader.readNext()) != null)
		{
			independentVars.add(nextLine[0].trim());
		}
		
		isTrue3 = checkVariables(independentVars);
		
		if((isTrue1&&isTrue2&&isTrue3) == true)
			areTheyTrue = true;
		
		return areTheyTrue;
	}
	
	/*
	 * Input: ArrayList of the product names, start and end dates listed on the csv file read in createDir()
	 * Output: 
	 */
	public void batchQueries(ArrayList<String> productNames, ArrayList<String> startDate, ArrayList<String> endDate) throws Exception
	{
		int prodCount = productNames.size();
		DatabaseAccessor da = Controller.da;
		IOFormatter io = new IOFormatter();
		
		
		for(int i = 0; i < prodCount; i++)
		{
			da.createPajek(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
			da.generateBugsByDev(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-by-devs.csv");
			da.generateDevsByDevs(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-by-devs.csv");
			da.generateCSV(productNames.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-summary.csv");
			da.generateBugModel(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-details.csv");
			da.generateDevModel(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
		}
	}
	
	public boolean checkModelType(String s)
	{
		if(s.trim().equals("developer")||s.trim().equals("bug"))
		{
			return true;
		} else return false;
	}
	
	public boolean checkVariables(ArrayList<String> s)
	{
		int legalVarArraySize 	= legalVariables.length;
		int varArraySize		= s.size();
		int trueCount			= 0;
		for(int i = 0; i < varArraySize; i++)
		{
			for(int j = 0; j < legalVarArraySize; j++)
			{
				if(legalVariables[j].equals(s.get(i)))
					trueCount++;
			}
		}
		
		if(trueCount == varArraySize)
		{
			return true;
		} else return false;
	}
	
	public boolean checkVariables(String s)
	{
		int legalVarArraySize 	= legalVariables.length;
		Boolean truth = false;
		
		int trueCount			= 0;
		
		for(int j = 0; j < legalVarArraySize; j++)
		{
			if(legalVariables[j].equals(s))
			  truth = true;
		}
		
		return truth;
	}
	
	public void batch(String s) throws Exception
	{
		createDir(s);
		if(checkVars(s))
		{
			batchQueries(productNames, startDates, endDates);
		} else
		{
			System.out.println("Illegal dependent variable, independent variables or model-type.");
		}
	}
	

}
