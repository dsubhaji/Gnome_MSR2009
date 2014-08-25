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
			
			int multiplier = 1;
			int choice = 0;
			
			if(io.getDBN().equalsIgnoreCase("sutd")||io.getDBN().equalsIgnoreCase("gnome_msr2009"))
			{
				multiplier = io.inputType();
				choice = io.inputChoiceGnome();
			}
			if(io.getDBN().equalsIgnoreCase("github_msr2014"))
				choice = io.inputChoiceGithub();
			
			io.batchInput();
			startTime = System.nanoTime();
			bp.singleServices(io.getDirectoryPath(), choice*multiplier);
			endTime = System.nanoTime();
			System.out.println("Total Time Elapsed: " + (((endTime - startTime)/1000000000)/60) + " minutes");
		}
		else
		{
			System.out.println("Wrong Connection String/UserName/Password!");
		}
		
		
		rf.closeRengine();
	}

}
