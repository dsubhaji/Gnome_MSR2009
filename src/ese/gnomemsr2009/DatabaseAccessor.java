package ese.gnomemsr2009;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class DatabaseAccessor 
{
	Connection con;
	ResultSet rs;
	Statement s;
	
	private ArrayList<String> developers;
	private ArrayList<String> developers2;
	private ArrayList<String> developers3;
	private ArrayList<Integer> edges;
	private int num;

	public DatabaseAccessor()
	{
		developers = new ArrayList<String>();
		developers2 = new ArrayList<String>();
		developers3 = new ArrayList<String>();
		edges = new ArrayList<Integer>();
		num = 0;
	}
	
	public ArrayList<String> getDevelopers()
	{
		return developers;
	}
	
	public ArrayList<String> getDevelopers2()
	{
		return developers2;
	}
	
	public ArrayList<String> getDevelopers3()
	{
		return developers3;
	}
	
	public ArrayList<Integer> getEdges()
	{
		return edges;
	}
	
	public int getNum()
	{
		return num;
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
		
	}
	
	public String generateCSV() throws Exception
	{
		s = con.createStatement(); //Statements to issue sql queries
		rs = s.executeQuery("select distinct(trim(' ' from replace(a.product, '\n', ''))), count(distinct(b.bugid)), count(b.text), count(distinct(b.who)), MIN(trim(' ' from replace(b.bug_when, '\n', ''))), MAX(trim(' ' from replace(b.bug_when, '\n', ''))) "
							+"from bugs a, comment b "
							+"where a.bug_id = b.bugid "
							+"group by a.product "
							);
		
		StringBuilder csv = new StringBuilder();
		csv.append("\"Name of Component\", \"Number of Bugs\", \"Total Number of Comments\", \"No. Of Distinct Developers\", \"Date of First Comment\", \"Date of Last Comment\"\n");
		
		while(rs.next())
		{
			csv.append("\""+rs.getString("(trim(' ' from replace(a.product, '\n', '')))")+"\", ");
			csv.append(rs.getInt("count(distinct(b.bugid))") + ",");
			csv.append(rs.getInt("count(b.text)") + ", ");
			csv.append(rs.getInt("count(distinct(b.who))")+ ", ");
			csv.append("\""+rs.getString("MIN(trim(' ' from replace(b.bug_when, '\n', '')))")+"\", ");
			csv.append("\""+rs.getString("MAX(trim(' ' from replace(b.bug_when, '\n', '')))")+"\"\n");
		}
		
		return csv.toString();
	}
	
	public void closeConnection() throws Exception
	{
		rs.close(); //close connections
		s.close();
		con.close();
	}


}
