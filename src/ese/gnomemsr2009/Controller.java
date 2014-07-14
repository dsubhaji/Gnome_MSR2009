package ese.gnomemsr2009;

public class Controller {

	static DatabaseAccessor da = new DatabaseAccessor();
	static IOFormatter io = new IOFormatter();
	static BatchProcess bp = new BatchProcess();
	static RFunctions rf = new RFunctions();
	
	
	public static void main(String[] args) throws Exception
	{
		//initialize objects
		
		float startTime = 0;
		float endTime = 0;
		rf.startRengine();
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
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 1);
				
				endTime = System.nanoTime();
				
				
			}else if(choice == 2)
			{
				//request user input for product name, start date and end date.
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 2);
				
				endTime = System.nanoTime();
			}else if(choice == 3)
			{
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 3);
				
				endTime = System.nanoTime();
			}else if(choice == 4)
			{				
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 4);
				
				endTime = System.nanoTime();
			}else if(choice == 5)
			{
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 5);
				
				endTime = System.nanoTime();
			}else if(choice == 6)
			{
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 6);
				
				endTime = System.nanoTime();
			}else if(choice == 7)
			{
				io.batchInput();
				
				startTime = System.nanoTime();
				
				//queries database for result
				bp.singleServices(io.getDirectoryPath(), 7);
				
				endTime = System.nanoTime();
			}else if(choice == 8)
			{
				io.batchInput();
				startTime = System.nanoTime();
				bp.batch(io.getDirectoryPath(), 1);
				endTime = System.nanoTime();
			}else if(choice == 9)
			{
				io.batchInput();
				startTime = System.nanoTime();
				bp.batch(io.getDirectoryPath(), 2);
				endTime = System.nanoTime();
			}else if(choice == 10)
			{
				io.batchInput();
				startTime = System.nanoTime();
				bp.batch(io.getDirectoryPath(), 3);
				endTime = System.nanoTime();
			}
			
			
			
			//output 'matrix' to a .csv file and append product name to the file name
			/*if((choice != 7)||(choice != 8)||(choice != 9))
			{
				if(io.writeFile(da.getFileContent(), da.getFileName()))
				{
					System.out.println("");
					System.out.println("File Generated");
				}else
				{
					System.out.println("");
					System.out.println("COULD NOT WRITE!");
				}
			}*/
			 
			if((((endTime - startTime)/1000000000)/60) < 1) System.out.println("Total Time Elapsed: " + (((endTime - startTime)/1000000)) + " milliseconds");
			else System.out.println("Total Time Elapsed: " + (((endTime - startTime)/1000000000)/60) + " minutes");
			//close connection
			//da.closeConnection();
			
			
		}
		else
		{
			System.out.println("Wrong Connection String/UserName/Password!");
		}
		
		rf.closeRengine();
	}

}
