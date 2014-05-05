package ese.gnomemsr2009;

public class Controller {

	public static void main(String[] args) throws Exception
	{
		//initialize objects
		DatabaseAccessor da = new DatabaseAccessor();
		IOFormatter io = new IOFormatter();
		long startTime = 0;
		long endTime = 0;
		//request for user input of Database name, database user-name and password
		io.inputConString();
		
		System.out.println("");
		System.out.println("Connecting to Database...");
		
		if(da.openConnection(io.getDBN(), io.getMysqlUserName(), io.getMysqlPass()))
		{
			System.out.println("Connected...");
			System.out.println("");
			
			//Request for user input for choice of service.
			//1. Generate Developers Network File in PAJEK Format
			//2. Generate Bugs-By-Developer Matrix in CSV Format
			//3. Generate Project Data Summary in CSV Format
			
			int choice = io.inputChoice();
			
			if(choice == 1)
			{
				//request user input for product name, start date and end date.
				io.inputData();
				
				startTime = System.nanoTime();
				
				//queries database for result
				da.sqlQueries(io.getProduct(), io.getStartDate(), io.getEndDate());
				
				endTime = System.nanoTime();
				
				
			}else if(choice == 2)
			{
				//request user input for product name, start date and end date.
				io.inputData();
				
				startTime = System.nanoTime();
				
				da.generateBugsByDev(io.getProduct(), io.getStartDate(), io.getEndDate());
				//Queries database for result, rearrange it into a bugs-by-developers matrix and save it to variable 'matrix'
				
				endTime = System.nanoTime();
			}else if(choice == 3)
			{
				io.inputData();
				
				startTime = System.nanoTime();
				
				da.generateDevsByDevs(io.getProduct(), io.getStartDate(), io.getEndDate());
				
				endTime = System.nanoTime();
			}else if(choice == 4)
			{				
				startTime = System.nanoTime();
				
				//queries database for project data summary and rearrange it to a .csv file format and save it to variable 'ProjectData'
				da.generateCSV();
				
				//output 'projectData' to a .csv file
				endTime = System.nanoTime();
			}else if(choice == 5)
			{
				io.inputData();
				
				
				startTime = System.nanoTime();
				
				//queries database for project data summary and rearrange it to a .csv file format and save it to variable 'ProjectData'
				da.generateBugModel(io.getProduct(), io.getStartDate(), io.getEndDate());
				
				//output 'projectData' to a .csv file
				endTime = System.nanoTime();
			}
			
			String matrix = da.getFileContent();
			String fN = da.getFileName();
			
			//output 'matrix' to a .csv file and append product name to the file name
			io.writeFile(matrix, fN);
			//close connection
			da.closeConnection();
			System.out.println("Time Elapsed: " + ((endTime - startTime)/1000000) + " milliseconds");
			
		}
		else
		{
			System.out.println("Wrong Connection String/UserName/Password!");
		}
	}

}
