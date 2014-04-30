package ese.gnomemsr2009;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DatabaseAccessor 
{
	Connection con;
	ResultSet rs;
	Statement s;
	
	private NetworkBuilder nb = new NetworkBuilder();
	
	private String fileContent;
	private String fileName;

	private int num;

	public DatabaseAccessor()
	{
		fileContent = "";
		fileName = "";
	}
	
	public String getFileContent()
	{
		return fileContent;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public boolean openConnection(String databaseName, String mysqlUser, String password) throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver"); //load mysql driver
		try
		{
			con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/" + databaseName + "?user=" + mysqlUser + "&password=" + password); //set-up connection with database
			
			
		} catch (SQLException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void sqlQueries(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> developers = new ArrayList<String>();
		ArrayList<String> developers2= new ArrayList<String>();
		ArrayList<String> developers3= new ArrayList<String>();
		ArrayList<Integer> edges     = new ArrayList<Integer>();
		
		
		System.out.println("");
		System.out.println("Calculating the Total Number of Distinct Developers...");
		s = con.createStatement(); //Statements to issue sql queries
		rs = s.executeQuery(
				"select count(distinct(b.who)) "+
				"from bugs c, comment b " +
				"where c.bug_id = b.bugid " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"' ) "+
				"and trim(' ' from product) like '%"+product+"\n';"
				); //ResultSet gets Query results. Query to find out the total number of distinct developers commenting on the bugs of a specific product
		
		while(rs.next())
		{
			num = rs.getInt("count(distinct(b.who))");
		}
		
		System.out.println("Retrieving the Developer's E-Mail Addresses...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(b.who, '\n', ''))) \"who\""+
				"from bugs c, comment b " +
				"where c.bug_id = b.bugid " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s')) between '"+startDate+"' and '"+endDate+"' "  +
				"and trim(' ' from product) like '%"+product+"\n' " +
				"order by who;"
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			developers.add(rs.getString("who"));
		}
		
		System.out.println("Building the Developer Communication Network...");
		
		rs = s.executeQuery(
				"select trim(' ' from replace(a.who, '\n', '')), count(distinct(a.bugid)), trim(' ' from replace(b.who, '\n', '')) " +
						"from comment a, comment b " +
						"where a.bugid IN " +
						"(" +
							"select b.bugid " +
							"from bugs c, comment b " +
							"where c.bug_id = b.bugid " +
							"and trim(' ' from c.product) like '%"+product+"\n' "+
						") " +
						"and a.who <> b.who " +
						"and a.bugid = b.bugid "+
						"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
						"group by a.who, b.who " +
						"order by trim(' ' from replace(a.who, '\n', ''));"
						//Query to find how many times a developer work with another developer on the bugs of a particular component
				);
		
		while(rs.next())
		{
			developers2.add(rs.getString("trim(' ' from replace(a.who, '\n', ''))"));
			developers3.add(rs.getString("trim(' ' from replace(b.who, '\n', ''))"));
			edges.add((rs.getInt("count(distinct(a.bugid))")));
		}
		
		
		
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss");
		fileName = "DCN-gnomemsr2009-"+product+"-"+df.format(new Date())+".net";
		fileContent = nb.networkBuilder(developers, developers2, developers3, edges, num);
		
	}
	
	public void generateCSV() throws Exception
	{
		System.out.println("Extracting Data from Database...");
		
		s = con.createStatement(); //Statements to issue sql queries
		rs = s.executeQuery("select distinct(trim(' ' from replace(a.product, '\n', ''))), count(distinct(b.bugid)), count(b.bug_when), count(distinct(b.who)), MIN(trim(' ' from replace(b.bug_when, '\n', ''))), MAX(trim(' ' from replace(b.bug_when, '\n', ''))) "
							+"from bugs a, comment b "
							+"where a.bug_id = b.bugid "
							+"group by a.product "
							);
		
		StringBuilder csv = new StringBuilder();
		csv.append("\"Name of Component\", \"Number of Bugs\", \"Total Number of Comments\", \"No. Of Distinct Developers\", \"Date of First Comment\", \"Date of Last Comment\"\n");
		
		System.out.println("Generating .CSV File");
		while(rs.next())
		{
			csv.append("\""+rs.getString("(trim(' ' from replace(a.product, '\n', '')))")+"\", ");
			csv.append(rs.getInt("count(distinct(b.bugid))") + ",");
			csv.append(rs.getInt("count(b.bug_when)") + ", ");
			csv.append(rs.getInt("count(distinct(b.who))")+ ", ");
			csv.append("\""+rs.getString("MIN(trim(' ' from replace(b.bug_when, '\n', '')))")+"\", ");
			csv.append("\""+rs.getString("MAX(trim(' ' from replace(b.bug_when, '\n', '')))")+"\"\n");
		}
		
		fileName = "ProjectDataSummary.csv";
		fileContent = csv.toString();
	}
	
	public void generateBugsByDev(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> distinctBug_id 			= new ArrayList<String>();
		ArrayList<String> distinctDev_email 		= new ArrayList<String>();
		ArrayList<String> bug_id 			= new ArrayList<String>();
		ArrayList<String> dev_email 		= new ArrayList<String>();
		ArrayList<Integer> numOfComments 	= new ArrayList<Integer>();
		
		System.out.println("\nExtracting Data from Database...");
		
		s = con.createStatement(); //Statements to issue sql queries
		rs = s.executeQuery("select distinct(trim(' ' from replace(a.bug_id, '\n', ''))) " +
							"from bugs a, comment b "+
							"where a.bug_id = b.bugid "+
							"and trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
							"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
							"order by bug_id;"	
							);
		
		while(rs.next())
		{
			distinctBug_id.add(rs.getString("(trim(' ' from replace(a.bug_id, '\n', '')))"));
		}
		
		rs = s.executeQuery(
							"select distinct(trim(' ' from replace(b.who, '\n', ''))) " +
							"from bugs a, comment b " +
							"where a.bug_id = b.bugid " +
							"and trim(' ' from replace(a.product, '\n', '')) = '"+product+"' " +
							"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
							"order by b.who;"
							);
		
		while(rs.next())
		{
			distinctDev_email.add(rs.getString("(trim(' ' from replace(b.who, '\n', '')))"));
		}
		
		rs = s.executeQuery(
							"select distinct(trim(' ' from replace(a.bug_id, '\n', ''))), trim(' ' from replace(b.who, '\n', '')), count(b.bug_when) "+
							"from bugs a, comment b " +
							"where a.bug_id = b.bugid " +
							"and trim(' ' from replace(a.product, '\n', '')) = '"+product+"' " +
							"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
							"group by a.bug_id, b.who " +
							"order by b.who, a.bug_id;"
							);
		
		while(rs.next())
		{
			bug_id.add(rs.getString("(trim(' ' from replace(a.bug_id, '\n', '')))"));
			dev_email.add(rs.getString("trim(' ' from replace(b.who, '\n', ''))"));
			numOfComments.add(rs.getInt("count(b.bug_when)"));
		}
		
		
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss"); 
		fileName = "BugsByDevelopersMatrix-"+product+"-"+df.format(new Date())+".csv";
		fileContent = nb.bugsByDevs(distinctDev_email, distinctBug_id, dev_email, bug_id, numOfComments);
	}
	
	public void generateDevsByDevs(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> developers = new ArrayList<String>();
		ArrayList<String> developers2= new ArrayList<String>();
		ArrayList<String> developers3= new ArrayList<String>();
		ArrayList<Integer> edges     = new ArrayList<Integer>();
		
		
		System.out.println("");
		System.out.println("Retrieving the Developer's E-Mail Addresses...");
		
		s = con.createStatement(); //Statements to issue sql queries
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(b.who, '\n', ''))) \"who\""+
				"from bugs c, comment b " +
				"where c.bug_id = b.bugid " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s')) between '"+startDate+"' and '"+endDate+"' "  +
				"and trim(' ' from product) like '%"+product+"\n' " +
				"order by who;"
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			developers.add(rs.getString("who"));
		}
		
		System.out.println("Building the Developer Communication Network...");
		
		rs = s.executeQuery(
				"select trim(' ' from replace(a.who, '\n', '')), count(distinct(a.bugid)), trim(' ' from replace(b.who, '\n', '')) " +
						"from comment a, comment b " +
						"where a.bugid IN " +
						"(" +
							"select b.bugid " +
							"from bugs c, comment b " +
							"where c.bug_id = b.bugid " +
							"and trim(' ' from c.product) like '%"+product+"\n' "+
						") " +
						"and a.who <> b.who " +
						"and a.bugid = b.bugid "+
						"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
						"group by a.who, b.who " +
						"order by trim(' ' from replace(a.who, '\n', ''));"
						//Query to find how many times a developer work with another developer on the bugs of a particular component
				);
		
		while(rs.next())
		{
			developers2.add(rs.getString("trim(' ' from replace(a.who, '\n', ''))"));
			developers3.add(rs.getString("trim(' ' from replace(b.who, '\n', ''))"));
			edges.add((rs.getInt("count(distinct(a.bugid))")));
		}
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss");
		fileName = "DevsByDevsMatrix-"+product+"-"+df.format(new Date())+".csv";
		fileContent = nb.devsByDevs(developers, developers2, developers3, edges);
		
	}
	
	public void closeConnection() throws Exception
	{
		rs.close(); //close connections
		s.close();
		con.close();
	}


}
