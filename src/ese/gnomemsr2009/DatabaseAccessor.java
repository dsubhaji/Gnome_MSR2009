package ese.gnomemsr2009;

import java.io.File;
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
	
	private String fileContent;
	private String fileName;
	private ArrayList<String> developers;
	private ArrayList<String> developers2;
	private ArrayList<String> developers3;
	private ArrayList<Integer> edges;
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
		
		int vertexNumber = 1;
		
		String dcn = "*Vertices " + num;
		int dev1 = 0;
		int dev2 = 0;
		int f = 0;
		int devSize = developers.size();
		int dev2Size= developers2.size();
		
		ArrayList<String> newDev2 = new ArrayList<String>();
		ArrayList<String> newDev3 = new ArrayList<String>();
		ArrayList<Integer> newEdges = new ArrayList<Integer>();
		
		for(int i = 0; i < dev2Size; i++)
		{
			String d = ""+developers2.get(i)+" "+developers3.get(i);
			if(newDev2.size() > 0)
			{
				for(int j = 0; j < newDev2.size(); j++)
				{
					String e = ""+newDev3.get(j)+" "+newDev2.get(j);
					if(d.equals(e))
					{
						f = 1;
					}
				}
				if(f == 0)
				{
					newDev2.add(developers2.get(i));
					newDev3.add(developers3.get(i));
					newEdges.add(edges.get(i));
					
				}
				f = 0;
			}else 
			{
				newDev2.add(developers2.get(i));
				newDev3.add(developers3.get(i));
				newEdges.add(edges.get(i));
			}
		}
		
		for(int i = 0; i<devSize;i++)
		{
			dcn = dcn + "\r\n" + vertexNumber + " \"" + developers.get(i) +"\"";
			vertexNumber++;
			//append the vertices to variable 'vertices'
		}
		
		dcn = dcn + "\r\n*Edges";
		
		for(int i = 0; i < newDev2.size(); i++)
		{
			for(int j = 0; j < devSize; j++)
			{
				if(newDev2.get(i).equals(developers.get(j)))
				{
					dev1 = j+1;
				}
				if(newDev3.get(i).equals(developers.get(j)))
				{
					dev2 = j+1;
				}
			}
			
			if((dev1 > 0) && (dev2 > 0))
			{
				dcn = dcn + "\r\n" + dev1 + "\t" + dev2 + "\t" + newEdges.get(i);
			}
		}
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss");
		fileName = "DCN-gnomemsr2009-"+product+"-"+df.format(new Date())+".net";
		fileContent = dcn;
		
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
	
	public void generateMatrix(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> distinctBug_id 			= new ArrayList<String>();
		ArrayList<String> distinctDev_email 		= new ArrayList<String>();
		ArrayList<String> bug_id 			= new ArrayList<String>();
		ArrayList<String> dev_email 		= new ArrayList<String>();
		ArrayList<Integer> numOfComments 	= new ArrayList<Integer>();
		
		System.out.println("Extracting Data from Database...");
		
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
		
		StringBuilder matrix = new StringBuilder();
		
		System.out.println("Building BugsByDevelopers Matrix...");
		
		matrix.append("bug_id, ");
		
		for(int i = 0; i < distinctDev_email.size(); i++)
		{
			matrix.append(distinctDev_email.get(i));
			matrix.append(", ");
		}
		
		for(int i = 0; i < distinctBug_id.size(); i++)
		{
			matrix.append("\n");
			matrix.append(distinctBug_id.get(i));
			matrix.append(", ");
			
			for(int j = 0; j < distinctDev_email.size(); j++)
			{
				for(int k = 0; k < dev_email.size(); k++)
				{
					if(	(bug_id.get(k).equals(distinctBug_id.get(i)))	&& (dev_email.get(k).equals(distinctDev_email.get(j))))
					{
						matrix.append(numOfComments.get(k).toString());
						matrix.append(" ");
					}
					
				}
				
				matrix.append(", ");
			}
			
		}
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss"); 
		fileName = "BugsByDevelopersMatrix-"+product+"-"+df.format(new Date())+".csv";
		fileContent = matrix.toString();
	}
	
	public void closeConnection() throws Exception
	{
		rs.close(); //close connections
		s.close();
		con.close();
	}


}
