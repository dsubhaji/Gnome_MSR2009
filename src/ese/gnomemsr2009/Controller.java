package ese.gnomemsr2009;

public class Controller {

	public static void main(String[] args) throws Exception
	{
		//initialize objects
		DatabaseAccessor da = new DatabaseAccessor();
		IOFormatter io = new IOFormatter();
		NetworkBuilder nb = new NetworkBuilder();
		
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
				
				long startTime = System.nanoTime();
				
				//queries database for result
				da.sqlQueries(io.getProduct(), io.getStartDate(), io.getEndDate());
				
				//rearrange result into a PAJEK file format
				nb.networkBuilder(da.getDevelopers(), da.getDevelopers2(), da.getDevelopers3(), da.getEdges(), da.getNum());
				
				//output text into file
				io.writePajekFile(nb.getDCN());
				long endTime = System.nanoTime();
				
				System.out.println("Time Elapsed: " + ((endTime - startTime)/1000000) + " milliseconds");
			}else if(choice == 2)
			{
				//request user input for product name, start date and end date.
				io.inputData();
				
				long startTime = System.nanoTime();
				
				//Queries database for result, rearrange it into a bugs-by-developers matrix and save it to variable 'matrix'
				String matrix = da.generateMatrix(io.getProduct(), io.getStartDate(), io.getEndDate());
				
				//output 'matrix' to a .csv file and append product name to the file name
				io.writeBugsByDevCSV(matrix);
				
				long endTime = System.nanoTime();
				
				System.out.println("Time Elapsed: " + ((endTime - startTime)/1000000) + " milliseconds");
			}else if(choice == 3)
			{				
				long startTime = System.nanoTime();
				
				//queries database for project data summary and rearrange it to a .csv file format and save it to variable 'ProjectData'
				String projectData = da.generateCSV();
				
				//output 'projectData' to a .csv file
				io.writeCSVFile(projectData);
				long endTime = System.nanoTime();
				System.out.println("Time Elapsed: " + ((endTime - startTime)/1000000) + " milliseconds");
			}
			
			//close connection
			da.closeConnection();
			
		}
		else
		{
			System.out.println("Wrong Connection String/UserName/Password!");
		}
	}

}
