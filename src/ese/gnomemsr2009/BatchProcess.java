package ese.gnomemsr2009;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
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
			"number-of-commenters", "interest-span", "owner-workload", "owner-comment-arc"
			};
	private String legalDevVariables[] = {"bugs-owned",
			"bugs-commented", "comment-span", "comments-on-owned", "comments-on-nonowned", "noof-activities",
			"average-elapsed-time", "median-elapsed-time", "average-interest-span", "median-interest-span", "congruence",
			"dcn.degree", "dcn.betweenness", "dcn.clustcoeff", "dcn.closeness", "dcn.eigencentrality", "dcn.pagerank",
			"dan.degree", "dan.betweenness", "dan.clustcoeff", "dan.closeness", "dan.eigencentrality", "dan.pagerank"
			};
	
	private ArrayList<String> productNames = new ArrayList<String>();
	private ArrayList<String> startDates = new ArrayList<String>();
	private ArrayList<String> endDates = new ArrayList<String>();
	
	private ArrayList<String> varTransform	 = new ArrayList<String>();
	private ArrayList<String> variables = new ArrayList<String>();
	
	private ArrayList<String> parameters = new ArrayList<String>();
	
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
			if(nextLine[1].isEmpty()||nextLine[1].trim().isEmpty()||nextLine[1]==null||nextLine[1].trim().equals("none"))
			{
				nextLine[1] = "0000-00-00";
			}
			if(nextLine[2].isEmpty()||nextLine[2].trim().isEmpty()||nextLine[2]==null||nextLine[2].trim().equals("none"))
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
	public boolean checkVars(String s, int i) throws Exception
	{
		dirName = s;
		
		String [] nextLine;
		Boolean isTrue1 = false;
		Boolean isTrue2 = false;
		Boolean isTrue3 = false;
		Boolean areTheyTrue = false;
		
		if(i == 1)
		{
			areTheyTrue = true;
		}
		if(i == 2)
		{
			CSVReader reader = new CSVReader(new FileReader(dirName+"/model-type.csv"), ',', '\"', 1);
			
			nextLine = reader.readNext();
			modelType = nextLine[0].trim();
			
			isTrue1 = checkModelType(modelType);
			
			reader = new CSVReader(new FileReader(dirName+"/dependent.csv"), ',', '\"', 1);
			
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
			
			reader = new CSVReader(new FileReader(dirName+"/independent.csv"), ',', '\"', 1);
			
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
			
			System.out.println("truth1: "+isTrue1);System.out.println("truth2: "+isTrue2);System.out.println("truth3: "+isTrue3);
			
			
			if((isTrue1&&isTrue2&&isTrue3) == true)
				areTheyTrue = true;
		}
		if(areTheyTrue == false) System.out.println("\nIllegal dependent variable, independent variables or model-type.");	
		return areTheyTrue;
	}
	
	@SuppressWarnings("resource")
	public boolean factorAnalysis(String s, int i) throws Exception
	{
		String [] nextLine;
		
		CSVReader reader;
		
		if(i == 1)
		{
			reader = new CSVReader(new FileReader(dirName+"/parameters.csv"), ',', '\"', 1);
			
			while ((nextLine = reader.readNext()) != null)
			{
				parameters.add(nextLine[1].trim());
			}
		}
		
		
		reader = new CSVReader(new FileReader(dirName+"/model-type.csv"), ',', '\"', 1);
		
		nextLine = reader.readNext();
		modelType = nextLine[0].trim();
		
		boolean isTrue = false;
		isTrue = checkModelType(modelType);
		
		reader = new CSVReader(new FileReader(dirName+"/variables.csv"), ',', '\"', 1);
		
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
		
		
		isTrue = isTrue&&checkVariables(variables, modelType);
		
		if(isTrue == false) System.out.println("\nIllegal variables or model-type.");
		return isTrue;
		
	}
	
	/* batchQueries(ArrayList<String>, ArrayList<String>, ArrayList<String)
	 * Input: ArrayList of the product names, start and end dates listed on the csv file read in createDir()
	 * Output: a bunch of files on the respective product directory
	 */
	public void batchQueries(int a) throws Exception
	{
		int prodCount = productNames.size();
		DatabaseAccessorGnome da = Controller.da;
		IOFormatter io = new IOFormatter();
		RFunctions rf = Controller.rf;
		
		for(int i = 0; i < prodCount; i++)
		{
			if(a==2)
			{
				da.generateOwnersDCN(productNames.get(i), startDates.get(i), endDates.get(i));
				io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
				da.generateOwnersDAN(productNames.get(i), startDates.get(i), endDates.get(i));
				io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN.net");
				da.generateOwnersModel(productNames.get(i), startDates.get(i), endDates.get(i), dirName);
				io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
			} else if (a==1)
			{
				da.generateDCN(productNames.get(i), startDates.get(i), endDates.get(i));
				io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
				da.generateDAN(productNames.get(i), startDates.get(i), endDates.get(i));
				io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN.net");
				da.generateCommenterModel(productNames.get(i), startDates.get(i), endDates.get(i), dirName);
				io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
			}
			
			da.generateBugModel(productNames.get(i), startDates.get(i), endDates.get(i));
			io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-details.csv");
			
			File file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
			if(file.exists()) 
			{
				System.out.println("\nGenerating DCN Metrics File for " + productNames.get(i));
				rf.DCNMetrics(dirName, productNames.get(i));
			}
			else System.out.println("\nCan't find DCN File for: "+productNames.get(i));
			
			file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN.net");
			if(file.exists())
			{
				System.out.println("\nGenerating DAN Metrics File for " + productNames.get(i));
				rf.DANMetrics(dirName, productNames.get(i));
			}
			else System.out.println("\nCan't find DAN File for: "+productNames.get(i));
		}
		System.out.println("\nGenerating Product Summary.");
		da.projectSummary(productNames, dirName, false);
		io.writeFile(da.getFileContent(), dirName+"/project-summary.csv");
	}
	
	/* descRegAndCor(ArrayList<String>, ArrayList<String>, ArrayList<String)
	 * Input: ArrayList of the product names, start and end dates listed on the csv file read in createDir()
	 * Output: Similar to batchQueries but only outputs linear regression, variables description and correlation
	 */
	public void descRegAndCor(int a) throws Exception
	{
		int prodCount = productNames.size();
		DatabaseAccessorGnome da = Controller.da;
		IOFormatter io = new IOFormatter();
		RFunctions rf = Controller.rf;
		
		//File theDir = new File(dirName+"/results/");
		//if (!theDir.exists()) theDir.mkdir();
		
		for(int i = 0; i < prodCount; i++)
		{
			long timeStart = System.nanoTime();
			System.out.println("\nSTARTING: "+productNames.get(i));
			
			//System.out.println(variables);
			if(a == 1) 
			{
				rf.linRegression(modelType, variables, varTransform, dirName, productNames.get(i));
				rf.varDescAndCor(modelType, variables, varTransform, dirName, productNames.get(i));
			}
			if(a == 2) rf.eigenVal(modelType, variables, varTransform, parameters, dirName, productNames.get(i));
			
			
			long timeEnd = System.nanoTime();
			System.out.println("");
			System.out.println(productNames.get(i)+" ENDED");
			System.out.println("TIME TAKEN: " + (((float)(timeEnd - timeStart)/1000000000)/60) + " minutes");
		}
	}
	
	public boolean checkSubFolder()
	{
		int prodCount = productNames.size();
		File file = null;
		File file2 = null;
		boolean bool = true;
		ArrayList<String> errMess = new ArrayList<String>();
		
		
		for(int i = 0; i < prodCount; i++)
		{
			if(modelType.equals("bug"))
			{
				file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-details.csv");
			}else if(modelType.equals("developer"))
			{
				file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
			}
			file2 = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN-metrics.csv");
			
			if(file.exists()&&file2.exists())
			{
				bool = bool&&true;
			} else
			{
				bool = bool&&false;
				errMess.add(productNames.get(i));
			}
		}
		
		if(bool==false) System.out.println("\nMissing File(s) for: " + errMess);
		return bool;
	}
	
	/* checkModelType(String)
	 * Input: String of the modeltype
	 * Output: boolean
	 * Function: Checks if the string is either 'developer' or 'bug'
	 */
	public boolean checkModelType(String s)
	{
		if(s.trim().equalsIgnoreCase("developer")||s.trim().equalsIgnoreCase("bug"))
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
		switch(i)
		{
			case 100:	if(checkVars(s, 1)) batchQueries(2);
						break;
			case 1: 	if(checkVars(s, 1)) batchQueries(1);
						break;
			case 200:
			case 2: 	if(checkVars(s, 2)&&checkSubFolder()) descRegAndCor(1);
						break;
			case 300:
			case 3:		if(factorAnalysis(s, 1)&&checkSubFolder()) descRegAndCor(2);
						break;
			case 4: 
			default:break;	
		}
		
	}
	
	
	public void singleServices(String s, int a) throws Exception
	{
		createDir(s);
		
		int prodCount = productNames.size();
		DatabaseAccessorGnome da = Controller.da;
		DatabaseAccessorGithub daMSR = Controller.daMSR;
		IOFormatter io = new IOFormatter();
		RFunctions rf = Controller.rf;
		
		
		if(da.getDBName().equalsIgnoreCase("sutd")||da.getDBName().equalsIgnoreCase("gnome_msr2009"))
		{
			if(a!=8&&a!=800&&a!=9&&a!=900)
			for(int i = 0; i < prodCount; i++)
			{
				long timeStart = System.nanoTime();
				System.out.println("\nSTARTING: "+productNames.get(i));
				File file;
				switch(a)
				{
					/*
					 * Case 1: Generate PAJEK file for the specified product, if it already exist, do nothing.
					 */
					case 1: 	da.generateDCN(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
								break;
					case 100:	da.generateOwnersDCN(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
								break;
					case 2: 	da.generateDAN(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN.net");
								break;
					case 200:	da.generateOwnersDAN(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN.net");
								break;
					case 300: 
					case 3: 	file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
								if(file.exists()) 
								{
									System.out.println("\nGenerating DCN Metrics File for " + productNames.get(i));
									rf.DCNMetrics(dirName, productNames.get(i));
								}
								else System.out.println("\nCan't find DCN File for: "+productNames.get(i));
								file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN.net");
								if(file.exists())
								{
									System.out.println("\nGenerating DAN Metrics File for " + productNames.get(i));
									rf.DANMetrics(dirName, productNames.get(i));
								}
								else System.out.println("\nCan't find DAN File for: "+productNames.get(i));
								break;
					case 4:		da.generateBugsByDev(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN-bug-by-devs.csv");
								break;
					case 400:	da.generateBugsByOwners(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN-bug-by-devs.csv");
								break;
					case 5: 	da.generateDevsByDevs(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN-dev-by-devs.csv");
								break;
					case 500:	da.generateOwnersByOwners(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN-dev-by-devs.csv");
								break;
					case 6:		da.generateActivityByDev(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN-bug-by-devs.csv");
								break;
					case 600:	da.generateActivityByOwners(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN-bug-by-devs.csv");
								break;
					case 7: 	da.generateDevsByDevsActivity(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN-dev-by-devs.csv");
								break;
					case 700:	da.generateOwnersByOwnersActivity(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DAN-dev-by-devs.csv");
								break;
					case 800:
					case 8: 	break;
					case 900:
					case 9:		break;
					case 1000:
					case 10: 	da.generateBugModel(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-details.csv");
								break;
					case 11:	da.generateCommenterModel(productNames.get(i), startDates.get(i), endDates.get(i), dirName);
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
								break;
					case 1100: 	da.generateOwnersModel(productNames.get(i), startDates.get(i), endDates.get(i), dirName);
								io.writeFile(da.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-details.csv");
								break;
					case 1200:
					case 12:	if(factorAnalysis(s, 2)&&checkSubFolder()) rf.varDescAndCor(modelType, variables, varTransform, dirName, productNames.get(i));
								break;
					default:	System.out.println("Not Implemented Yet!");
								break;
				}
							
				long timeEnd = System.nanoTime();
				System.out.println("");
				System.out.println(productNames.get(i)+" ENDED");
				System.out.println("TIME TAKEN: " + (((float)(timeEnd - timeStart)/1000000000)/60) + " minutes");
				System.out.println("");
			}
			
			if(a==8||a==800)
			{
				System.out.println("\nGenerating Product Summary.");
				da.projectSummary(productNames, dirName, false);
				io.writeFile(da.getFileContent(), dirName+"/project-summary.csv");
			}
			if(a==9||a==900)
			{
				System.out.println("\nGenerating Product Summary.");
				da.projectSummary(productNames, dirName, true);
				io.writeFile(da.getFileContent(), dirName+"/project-summary.csv");
			}
			
		}else if(da.getDBName().equalsIgnoreCase("github_msr2014"))
		{
			for(int i = 0; i < prodCount; i++)
			{
				long timeStart = System.nanoTime();
				System.out.println("\nSTARTING: "+productNames.get(i));
				File file;
				switch(a)
				{
					/*
					 * Case 1: Generate PAJEK file for the specified product, if it already exist, do nothing.
					 */
					case 100:
						case 1: file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
								if(true) {daMSR.generateDCN(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(daMSR.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");}
								break;
					case 300:
						case 3: file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-DCN.net");
								//if(true) System.out.println("Can't find PAJEK File for: "+productNames.get(i));
								rf.DCNMetrics(dirName, productNames.get(i));
								break;
					case 400:
						case 4: file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-by-devs.csv");
								if(true) {daMSR.generateBugsByDev(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(daMSR.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-bug-by-devs.csv");}
								break;
					case 500:
						case 5: file = new File(dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-by-devs.csv");
								if(true) {daMSR.generateDevsByDevs(productNames.get(i), startDates.get(i), endDates.get(i));
								io.writeFile(daMSR.getFileContent(), dirName+"/"+productNames.get(i)+"/"+productNames.get(i)+"-dev-by-devs.csv");}
								break;
					case 600:
						case 6: break;
					default:System.out.println("Not Implemented Yet!");
							break;
				}
							
				long timeEnd = System.nanoTime();
				System.out.println("");
				System.out.println(productNames.get(i)+" ENDED");
				System.out.println("TIME TAKEN: " + (((float)(timeEnd - timeStart)/1000000000)/60) + " minutes");
				System.out.println("");
			}
			
			if(a==8||a==800)
			{
				System.out.println("\nGenerating Product Summary.");
				daMSR.projectSummary(productNames, dirName);
				io.writeFile(daMSR.getFileContent(), dirName+"/project-summary.csv");
			}
		}
	}

}
