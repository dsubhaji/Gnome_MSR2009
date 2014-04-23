package ese.gnomemsr2009;



public class Controller {

	public static void main(String[] args) throws Exception
	{
	
		DatabaseAccessor da = new DatabaseAccessor();
		IOFormatter io = new IOFormatter();
		NetworkBuilder nb = new NetworkBuilder();
		
		io.inputConString();
		
		System.out.println("");
		System.out.println("Connecting to Database...");
		
		if(da.openConnection(io.getDBN(), io.getMysqlUserName(), io.getMysqlPass()))
		{
			System.out.println("Connected...");
			System.out.println("");
			io.inputData();
			
			long startTime = System.nanoTime();
			da.sqlQueries(io.getProduct(), io.getStartDate(), io.getEndDate());
			da.closeConnection();
		
			nb.networkBuilder(da.getDevelopers(), da.getDevelopers2(), da.getDevelopers3(), da.getEdges(), da.getNum());
			
			io.writePajekFile(io.getProduct(), nb.getDCN());
			long endTime = System.nanoTime();
			
			System.out.println("Time Elapsed: " + ((endTime - startTime)/1000000) + " milliseconds");
		}
		else
		{
			System.out.println("Wrong Connection String/UserName/Password!");
		}
	}

}
