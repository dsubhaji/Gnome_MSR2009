package ese.gnomemsr2009;

public class Controller {

	static DatabaseAccessorGnome da = new DatabaseAccessorGnome();
	static DatabaseAccessorGithub daMSR = new DatabaseAccessorGithub();
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
		
		boolean isGnome = da.openConnection(io.getDBN(), io.getMysqlUserName(), io.getMysqlPass());
		boolean isGithub= daMSR.openConnection(io.getDBN(), io.getMysqlUserName(), io.getMysqlPass());
		
		if(isGnome&&isGithub)
		{
			System.out.println("Connected...");
			System.out.println("");
			
			//Request for user input for choice of service.
			//1. Generate Developers Network File in PAJEK Format
			//2. Generate Bugs-By-Developer Matrix in CSV Format
			//3. Generate Project Data Summary in CSV Format
			
			int multiplier = 1;
			
			int choice = 0;
			
			if(io.getDBN().equalsIgnoreCase("sutd")||io.getDBN().equalsIgnoreCase("gnome_msr2009"))
			{
				multiplier = io.inputType();
				choice = io.inputChoiceGnome();
			}
			if(io.getDBN().equalsIgnoreCase("github_msr2014"))
				choice = io.inputChoiceGithub();
			
			if((choice >= 1)&&(choice <= 12))
			{
				io.batchInput();
				
				startTime = System.nanoTime();
				
				bp.singleServices(io.getDirectoryPath(), choice*multiplier);
				
				endTime = System.nanoTime();
			}else if(choice == 13)
			{
				io.batchInput();
				startTime = System.nanoTime();
				bp.batch(io.getDirectoryPath(), 1*multiplier);
				endTime = System.nanoTime();
			}else if(choice == 14)
			{
				io.batchInput();
				startTime = System.nanoTime();
				bp.batch(io.getDirectoryPath(), 2*multiplier);
				endTime = System.nanoTime();
			}else if(choice == 15)
			{
				io.batchInput();
				startTime = System.nanoTime();
				bp.batch(io.getDirectoryPath(), 3*multiplier);
				endTime = System.nanoTime();
			}
			 
			System.out.println("Total Time Elapsed: " + (((endTime - startTime)/1000000000)/60) + " minutes");
		}
		else
		{
			System.out.println("Wrong Connection String/UserName/Password!");
		}
		
		rf.closeRengine();
	}

}
