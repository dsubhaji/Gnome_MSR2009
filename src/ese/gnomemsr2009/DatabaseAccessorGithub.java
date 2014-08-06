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
import java.util.Locale;

public class DatabaseAccessorGithub 
{
	private Connection con;
	private ResultSet rs;
	private Statement s ;
	
	private NetworkBuilder nb = new NetworkBuilder();
	
	private String fileContent;
	private String fileName;
	
	public DatabaseAccessorGithub()
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
			s = con.createStatement(); //Statements to issue sql queries
			
		} catch (SQLException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	public void generateDCN(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> developers = new ArrayList<String>();
		ArrayList<String> developers2= new ArrayList<String>();
		ArrayList<String> developers3= new ArrayList<String>();
		ArrayList<Integer> edges     = new ArrayList<Integer>();
		int num = 0;
		
		System.out.println("");
		System.out.println("Calculating the Total Number of Distinct Developers...");

		rs = s.executeQuery(
				"select count(distinct(a.user_id)) 'who'" +
				"from pull_request_comments a, commits b, projects c " +
				"where a.commit_id = b.id " +
				"and b.project_id = c.id " +
				"and c.name like '"+product+"' " +
				"and a.created_at between '"+startDate+"' and '"+endDate+"' ;" 
				); //ResultSet gets Query results. Query to find out the total number of distinct developers commenting on the bugs of a specific product
		
		while(rs.next())
		{
			num = rs.getInt("who");
		}
		
		System.out.println("Retrieving the Developer's E-Mail Addresses...");
		
		rs = s.executeQuery(
				"select distinct(d.login) 'who' " +
				"from pull_request_comments a, commits b, projects c, users d " +
				"where a.commit_id = b.id " +
				"and b.project_id = c.id " +
				"and a.user_id = d.id " +
				"and c.name like '"+product+"' " +
				"and a.created_at between '"+startDate+"' and '"+endDate+"' " +
				"order by who; "
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			developers.add(rs.getString("who"));
		}
		
		System.out.println("Building the Developer Communication Network...");
		
		rs = s.executeQuery(
				"select a.user1, count(distinct(b.pid2)), b.user2 " +
				"from (select a.pull_request_id 'pid1', b.login 'user1', a.commit_id 'cid1', a.created_at from pull_request_comments a, users b where a.user_id = b.id) a, (select a.pull_request_id 'pid2', b.login 'user2', a.commit_id 'cid2', a.created_at from pull_request_comments a, users b where a.user_id = b.id) b, commits c, projects d " +
				"where user1 <> user2 " +
				"and a.pid1 = b.pid2 " +
				"and a.cid1 = c.id " +
				"and c.project_id = d.id " +
				"and d.name like '"+product+"' " +
				"and a.created_at between '"+startDate+"' and '"+endDate+"' " +
				"group by a.user1, b.user2 " +
				"order by a.user1; "
				);
		
		while(rs.next())
		{
			developers2.add(rs.getString("a.user1"));
			developers3.add(rs.getString("b.user2"));
			edges.add((rs.getInt("count(distinct(b.pid2))")));
		}
		
		//System.out.println(developers + "\n" + developers2 + "\n" + developers3 + "\n" + edges + "\n" );
		
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss");
		fileName = "DCN-gnomemsr2009-"+product+"-"+df.format(new Date())+".net";
		fileContent = nb.networkBuilder(developers, developers2, developers3, edges, num);
		
	}
	
	public void generateDevsByDevs(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> developers = new ArrayList<String>();
		ArrayList<String> developers2= new ArrayList<String>();
		ArrayList<String> developers3= new ArrayList<String>();
		ArrayList<Integer> edges     = new ArrayList<Integer>();
		
		
		System.out.println("");
		System.out.println("Retrieving the Developer's E-Mail Addresses...");
		
		rs = s.executeQuery(
				"select distinct(d.login) 'who'" +
				"from pull_request_comments a, commits b, projects c, users d " +
				"where a.commit_id = b.id " +
				"and b.project_id = c.id " +
				"and a.user_id = d.id " +
				"and c.name = '"+product+"' " +
				"and a.created_at between '"+startDate+"' and '"+endDate+"' ;" 
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			developers.add(rs.getString("who"));
		}
		
		System.out.println("Building the Developer Communication Network...");
		
		rs = s.executeQuery(
				"select a.user1, count(distinct(b.pid2)), b.user2 " +
						"from (select a.pull_request_id 'pid1', b.login 'user1', a.commit_id 'cid1', a.created_at from pull_request_comments a, users b where a.user_id = b.id) a, (select a.pull_request_id 'pid2', b.login 'user2', a.commit_id 'cid2', a.created_at from pull_request_comments a, users b where a.user_id = b.id) b, commits c, projects d " +
						"where user1 <> user2 " +
						"and a.pid1 = b.pid2 " +
						"and a.cid1 = c.id " +
						"and c.project_id = d.id " +
						"and d.name like '"+product+"' " +
						"and a.created_at between '"+startDate+"' and '"+endDate+"' " +
						"group by a.user1, b.user2 " +
						"order by a.user1; "
						//Query to find how many times a developer work with another developer on the bugs of a particular component
				);
		
		while(rs.next())
		{
			developers2.add(rs.getString("a.user1"));
			developers3.add(rs.getString("b.user2"));
			edges.add((rs.getInt("count(distinct(b.pid2))")));
		}
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss");
		fileName = "DevsByDevsMatrix-"+product+"-"+df.format(new Date())+".csv";
		fileContent = nb.devsByDevs(developers, developers2, developers3, edges);
		
	}
	
	public void generateBugsByDev(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> distinctBug_id 			= new ArrayList<String>();
		ArrayList<String> distinctDev_email 		= new ArrayList<String>();
		ArrayList<String> bug_id 			= new ArrayList<String>();
		ArrayList<String> dev_email 		= new ArrayList<String>();
		ArrayList<Integer> numOfComments 	= new ArrayList<Integer>();
		
		System.out.println("\nExtracting Data from Database...");
		
		rs = s.executeQuery(
							"select distinct(a.pull_request_id) 'id'" +
							"from pull_request_comments a, commits b, projects c " +
							"where a.commit_id = b.id " +
							"and b.project_id = c.id " +
							"and a.created_at between '"+startDate+"' and '"+endDate+"' " +
							"and c.name = '"+product+"';"
							);
		
		while(rs.next())
		{
			distinctBug_id.add(rs.getString("id"));
		}
		
		rs = s.executeQuery(
							"select distinct(d.login) 'who'" +
							"from pull_request_comments a, commits b, projects c, users d " +
							"where a.commit_id = b.id " +
							"and b.project_id = c.id " +
							"and a.user_id = d.id " +
							"and c.name = '"+product+"' " +
							"and a.created_at between '"+startDate+"' and '"+endDate+"' ;" 
							); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			distinctDev_email.add(rs.getString("who"));
		}
		
		rs = s.executeQuery(
							"select distinct(a.pull_request_id) 'id', d.login, count(distinct(a.comment_id)) 'count' " +
							"from pull_request_comments a, commits b, projects c, users d " +
							"where a.commit_id = b.id " +
							"and b.project_id = c.id " +
							"and a.user_id = d.id " +
							"and c.name = '"+product+"' " +
							"and a.created_at between '"+startDate+"' and '"+endDate+"' " +
							"group by d.login, a.pull_request_id; " 
							);
		
		while(rs.next())
		{
			bug_id.add(rs.getString("id"));
			dev_email.add(rs.getString("d.login"));
			numOfComments.add(rs.getInt("count"));
		}
		
		
		
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss"); 
		fileName = "BugsByDevelopersMatrix-"+product+"-"+df.format(new Date())+".csv";
		fileContent = nb.bugsByDevsGithub(distinctDev_email, distinctBug_id, dev_email, bug_id, numOfComments);
	}
	
	public void projectSummary(ArrayList<String> productName, String dirName) throws Exception
	{
		ArrayList<String> projectID 		= new ArrayList<String>();
		ArrayList<String> parametersOne		= new ArrayList<String>();
		//project name, url, language, time created, forked from, number of members and number of watchers
		ArrayList<String> parametersTwo		= new ArrayList<String>();
		//number of issues and number of comments on issues
		ArrayList<String> parametersThree	= new ArrayList<String>();
		//number of commits and number of comments of commits
		ArrayList<String> parametersFour	= new ArrayList<String>();
		//number of pull requests and number of comments on pull requests
		int arrayLength = productName.size();
		String inStatement = "";
		
		for(int i = 0; i < arrayLength; i++)
		{
			if(i == 0)
				inStatement = inStatement + "WHERE a.name like '"+productName.get(i).trim()+"'";
			if(i == arrayLength - 1)
				inStatement = inStatement + " OR a.name like '"+productName.get(i).trim()+"' ";
			else
				inStatement = inStatement + " OR a.name like '"+productName.get(i).trim()+"'";
		}
		
		System.out.println("\nExtracting Data from Database...");
		
		rs = s.executeQuery("select a.id, a.name, a.url, IFNULL(a.language, 'NA') 'language', a.created_at, IFNULL(a.forked_from, 'NA') 'forked_from', count(distinct(c.user_id)) 'no_of_members', count(b.user_id) 'no_of_watchers' "
							+"from projects a "
							+"LEFT JOIN watchers b ON a.id = b.repo_id "
							+"LEFT JOIN project_members c ON c.repo_id = a.id "
							+ inStatement
							+"group by a.id "
							+"order by a.id;"
							);
		
		while(rs.next())
		{
			projectID.add(rs.getString("a.id"));
			String parameters = rs.getString("a.name") + ", "
					+ rs.getString("a.url") + ", "
					+ rs.getString("language") + ", "
					+ rs.getString("a.created_at") + ", "
					+ rs.getString("forked_from") + ", "
					+ rs.getString("no_of_members") + ", "
					+ rs.getString("no_of_watchers") + ", ";
			parametersOne.add(parameters);
		}
		
		rs = s.executeQuery("select a.id, a.name, count(distinct(c.id)) 'no_of_issues', count(distinct(d.comment_id)) 'no_of_comments_on_issues' "
				+"from projects a "
				+"LEFT JOIN issues c ON c.repo_id = a.id "
				+"LEFT JOIN issue_comments d ON c.id = d.issue_id "
				+ inStatement
				+"group by a.id "
				+"order by a.id;"
				);

		while(rs.next())
		{
			String parameters = rs.getString("no_of_issues") + ", "
					+ rs.getString("no_of_comments_on_issues") + ", ";
			
			parametersTwo.add(parameters);
		}
		
		rs = s.executeQuery("select a.id, a.name, count(distinct(c.id)) 'no_of_commits', count(distinct(d.comment_id)) 'no_of_comments_on_commits' "
				+"from projects a "
				+"LEFT JOIN commits c ON c.project_id = a.id "
				+"LEFT JOIN commit_comments d ON c.id = d.commit_id "
				+ inStatement
				+"group by a.id "
				+"order by a.id;"
				);

		while(rs.next())
		{
			String parameters = rs.getString("no_of_commits") + ", "
					+ rs.getString("no_of_comments_on_commits") + ", ";
			
			parametersThree.add(parameters);
		}
		
		rs = s.executeQuery("select a.id, a.name, count(distinct(c.id)) 'no_of_pull_requests', count(distinct(d.comment_id)) 'no_of_comments_on_pull_requests' "
				+"from projects a "
				+"LEFT JOIN pull_requests c ON c.head_repo_id = a.id "
				+"LEFT JOIN pull_request_comments d ON c.id = d.pull_request_id "
				+ inStatement
				+"group by a.id "
				+"order by a.id;"
				);

		while(rs.next())
		{
			String parameters = rs.getString("no_of_pull_requests") + ", "
					+ rs.getString("no_of_comments_on_pull_requests") + "";
			
			parametersFour.add(parameters);
		}
		StringBuilder csv = new StringBuilder();
		csv.append("\"Project ID\", \"Project Name\", \"URL\", \"Language\", "
				+ "\"Created At\", \"Forked From\", \"Number of Members\", \"Number of Watchers\", "
				+ "\"Number of Issues\", \"Number of Comments on Issues\", "
				+ "\"Number of Commits\", \"Number of Comments on Commits\", "
				+ "\"Number of Pull Requests\", \"Number of Comments on Pull Requests\"\n");
		
		System.out.println("Generating .CSV File");
		
		for(int i = 0; i < projectID.size(); i++)
		{
			csv.append(projectID.get(i) + ", ");
			csv.append(parametersOne.get(i));
			csv.append(parametersTwo.get(i));
			csv.append(parametersThree.get(i));
			csv.append(parametersFour.get(i) + "\n");
		}
		
		fileContent = csv.toString();
	}
}