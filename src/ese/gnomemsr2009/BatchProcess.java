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
	private String legalBugVariables[] = {"owner", "elapsed-time", "component", "version", "rep-platform", "op-sys",
			"bug-status", "resolution", "priority", "severity", "target-milestone", 
			"duplicate", "activity-level", "number-of-comments", "number-of-comments-by-owner",
			"number-of-commenters", "interest-span", "owner-workload", "owner-comment-arc", "degree", "betweenness", "clustcoeff",
			"closeness", "eigencentrality", "pagerank"
			};
	private String legalDevVariables[] = {"bugs-owned",
			"bugs-commented", "comment-span", "comments-on-owned", "comments-on-nonowned", "noof-activities",
			"average-elapsed-time", "median-elapsed-time", "average-interest-span", "median-interest-span",
			"degree", "betweenness", "clustcoeff", "closeness", "eigencentrality", "pagerank"
			};
	
	private ArrayList<String> productNames = new ArrayList<String>();
	private ArrayList<String> startDates = new ArrayList<String>();
	private ArrayList<String> endDates = new ArrayList<String>();
	
	private ArrayList<String> varTransform	 = new ArrayList<String>();
	private ArrayList<String> variables = new ArrayList<String>();
	
	private String modelType = "";
	
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
	
	/* checkVars(String)
	 * Input: Location of dependent.csv, independent.csv and model-type.csv
	 * Output: True if all the values in all three files are legal, false otherwise
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
		modelType = nextLine[0].trim();
		
		isTrue1 = checkModelType(modelType);
		
		reader = new CSVReader(new FileReader(dirName+"/dependent.csv"));
		
		nextLine = reader.readNext();
		variables.add(nextLine[0].trim());
		if(nextLine[1].trim().isEmpty())
		{
			varTransform.add("none");
		} else 
		{
			varTransform.add(nextLine[1].trim());
		}
		//dependentVar = nextLine[0].trim();
		isTrue2 = checkVariables(variables.get(0), modelType);
		
		reader = new CSVReader(new FileReader(dirName+"/independent.csv"));
		
		while ((nextLine = reader.readNext()) != null)
		{
			variables.add(nextLine[0].trim());
			//independentVars.add(nextLine[0].trim());
			if(nextLine[1].trim().isEmpty())
			{
				varTransform.add("none");
			} else
			{
				varTransform.add(nextLine[1].trim());
			}
		}
		
		
		//transformedVars = transformVariables(independentVars, varTransform);
		
		
		isTrue3 = checkVariables(variables, modelType);
		
		if((isTrue1&&isTrue2&&isTrue3) == true)
			areTheyTrue = true;
		
		return areTheyTrue;
	}
	
	/* batchQueries(ArrayList<String>, ArrayList<String>, ArrayList<String)
	 * Input: ArrayList of the product names, start and end dates listed on the csv file read in createDir()
	 * Output: a bunch of files on the respective product directory
	 */
	public void batchQueries(ArrayList<String> productNames, ArrayList<String> startDate, ArrayList<String> endDate) throws Exception
	{
		int prodCount = productNames.size();
		DatabaseAccessor da = Controller.da;
		IOFormatter io = new IOFormatter();
		RFunctions rf = Controller.rf;
		
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
			
			rf.nwMatrix(dirName, productNames.get(i));
			
			rf.linRegression(modelType, variables, varTransform, dirName, productNames.get(i));
			
			rf.varDescAndCor(modelType, variables, varTransform, dirName, productNames.get(i));
		}
	}
	
	/* descRegAndCor(ArrayList<String>, ArrayList<String>, ArrayList<String)
	 * Input: ArrayList of the product names, start and end dates listed on the csv file read in createDir()
	 * Output: Similar to batchQueries but only outputs linear regression, variables description and correlation
	 */
	public void descRegAndCor(ArrayList<String> productNames, ArrayList<String> startDate, ArrayList<String> endDate) throws Exception
	{
		int prodCount = productNames.size();
		DatabaseAccessor da = Controller.da;
		IOFormatter io = new IOFormatter();
		RFunctions rf = Controller.rf;
		File file = new File("");
		
		for(int i = 0; i < prodCount; i++)
		{
			long timeStart = System.nanoTime();
			System.out.println("STARTING: "+productNames.get(i));
			
			da.createPajek(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
			
			if(modelType.equals("bug"))
			{
			da.generateBugModel(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-details.csv");
			file = new File(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-details.csv");
			}else if(modelType.equals("developer"))
			{
			da.generateDevModel(productNames.get(i), startDate.get(i), endDate.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
			file = new File(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
			}
			
			rf.linRegression(modelType, variables, varTransform, dirName, productNames.get(i));
			rf.varDescAndCor(modelType, variables, varTransform, dirName, productNames.get(i));
			
			long timeEnd = System.nanoTime();
			System.out.println("");
			System.out.println(productNames.get(i)+" ENDED");
			System.out.println("TIME TAKEN: " + (((float)(timeEnd - timeStart)/1000000000)/60) + " minutes");
			System.out.println("");
			
		}
	}
	
	/* checkModelType(String)
	 * Input: String of the modeltype
	 * Output: boolean
	 * Function: Checks if the string is either 'developer' or 'bug'
	 */
	public boolean checkModelType(String s)
	{
		if(s.trim().equals("developer")||s.trim().equals("bug"))
		{
			return true;
		} else return false;
	}
	
	
	/* checkVariables(ArrayList<String>, String)
	 * Input: ArrayList of the independent variables and String of the modeltype
	 * Output: boolean
	 * Function: Checks if it is either a bug or developer model, and then checks if all entries in the arraylist is included in legalBugVariables or legalDevVariables respectively
	 * 			returns true if it checks out, returns false otherwise
	 */
	public boolean checkVariables(ArrayList<String> s, String b)
	{
		int legalVarArraySize = 0; 
		if(b.equals("developer"))
			legalVarArraySize = legalDevVariables.length;
		if(b.equals("bug"))
			legalVarArraySize = legalBugVariables.length;
		int varArraySize		= s.size();
		int trueCount			= 0;
		
		for(int i = 0; i < varArraySize; i++)
		{
			for(int j = 0; j < legalVarArraySize; j++)
			{
				if(b.equals("developer"))
				{
					if(legalDevVariables[j].equals(s.get(i)))
						trueCount++;
				}else if(b.equals("bug"))
				{
					if(legalBugVariables[j].equals(s.get(i)))
						trueCount++;
				}
				
			}
		}
		
		if(trueCount == varArraySize)
		{
			return true;
		} else return false;
	}
	
	/* checkVariables(String, String)
	 * Input: String of the independent variables and String of the modeltype
	 * Output: boolean
	 * Function: Checks if it is either a bug or developer model, and then checks if the dependent variable is included in legalBugVariables or legalDevVariables respectively
	 * 			returns true if it checks out, returns false otherwise
	 */
	public boolean checkVariables(String s, String b)
	{
		int legalVarArraySize = 0; 
		if(b.equals("developer"))
			legalVarArraySize = legalDevVariables.length;
		if(b.equals("bug"))
			legalVarArraySize = legalBugVariables.length;
		Boolean truth = false;
		
		for(int j = 0; j < legalVarArraySize; j++)
		{
			if(b.equals("developer"))
			{
				if(legalDevVariables[j].equals(s))
					  truth = true;
			} else if(b.equals("bug"))
			{
				if(legalBugVariables[j].equals(s))
					  truth = true;
			}
			
		}
		
		return truth;
	}
	
	
	
	/* batch(String)
	 * Input: String of the directory
	 * Function: Executes the various methods if all the dependent variable, independent variables and model type are legal
	 */
	
	public void batch(String s, int i) throws Exception
	{
		createDir(s);
		if(checkVars(s))
		{
			switch(i)
			{
				case 1: batchQueries(productNames, startDates, endDates);
						break;
				case 2: descRegAndCor(productNames, startDates, endDates);
						break;
				default:break;
			}
			
		} else
		{
			System.out.println("Illegal dependent variable, independent variables or model-type.");
		}
	}
	

}