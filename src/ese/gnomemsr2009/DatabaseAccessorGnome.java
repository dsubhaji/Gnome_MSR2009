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


public class DatabaseAccessorGnome 
{
	Connection con;
	ResultSet rs, rs2;
	Statement s ;
	Statement s2;
	
	private NetworkBuilder nb = new NetworkBuilder();
	
	
	private String fileContent;
	private String fileName;
	private String dbName;
	
	private int num;

	public DatabaseAccessorGnome()
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
	
	public String getDBName()
	{
		return dbName;
	}
	
	
	public boolean openConnection(String databaseName, String mysqlUser, String password) throws Exception
	{
		dbName = databaseName;
		Class.forName("com.mysql.jdbc.Driver"); //load mysql driver
		try
		{
			con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/" + databaseName + "?user=" + mysqlUser + "&password=" + password); //set-up connection with database
			s = con.createStatement(); //Statements to issue sql queries
			s2 = con.createStatement();
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
		
		
		System.out.println("");
		System.out.println("Calculating the Total Number of Distinct Developers...");

		rs = s.executeQuery(
				"select count(distinct(b.who)) "+
				"from bugs c, comment b " +
				"where c.bug_id = b.bugid " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"' ) "+
				"and trim(' ' from c.product) like '%"+product+"\n';"
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
				"and trim(' ' from c.product) like '%"+product+"\n' " +
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
	
	public void generateOwnersDCN(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> developers = new ArrayList<String>();
		ArrayList<String> developers2= new ArrayList<String>();
		ArrayList<String> developers3= new ArrayList<String>();
		ArrayList<Integer> edges     = new ArrayList<Integer>();
		
		
		System.out.println("");
		System.out.println("Calculating the Total Number of Distinct Developers...");

		rs = s.executeQuery(
				"select count(distinct(assigned_to)) "+
				"from bugs " +
				"where (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and trim(' ' from product) like '%"+product+"\n';"
				); //ResultSet gets Query results. Query to find out the total number of distinct developers commenting on the bugs of a specific product
		
		while(rs.next())
		{
			num = rs.getInt("count(distinct(assigned_to))");
		}
		
		System.out.println("Retrieving the Developer's E-Mail Addresses...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(assigned_to, '\n', ''))) \"who\""+
				"from bugs " +
				"where (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and trim(' ' from product) like '%"+product+"\n' " +
				"order by who;"
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			developers.add(rs.getString("who"));
		}
		
		String in = "(";
		for (int i = 0; i < developers.size(); i++)
		{
			if(i==developers.size()-1)
				in = in +"'" +developers.get(i)+ "') ";
			else
				in = in +"'" +developers.get(i)+ "', ";
		}
		//System.out.println(in);
		System.out.println("Building the Developer Communication Network...");
		
		rs = s.executeQuery(
				"select trim(' ' from replace(a.who, '\n', '')), count(distinct(a.bugid)), trim(' ' from replace(b.who, '\n', '')) " +
						"from comment a, comment b, bugs c " +
						"where trim(' ' from replace(c.product, '\n', '')) like '"+product+"' " +
						"and a.who <> b.who " +
						"and a.bugid = b.bugid "+
						"and a.bugid = c.bug_id "+
						"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
						"and trim(' ' from replace(a.who, '\n', '')) IN " + in +
						"and trim(' ' from replace(b.who, '\n', '')) IN " + in +
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
		
		rs = s.executeQuery("select distinct(trim(' ' from replace(a.product, '\n', ''))), count(distinct(b.bugid)), count(b.bug_when), count(distinct(b.who)), MIN(trim(' ' from replace(b.bug_when, '\n', ''))), MAX(trim(' ' from replace(b.bug_when, '\n', ''))) "
							+"from bugs a, comment b "
							+"where a.bug_id = b.bugid "
							+"group by a.product "
							);
		
		StringBuilder csv = new StringBuilder();
		csv.append("\"Name of Component\", \"Number of Bugs\", \"Total Number of Comments\", \"No. Of Distinct Developers\", \"Date of First Comment\", \"Date of Last Comment\", \"Time Elapsed(Days)\", \"Time Elapsed(Hours)\", \"Time Elapsed(Minutes)\"\n");
		
		System.out.println("Generating .CSV File");
		while(rs.next())
		{
			csv.append("\""+rs.getString("(trim(' ' from replace(a.product, '\n', '')))")+"\", ");
			csv.append(rs.getInt("count(distinct(b.bugid))") + ",");
			csv.append(rs.getInt("count(b.bug_when)") + ", ");
			csv.append(rs.getInt("count(distinct(b.who))")+ ", ");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d H:mm:ss", Locale.ENGLISH);
			
			String minDate = rs.getString("MIN(trim(' ' from replace(b.bug_when, '\n', '')))");
			String maxDate = rs.getString("MAX(trim(' ' from replace(b.bug_when, '\n', '')))");
			
			Date dateMin = sdf.parse(minDate);
			Date dateMax = sdf.parse(maxDate);
			
			csv.append("\""+minDate+"\", ");
			csv.append("\""+maxDate+"\", ");
			
			float differenceInTime = dateMax.getTime() - dateMin.getTime(); //elapsed time in millisecond
			float days = (((differenceInTime/1000)/3600)/24); //elapsed time in days
			float hours = ((differenceInTime/1000)/3600); //elapsed time in hours
			float minutes = ((differenceInTime/1000)/60); //elapsed time in minutes
			
			csv.append(days+", ");
			csv.append(hours+", ");
			csv.append(minutes+"\n ");
			
		}
		
		fileName = "ProjectDataSummary.csv";
		fileContent = csv.toString();
	}
	
	public void generateCSV(ArrayList<String> productName, String dirName) throws Exception
	{
		ArrayList<String> elapsedDays 		= new ArrayList<String>();
		ArrayList<String> elapsedHours 		= new ArrayList<String>();
		ArrayList<String> elapsedMinutes	= new ArrayList<String>();
		
		ArrayList<String> firstComment		= new ArrayList<String>();
		ArrayList<String> lastComment		= new ArrayList<String>();
		
		ArrayList<String> productName2 		= new ArrayList<String>();
		ArrayList<String> numOfBugs 		= new ArrayList<String>();
		ArrayList<String> numOfComments 	= new ArrayList<String>();
		ArrayList<String> numOfDevs			= new ArrayList<String>();
		ArrayList<String> numOfOwners		= new ArrayList<String>();
		
		ArrayList<String> medianElapsedTime	= new ArrayList<String>();
		ArrayList<String> avgElapsedTime	= new ArrayList<String>();
		ArrayList<String> everythingElse	= new ArrayList<String>();
		//everythingElse includes cent.Degree, cent.Betweenness, cent.closeness, cent.EVcent, transitivity.global, assortativity, diameter, density, modularity, avg.PathLength, and avg.Degree
		
		int arrayLength = productName.size();
		String inStatement = "";
		
		RFunctions rf = Controller.rf;
		
		for(int i = 0; i < arrayLength; i++)
		{
			if(i == 0)
				inStatement = inStatement + "AND (trim(' ' from replace(a.product, '\n', '')) like '"+productName.get(i).trim()+"'";
			if(i == arrayLength - 1)
				inStatement = inStatement + " OR trim(' ' from replace(a.product, '\n', '')) like '"+productName.get(i).trim()+"') ";
			else
				inStatement = inStatement + " OR trim(' ' from replace(a.product, '\n', '')) like '"+productName.get(i).trim()+"'";
		}
		
		System.out.println("\nExtracting Data from Database...");
		
		rs = s.executeQuery("select distinct(trim(' ' from replace(a.product, '\n', ''))), count(distinct(b.bugid)), count(b.bug_when), count(distinct(b.who)), MIN(trim(' ' from replace(b.bug_when, '\n', ''))), MAX(trim(' ' from replace(b.bug_when, '\n', ''))), count(distinct(a.assigned_to)) "
							+"from bugs a, comment b "
							+"where a.bug_id = b.bugid "
							+ inStatement
							+"group by a.product "
							);
		
		
		while(rs.next())
		{
			String product = rs.getString("(trim(' ' from replace(a.product, '\n', '')))");
			
			productName2.add("\"" + product +"\"");
			numOfBugs.add("" + rs.getInt("count(distinct(b.bugid))"));
			numOfComments.add("" + rs.getInt("count(b.bug_when)"));
			numOfDevs.add("" + rs.getInt("count(distinct(b.who))"));
			numOfOwners.add("" + rs.getInt("count(distinct(a.assigned_to))"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d H:mm:ss", Locale.ENGLISH);
			
			String minDate = rs.getString("MIN(trim(' ' from replace(b.bug_when, '\n', '')))");
			String maxDate = rs.getString("MAX(trim(' ' from replace(b.bug_when, '\n', '')))");
			
			Date dateMin = sdf.parse(minDate);
			Date dateMax = sdf.parse(maxDate);
			
			firstComment.add("\""+minDate+"\"");
			lastComment.add("\""+maxDate+"\"");
			
			float differenceInTime = dateMax.getTime() - dateMin.getTime(); //elapsed time in millisecond
			elapsedDays.add("" + (float)(((differenceInTime/1000)/3600)/24)); //elapsed time in days
			elapsedHours.add("" + (float)((differenceInTime/1000)/3600)); //elapsed time in hours
			elapsedMinutes.add("" + (float)((differenceInTime/1000)/60)); //elapsed time in minutes
			
			System.out.println("\nCalculating Median Elapsed Time of: " + product + "\n");
			
			rs2 = s2.executeQuery(
							"select timestampdiff(second, a.creation_ts, a.delta_ts)/3600 'elapsed_time' " +
							"from bugs a " +
							"where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
							"order by timestampdiff(second, a.creation_ts, a.delta_ts)/3600; "
								);
			
			float elTime = 0.0f;
			ArrayList<Float> elapsedTime 			= new ArrayList<Float>();
			
			while(rs2.next())
			{
				elTime = elTime + rs2.getFloat("elapsed_time");
				
				elapsedTime.add(elTime);
			}
			
			if(elTime == 0.0f)
			{
				elapsedTime.add(elTime);
			}
			
			//find the median of the elapsed time
			int mid = elapsedTime.size()/2; 
			float median = elapsedTime.get(mid); 
			
			if (elapsedTime.size()%2 == 0) 
			{ 
				median = (median + elapsedTime.get(mid-1))/2; 
			}
			
			medianElapsedTime.add(""+median);
			
			System.out.println("\nCalculating Average Elapsed Time of: " + product + "\n");
			
			rs2 = s2.executeQuery(
					"select a.assigned_to,  avg(timestampdiff(second, a.creation_ts, a.delta_ts)/3600) as avgElapsedTime " +
					"from bugs a " +
					"where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
					"group by a.assigned_to " +
					"order by a.assigned_to; "
					); //Query to find the distinct developers working on the bugs
			
			while(rs2.next())
			{
				avgElapsedTime.add(rs2.getString("avgElapsedTime"));
			}
			
			System.out.println("\nCalculating Network Metrics of: " + product + "\n");
			
			everythingElse.add(rf.summaryMetrics(dirName, product));
		}
		
		//StringBuilder start
		StringBuilder csv = new StringBuilder();
		csv.append("\"Name of Component\", \"Number of Bugs\", \"Total Number of Comments\", \"No. Of Distinct Developers\", "
				+ "\"Date of First Comment\", \"Date of Last Comment\", \"Time Elapsed(Days)\", \"Time Elapsed(Hours)\", \"Time Elapsed(Minutes)\", "
				+ "\"Number of Owners\", \"Degree\", \"Betweenness\", \"Closeness\", \"EVCent\", \"Transitivity(global)\", "
				+ "\"Assortativity\", \"Diameter\", \"Density\", \"Modularity\", \"Avg. Path Length\", \"Avg. Degree\", "
				+ "\"Avg. Elapsed Time\", \"Median Elapsed Time\"\n");
		
		System.out.println("Generating .CSV File");
		
		for(int i = 0; i < productName2.size(); i++)
		{
			csv.append(productName2.get(i) + ", ");
			csv.append(numOfBugs.get(i) + ", ");
			csv.append(numOfComments.get(i) + ", ");
			csv.append(numOfDevs.get(i) + ", ");
			
			csv.append(firstComment.get(i) + ", ");
			csv.append(lastComment.get(i) + ", ");
			csv.append(elapsedDays.get(i) + ", ");
			csv.append(elapsedHours.get(i) + ", ");
			csv.append(elapsedMinutes.get(i) + ", ");
			
			csv.append(numOfOwners.get(i) + ", ");
			csv.append(everythingElse.get(i) + ", ");
			
			csv.append(avgElapsedTime.get(i) + ", ");
			csv.append(medianElapsedTime.get(i) + "\n");
		}
		
		fileName = "ProjectDataSummary.csv";
		fileContent = csv.toString();
	}
	
	
	public void generateBugsByOwners(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> distinctBug_id 			= new ArrayList<String>();
		ArrayList<String> distinctDev_email 		= new ArrayList<String>();
		ArrayList<String> bug_id 			= new ArrayList<String>();
		ArrayList<String> dev_email 		= new ArrayList<String>();
		ArrayList<Integer> numOfComments 	= new ArrayList<Integer>();
		
		System.out.println("\nExtracting Data from Database...");
		
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
							"select distinct(trim(' ' from replace(assigned_to, '\n', ''))) \"who\" "+
							"from bugs " +
							"where (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
							"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
							"and trim(' ' from product) like '%"+product+"\n' " +
							"order by who;"
							);
		
		while(rs.next())
		{
			distinctDev_email.add(rs.getString("who"));
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
	
	public void generateBugsByDev(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> distinctBug_id 			= new ArrayList<String>();
		ArrayList<String> distinctDev_email 		= new ArrayList<String>();
		ArrayList<String> bug_id 			= new ArrayList<String>();
		ArrayList<String> dev_email 		= new ArrayList<String>();
		ArrayList<Integer> numOfComments 	= new ArrayList<Integer>();
		
		System.out.println("\nExtracting Data from Database...");
		
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
	
	public void generateOwnersByOwners(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> developers = new ArrayList<String>();
		ArrayList<String> developers2= new ArrayList<String>();
		ArrayList<String> developers3= new ArrayList<String>();
		ArrayList<Integer> edges     = new ArrayList<Integer>();
		
		
		System.out.println("");
		System.out.println("Retrieving the Developer's E-Mail Addresses...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(assigned_to, '\n', ''))) \"who\" "+
				"from bugs " +
				"where (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
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
	
	
	public void generateBugModel(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> bug_id	 	= new ArrayList<String>();
		ArrayList<String> owner		 	= new ArrayList<String>();
		ArrayList<String> elapsedTime	= new ArrayList<String>();
		ArrayList<String> component	 	= new ArrayList<String>();
		ArrayList<String> version	 	= new ArrayList<String>();
		ArrayList<String> repPlatform	= new ArrayList<String>();
		ArrayList<String> op_sys	 	= new ArrayList<String>();
		ArrayList<String> bug_status 	= new ArrayList<String>();
		ArrayList<String> resolution 	= new ArrayList<String>();
		ArrayList<String> priority   	= new ArrayList<String>();
		ArrayList<String> bugSeverity	= new ArrayList<String>();
		ArrayList<String> tgtMilestone	= new ArrayList<String>();
		ArrayList<String> dupeOf		= new ArrayList<String>();
		
		ArrayList<String> bug_id2		= new ArrayList<String>();
		ArrayList<String> activityLevel	= new ArrayList<String>();
		
		ArrayList<String> bug_id3		= new ArrayList<String>();
		ArrayList<String> totalComments	= new ArrayList<String>();
		ArrayList<String> numOfDevs		= new ArrayList<String>();
		ArrayList<String> interestSpan	= new ArrayList<String>();
		
		ArrayList<String> bug_id4		= new ArrayList<String>();
		ArrayList<String> ownerComments	= new ArrayList<String>();
		ArrayList<String> owner2		= new ArrayList<String>();
		
		ArrayList<String> owner4		= new ArrayList<String>();
		ArrayList<String> ownerWorkload	= new ArrayList<String>();
		
		ArrayList<String> ownerComArc	= new ArrayList<String>();
		ArrayList<String> owner3		= new ArrayList<String>();
		
		
		System.out.println("");
		System.out.println("Retrieving Data from 'Bugs' Table...");
		
		rs = s.executeQuery(
				"select a.bug_id, trim(' ' from replace(a.assigned_to, '\n', '')) \"owner\", timestampdiff(second, a.creation_ts, a.delta_ts)/3600 \"ElapsedTime\", trim(' ' from replace(a.Component, '\n', '')), trim(' ' from replace(a.Version, '\n', '')), trim(' ' from replace(a.Rep_Platform, '\n', '')), trim(' ' from replace(a.Op_Sys, '\n', '')), trim(' ' from replace(a.Bug_Status, '\n', '')), trim(' ' from replace(a.Resolution, '\n', '')), trim(' ' from replace(a.Priority, '\n', '')), trim(' ' from replace(a.Bug_Severity, '\n', '')), trim(' ' from replace(a.Target_Milestone, '\n', '')), trim(' ' from replace(a.duplicate_Of, '\n', '')) " +
				"from bugs a " +
				"where trim(' ' from replace(a.product, '\n', '')) like \""+product+"\" " +
				"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by a.bug_id " +
				"order by a.bug_id asc;"
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			bug_id.add(rs.getString("a.bug_id"));
			owner.add(rs.getString("owner"));
			elapsedTime.add(rs.getString("ElapsedTime"));
			component.add(rs.getString("trim(' ' from replace(a.Component, '\n', ''))"));
			version.add(rs.getString("trim(' ' from replace(a.version, '\n', ''))"));
			repPlatform.add(rs.getString("trim(' ' from replace(a.Rep_Platform, '\n', ''))"));
			op_sys.add(rs.getString("trim(' ' from replace(a.Op_Sys, '\n', ''))"));
			bug_status.add(rs.getString("trim(' ' from replace(a.bug_status, '\n', ''))"));
			resolution.add(rs.getString("trim(' ' from replace(a.resolution, '\n', ''))"));
			priority.add(rs.getString("trim(' ' from replace(a.priority, '\n', ''))"));
			bugSeverity.add(rs.getString("trim(' ' from replace(a.bug_severity, '\n', ''))"));
			tgtMilestone.add(rs.getString("trim(' ' from replace(a.target_milestone, '\n', ''))"));
			dupeOf.add(rs.getString("trim(' ' from replace(a.duplicate_Of, '\n', ''))"));
		}
		
		System.out.println("Retrieving Data from 'Activity' and 'Bugs' Table...");
		
		rs = s.executeQuery(
				"select COUNT(c.bug_when), a.bug_id " +
				"from activity c, bugs a " + 
				"where a.bug_id = c.bugid " +
				"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and c.bugid in " +
				"( select bug_id from bugs where trim(' ' from replace(a.product, '\n', '')) like \"" + product + "\"  )" +
				"group by a.bug_id " +
				"order by a.bug_id asc; "
				);
		
		while(rs.next())
		{
			bug_id2.add(rs.getString("a.bug_id"));
			activityLevel.add(rs.getString("COUNT(c.bug_when)"));
		}
		
		System.out.println("Retrieving Data from 'Comment' and 'Bugs' Table...");
		
		rs = s.executeQuery(
				"select b.bugid, count(b.text), count(distinct(b.who)), timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 " +
				"from bugs a, comment b " +
				"where a.bug_id = b.bugid " +
				"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and b.bugid in " +
				"(	select bug_id from bugs where trim(' ' from replace(a.product, '\n', '')) like \"" + product + "\"	)" +
				"group by b.bugid " +
				"order by b.bugid asc; " 
				);
		
		while(rs.next())
		{
			bug_id3.add(rs.getString("b.bugid"));
			totalComments.add(rs.getString("count(b.text)"));
			numOfDevs.add(rs.getString("count(distinct(b.who))"));
			interestSpan.add(rs.getString("timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600"));
		}
		
		System.out.println("Retrieving Owner Informations...");
		
		rs = s.executeQuery(
				"select count(text), trim(' ' from replace(who, '\n', '')), bugid " +
				"from comment " +
				"where bugid in "+
				"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like \"" + product + "\"	) " +
				"and trim(' ' from replace(who, '\n', '')) in " +
				"( select trim(' ' from replace(assigned_to, '\n', '')) from bugs where trim(' ' from replace(product, '\n', '')) like \"" + product + "\" ) " +
				"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +	
				"group by bugid, who " +
				"order by bugid;"
				);
		
		while(rs.next())
		{
			ownerComments.add(rs.getString("count(text)"));
			owner2.add(rs.getString("trim(' ' from replace(who, '\n', ''))"));
			bug_id4.add(rs.getString("bugid"));
		}
		
		rs = s.executeQuery(
				"select count(bug_id), trim(' ' from replace(assigned_to, '\n', '')) " + 
				"from bugs " +
				"where trim(' ' from replace(product, '\n', '')) like \"" + product + "\" " +
				"and (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by assigned_to;"
				);
		
		while(rs.next())
		{
			owner4.add(rs.getString("trim(' ' from replace(assigned_to, '\n', ''))"));
			ownerWorkload.add(rs.getString("count(bug_id)"));
		}
		
		rs = s.executeQuery(
				"select count(distinct(b.bugid)), trim(' ' from replace(b.who, '\n', '')) " +
				"from comment b " +
				"where b.bugid in " + 
				"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like \"" + product + "\"	) " +
				"and trim(' ' from replace(b.who, '\n', '')) in " + 
				"(	select trim(' ' from replace(assigned_to, '\n', '')) from bugs where trim(' ' from replace(product, '\n', '')) like \"" + product + "\"	) " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by who "
				);
		
		while(rs.next())
		{
			ownerComArc.add(rs.getString("count(distinct(b.bugid))"));
			owner3.add(rs.getString("trim(' ' from replace(b.who, '\n', ''))"));
		}
		
		
		StringBuilder matrix = new StringBuilder();
		
		//RFunctions rf = Controller.rf;
		
		//ArrayList<String> degNBetweenness = rf.rScript(fileContent, owner);
		//Column Headers
		//matrix.append("bug_id, owner, elapsed-time, component, version, rep-platform, op-sys, bug-status, resolution, priority, severity, target-milestone, duplicate, activity-level, number-of-comments, number-of-commenters, interest-span, number-of-comments-by-owner, owner-workload, owner-comment-arc, degree, betweenness, closeness, clustcoeff, eigencentrality, pagerank");
		matrix.append("bug_id, owner, elapsed-time, component, version, rep-platform, op-sys, bug-status, resolution, priority, severity, target-milestone, duplicate, activity-level, number-of-comments, number-of-commenters, interest-span, number-of-comments-by-owner, owner-workload, owner-comment-arc");
		matrix.append("\n");
		for(int i = 0; i < bug_id.size(); i++)
		{
			
			matrix.append(bug_id.get(i) + ", ");
			matrix.append(owner.get(i) + ", ");
			matrix.append(elapsedTime.get(i) + ", ");
			matrix.append(component.get(i) + ", ");
			matrix.append(version.get(i) + ", ");
			matrix.append(repPlatform.get(i) + ", ");
			matrix.append(op_sys.get(i) + ", ");
			matrix.append(bug_status.get(i) + ", ");
			matrix.append(resolution.get(i) + ", ");
			matrix.append(priority.get(i) + ", ");
			matrix.append(bugSeverity.get(i) + ", ");
			matrix.append(tgtMilestone.get(i) + ", ");
			matrix.append(dupeOf.get(i) + ", ");
			
			for(int j = 0; j < bug_id2.size(); j++)
			{
				if(bug_id.get(i).equals(bug_id2.get(j)))
				{
					matrix.append(activityLevel.get(j));
				}	
			}
			matrix.append(", ");
			
			for(int j = 0; j < bug_id3.size(); j++)
			{
				if(bug_id.get(i).equals(bug_id3.get(j)))
				{
					matrix.append(totalComments.get(j));
				}
			}
			matrix.append(", ");
			
			for(int j = 0; j < bug_id3.size(); j++)
			{
				if(bug_id.get(i).equals(bug_id3.get(j)))
				{
					matrix.append(numOfDevs.get(j));
				}
			}
			matrix.append(", ");
			
			for(int j = 0; j < bug_id3.size(); j++)
			{
				if(bug_id.get(i).equals(bug_id3.get(j)))
				{
					matrix.append(interestSpan.get(j));
				}
			}
			matrix.append(", ");
			
			for(int j = 0; j < owner2.size(); j++)
			{
				
				if(	(owner.get(i).equals(owner2.get(j)))	&& (bug_id.get(i).equals(bug_id4.get(j))))
				{
					matrix.append(ownerComments.get(j));
				}	
			}
			matrix.append(", ");
			
			for(int j = 0; j < owner4.size(); j++)
			{
				if(owner.get(i).equals(owner4.get(j)))
				{
					matrix.append(ownerWorkload.get(j));
				}
			}
			matrix.append(", ");
			
			for(int j = 0; j < owner3.size(); j++)
			{
				if(owner.get(i).equals(owner3.get(j)))
				{
					matrix.append(ownerComArc.get(j));
				}
			}
			//matrix.append(", ");
			
			//matrix.append(degNBetweenness.get(i));
			matrix.append("\n");
		}

		
		fileName = product+"-("+startDate+")-("+endDate+")-Bugs-Details.csv";
		fileContent = matrix.toString();
		System.out.println("");
		System.out.println("Generating .CSV File");
	}
	
	public void generateDevModel(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> owners		 		= new ArrayList<String>();
		ArrayList<String> bugsOwned		 		= new ArrayList<String>();
		ArrayList<String> assignedTo			= new ArrayList<String>();
		
		ArrayList<String> bugsCommented		 	= new ArrayList<String>();
		ArrayList<String> bugsCommentSpan	 	= new ArrayList<String>();
		
		ArrayList<String> commentsOnOwned		= new ArrayList<String>();
		ArrayList<String> commentsOffOwned	 	= new ArrayList<String>();
		
		ArrayList<String> noOfActivities	 	= new ArrayList<String>();
		
		ArrayList<String> avgElapsedTime	 	= new ArrayList<String>();
		ArrayList<String> medianElapsedTime		= new ArrayList<String>();
		
		ArrayList<String> avgInterestSpan	 	= new ArrayList<String>();
		ArrayList<String> medianInterestSpan	= new ArrayList<String>();
		
		
		System.out.println("");
		System.out.println("Finding the Number of Bugs Owned by Each Developers...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(a.assigned_to, '\n', ''))), count(a.bug_id) " +
				"from bugs a " +
				"where trim(' ' from replace(product, '\n', '')) like '"+product+"' " +
				"and (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by assigned_to " +
				"order by assigned_to;"
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			owners.add(rs.getString("(trim(' ' from replace(a.assigned_to, '\n', '')))"));
			bugsOwned.add(rs.getString("count(a.bug_id)"));
		}
		
		System.out.println("Calculating the Number of Comments and the Timespan by the Developers...");
		
		for(int i = 0; i < owners.size(); i++)
		{
			rs = s.executeQuery(
					"select count(text), timestampdiff(second, MIN(bug_when), MAX(bug_when))/3600 AS comment_span "+
					"from comment " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"'	) " +
					"and trim(' ' from replace(who, '\n', '')) like '"+owners.get(i) + "' " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"group by who " +
					"order by who;"
					); //Query to find the distinct developers working on the bugs
			
			String comment = "";
			String commentSpan = "";
			
			while(rs.next())
			{	
				comment = rs.getString("count(text)");
				commentSpan = rs.getString("comment_span");
				
				
				bugsCommented.add(comment);
				bugsCommentSpan.add(commentSpan);
			}
			
			if(comment.isEmpty())
			{
				comment = "0";
				bugsCommented.add(comment);
			}
			
			if(commentSpan.isEmpty())
			{
				commentSpan = "0";
				bugsCommentSpan.add(commentSpan);
			}
				
		}
		
		System.out.println("Calculating Activity Level of Each Developer...");
		
		for(int i = 0; i < owners.size(); i++)
		{
			
			
			rs = s.executeQuery(
					"select who, count(bug_when) " +
					"from activity " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"'	) " +
					"and trim(' ' from replace(who, '\n', '')) like '"+owners.get(i)+"' " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"group by who " +
					"order by who;"
					); 
			
			String numOfAct = "";
			
			while(rs.next())
			{
				numOfAct = rs.getString("count(bug_when)");
				
				noOfActivities.add(numOfAct);
			}
			
			if(numOfAct.isEmpty())
			{
				numOfAct = "0";
				noOfActivities.add(numOfAct);
			}
			
		}
		
		
		System.out.println("Calculating Average Elapsed Time...");
		
		rs = s.executeQuery(
				"select a.assigned_to,  avg(timestampdiff(second, a.creation_ts, a.delta_ts)/3600) as avgElapsedTime " +
				"from bugs a " +
				"where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
				"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by a.assigned_to " +
				"order by a.assigned_to; "
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			
			avgElapsedTime.add(rs.getString("avgElapsedTime"));
		}
		
		System.out.println("Calculating Average Interest Span...");
		
		rs = s.executeQuery(
				"select a.ownerz, avg(a.interest_span) " +
				"from " +
				"(select trim(' ' from replace(a.assigned_to, '\n', '')) as ownerz,  timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 as interest_span " +
				"from bugs a, comment b " +
				"where a.bug_id = b.bugid " +
				"and (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and b.bugid in " +
				"(	select bug_id from bugs where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' 	) " +
				"group by b.bugid " +
				"order by b.bugid asc) a " +
				"group by ownerz " +
				"order by ownerz; " 
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			assignedTo.add(rs.getString("ownerz"));
			avgInterestSpan.add(rs.getString("avg(a.interest_span)"));
		}
		
		
		System.out.println("Retrieving Specific Owners' Data and Median Elapsed and Interest Span...");
		/* A product can have many bugs, and not every bugs are owned by a single developer
		 * The next few queries require it to be repeated N times.
		 * N is the number of distinct developers that owns the bugs in the specified product.
		 */
		for(int i = 0; i < owners.size(); i++)
		{
			ArrayList<Float> elapsedTime 			= new ArrayList<Float>();
			ArrayList<Float> interestSpan 			= new ArrayList<Float>();
			//find the number of times the owner has commented on their own bugs
			rs = s.executeQuery(
					"select who, count(text) " +
					"from comment " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"' and assigned_to like '%" +owners.get(i) + "%' ) " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and who like '%" +owners.get(i) + "%' ; " 
					); //Query to find the distinct developers working on the bugs
			
			String comOnOwned = "";
			
			while(rs.next())
			{
				comOnOwned = rs.getString("count(text)");
				commentsOnOwned.add(comOnOwned);
			}
			
			if(comOnOwned.isEmpty())
			{
				comOnOwned = "0";
				commentsOnOwned.add(comOnOwned);
			}
			
			//find the number of times the owner has commented on bugs that they don't own
			rs = s.executeQuery(
					"select who, count(text) " +
					"from comment " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"' and assigned_to not like '%" +owners.get(i) + "%' ) " +
					"and who like '%" +owners.get(i) + "%' " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"');"
					);
			
			String comOffOwned = "";
			
			while(rs.next())
			{
				comOffOwned = rs.getString("count(text)");
				commentsOffOwned.add(comOffOwned);
			}
			
			if(comOffOwned.isEmpty())
			{
				comOffOwned = "0";
				commentsOffOwned.add(comOffOwned);
			}
			
			//find the elapsed time for every bug the owner has
			rs = s.executeQuery(
					"select timestampdiff(second, a.creation_ts, a.delta_ts)/3600 as elapsed_time " +
					"from bugs a " +
					"where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
					"and a.assigned_to like '%" +owners.get(i) + "%' " +
					"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"order by timestampdiff(second, a.creation_ts, a.delta_ts)/3600; "
					); //Query to find the distinct developers working on the bugs
			
			float elTime = 0.0f;
			
			while(rs.next())
			{
				elTime = elTime + rs.getFloat("elapsed_time");
				
				elapsedTime.add(elTime);
			}
			
			if(elTime == 0.0f)
			{
				elapsedTime.add(elTime);
			}
			
			//find the median of the elapsed time
			int mid = elapsedTime.size()/2; 
			float median = elapsedTime.get(mid); 
			if (elapsedTime.size()%2 == 0) 
			{ 
				median = (median + elapsedTime.get(mid-1))/2; 
			}
			
			medianElapsedTime.add(""+median);
			
			//find median interest spans for every bug the owner has
			rs = s.executeQuery(
					"select timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 as interest_span " +
					"from bugs a, comment b " +
					"where a.bug_id = b.bugid " +
					"and b.bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(a.product, '\n', '')) like '"+product+"'	) " +
					"AND a.assigned_to like '%" +owners.get(i) + "%' " +
					"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"group by b.bugid " +
					"order by timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 asc " 
					);
			Float intSpan = 0.0f;
			
			while(rs.next())
			{
				intSpan = intSpan + rs.getFloat("interest_span");
				interestSpan.add(intSpan);
			}
			
			if(intSpan == 0.0f)
			{
				interestSpan.add(intSpan);
			}
			//find the median of the interest span
			int mid2 = interestSpan.size()/2; 
			float median2 = interestSpan.get(mid2); 
			if (interestSpan.size()%2 == 0) 
			{ 
				median2 = (median2 + interestSpan.get(mid2-1))/2; 
			}
		
			medianInterestSpan.add(""+median2);
			
		}
		
		StringBuilder matrix = new StringBuilder();
		
		//RFunctions rf = Controller.rf;
		//ArrayList<String> degNBet = rf.rScript(fileContent, owners);
		
		//Column Headers
		//matrix.append("developer, bugs-owned, bugs-commented, comment-span, comments-on-owned, comments-on-nonowned, noof-activities, average-elapsed-time, median-elapsed-time, average-interest-span, median-interest-span, degree, betweenness, closeness, clustcoeff, eigencentrality, pagerank");
		matrix.append("developer, bugs-owned, bugs-commented, comment-span, comments-on-owned, comments-on-nonowned, noof-activities, average-elapsed-time, median-elapsed-time, average-interest-span, median-interest-span");
		matrix.append("\n");
		
		String tempString = "0";
		
		for(int i = 0; i < owners.size(); i++)
		{
			matrix.append(owners.get(i) + ", ");
			matrix.append(bugsOwned.get(i) + ", ");
			matrix.append(bugsCommented.get(i) + ", ");
			matrix.append(bugsCommentSpan.get(i) + ", ");
			matrix.append(commentsOnOwned.get(i) + ", ");
			matrix.append(commentsOffOwned.get(i) + ", ");
			matrix.append(noOfActivities.get(i) + ", ");
			
			for(int j = 0; j < assignedTo.size(); j++)
			{
				if(owners.get(i).equals(assignedTo.get(j)))
				{
					tempString = avgInterestSpan.get(j);
				}
			}
			matrix.append(avgElapsedTime.get(i) + ", ");
			
			matrix.append(medianElapsedTime.get(i) + ", ");
			matrix.append(tempString + ", ");
			matrix.append(medianInterestSpan.get(i));
			//matrix.append(degNBet.get(i));
			matrix.append("\n");
			tempString = "0";
		}
		
		
		
		fileName = product+"-("+startDate+")-("+endDate+")-Devs-Details.csv";
		fileContent = matrix.toString();
		
		System.out.println("");
		System.out.println("Building " + fileName);
	}
	
	public void generateCommenterModel(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> commenters 			= new ArrayList<String>();
		ArrayList<String> owners2		 		= new ArrayList<String>();
		
		ArrayList<String> owners		 		= new ArrayList<String>();
		ArrayList<String> bugsOwned		 		= new ArrayList<String>();
		ArrayList<String> assignedTo			= new ArrayList<String>();
		
		ArrayList<String> bugsCommented		 	= new ArrayList<String>();
		ArrayList<String> bugsCommentSpan	 	= new ArrayList<String>();
		
		ArrayList<String> commentsOnOwned		= new ArrayList<String>();
		ArrayList<String> commentsOffOwned	 	= new ArrayList<String>();
		
		ArrayList<String> noOfActivities	 	= new ArrayList<String>();
		
		ArrayList<String> avgElapsedTime	 	= new ArrayList<String>();
		ArrayList<String> medianElapsedTime		= new ArrayList<String>();
		
		ArrayList<String> avgInterestSpan	 	= new ArrayList<String>();
		ArrayList<String> medianInterestSpan	= new ArrayList<String>();
		
		
		
		System.out.println("");
		System.out.println("Finding the Distinct Commenters...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(b.who, '/n', ''))) 'who' "
				+ "from bugs a, comment b "
				+ "where a.bug_id = b.bugid "
				+ "and trim(' ' from replace(a.product, '\n', '')) like '"+product+"' "
				+ "and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " 
				+ "order by assigned_to;"
				);
		
		while(rs.next())
		{
			commenters.add(rs.getString("who").trim());
		}
		
		System.out.println("");
		System.out.println("Finding the Number of Bugs Owned by Each Developers...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(a.assigned_to, '\n', ''))) 'who', count(a.bug_id) " +
				"from bugs a " +
				"where trim(' ' from replace(product, '\n', '')) like '"+product+"' " +
				"and (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by assigned_to " +
				"order by assigned_to;"
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			owners.add(rs.getString("who"));
			bugsOwned.add(rs.getString("count(a.bug_id)"));
		}
		
		System.out.println("Calculating the Number of Comments and the Timespan by the Developers...");

		for(int i = 0; i < commenters.size(); i++)
		{
			rs = s.executeQuery(
					"select count(text), timestampdiff(second, MIN(bug_when), MAX(bug_when))/3600 AS comment_span "+
					"from comment " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"'	) " +
					"and trim(' ' from replace(who, '\n', '')) like '"+commenters.get(i) + "' " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"group by who " +
					"order by who;"
					); //Query to find the distinct developers working on the bugs
			
			String comment = "";
			String commentSpan = "";
			
			while(rs.next())
			{	
				comment = rs.getString("count(text)");
				commentSpan = rs.getString("comment_span");
				
				
				bugsCommented.add(comment);
				bugsCommentSpan.add(commentSpan);
			}
			
			if(comment.isEmpty())
			{
				comment = "0";
				bugsCommented.add(comment);
			}
			
			if(commentSpan.isEmpty())
			{
				commentSpan = "0";
				bugsCommentSpan.add(commentSpan);
			}
				
		}
		
		System.out.println("Calculating Activity Level of Each Developer...");
		
		for(int i = 0; i < commenters.size(); i++)
		{
			rs = s.executeQuery(
					"select who, count(bug_when) " +
					"from activity " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"'	) " +
					"and trim(' ' from replace(who, '\n', '')) like '"+commenters.get(i)+"' " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"group by who " +
					"order by who;"
					); 
			
			String numOfAct = "";
			
			while(rs.next())
			{
				numOfAct = rs.getString("count(bug_when)");
				
				noOfActivities.add(numOfAct);
			}
			
			if(numOfAct.isEmpty())
			{
				numOfAct = "0";
				noOfActivities.add(numOfAct);
			}
			
		}
		
		System.out.println("Calculating Average Elapsed Time...");
		
		rs = s.executeQuery(
				"select a.assigned_to,  avg(timestampdiff(second, a.creation_ts, a.delta_ts)/3600) as avgElapsedTime " +
				"from bugs a " +
				"where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
				"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"group by a.assigned_to " +
				"order by a.assigned_to; "
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			owners2.add(rs.getString("a.assigned_to"));
			avgElapsedTime.add(rs.getString("avgElapsedTime"));
		}
		
		System.out.println("Calculating Average Interest Span...");
		
		rs = s.executeQuery(
				"select a.ownerz, avg(a.interest_span) " +
				"from " +
				"(select trim(' ' from replace(a.assigned_to, '\n', '')) as ownerz,  timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 as interest_span " +
				"from bugs a, comment b " +
				"where a.bug_id = b.bugid " +
				"and (STR_TO_DATE(creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
				"and b.bugid in " +
				"(	select bug_id from bugs where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' 	) " +
				"group by b.bugid " +
				"order by b.bugid asc) a " +
				"group by ownerz " +
				"order by ownerz; " 
				); //Query to find the distinct developers working on the bugs
		
		while(rs.next())
		{
			assignedTo.add(rs.getString("ownerz"));
			avgInterestSpan.add(rs.getString("avg(a.interest_span)"));
		}
		
		
		System.out.println("Retrieving Specific Owners' Data and Median Elapsed and Interest Span...");
		/* A product can have many bugs, and not every bugs are owned by a single developer
		 * The next few queries require it to be repeated N times.
		 * N is the number of distinct developers that owns the bugs in the specified product.
		 */
		for(int i = 0; i < commenters.size(); i++)
		{
			ArrayList<Float> elapsedTime 			= new ArrayList<Float>();
			ArrayList<Float> interestSpan 			= new ArrayList<Float>();
			//find the number of times the owner has commented on their own bugs
			rs = s.executeQuery(
					"select who, count(text) " +
					"from comment " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"' and assigned_to like '%" +commenters.get(i) + "%' ) " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and who like '%" +commenters.get(i) + "%' ; " 
					); //Query to find the distinct developers working on the bugs
			
			String comOnOwned = "";
			
			while(rs.next())
			{
				comOnOwned = rs.getString("count(text)");
				commentsOnOwned.add(comOnOwned);
			}
			
			if(comOnOwned.isEmpty())
			{
				comOnOwned = "0";
				commentsOnOwned.add(comOnOwned);
			}
			
			//find the number of times the owner has commented on bugs that they don't own
			rs = s.executeQuery(
					"select who, count(text) " +
					"from comment " +
					"where bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"' and assigned_to not like '%" +commenters.get(i) + "%' ) " +
					"and who like '%" +commenters.get(i) + "%' " +
					"and (STR_TO_DATE(bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"');"
					);
			
			String comOffOwned = "";
			
			while(rs.next())
			{
				comOffOwned = rs.getString("count(text)");
				commentsOffOwned.add(comOffOwned);
			}
			
			if(comOffOwned.isEmpty())
			{
				comOffOwned = "0";
				commentsOffOwned.add(comOffOwned);
			}
			
			//find the elapsed time for every bug the owner has
			rs = s.executeQuery(
					"select timestampdiff(second, a.creation_ts, a.delta_ts)/3600 as elapsed_time " +
					"from bugs a " +
					"where trim(' ' from replace(a.product, '\n', '')) like '"+product+"' " +
					"and a.assigned_to like '%" +commenters.get(i) + "%' " +
					"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"order by timestampdiff(second, a.creation_ts, a.delta_ts)/3600; "
					); //Query to find the distinct developers working on the bugs
			
			float elTime = 0.0f;
			
			while(rs.next())
			{
				elTime = elTime + rs.getFloat("elapsed_time");
				
				elapsedTime.add(elTime);
			}
			
			if(elTime == 0.0f)
			{
				elapsedTime.add(elTime);
			}
			
			//find the median of the elapsed time
			int mid = elapsedTime.size()/2; 
			float median = elapsedTime.get(mid); 
			if (elapsedTime.size()%2 == 0) 
			{ 
				median = (median + elapsedTime.get(mid-1))/2; 
			}
			
			medianElapsedTime.add(""+median);
				
			
			
			//find median interest spans for every bug the owner has
			rs = s.executeQuery(
					"select timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 as interest_span " +
					"from bugs a, comment b " +
					"where a.bug_id = b.bugid " +
					"and b.bugid in " +
					"(	select bug_id from bugs where trim(' ' from replace(a.product, '\n', '')) like '"+product+"'	) " +
					"AND a.assigned_to like '%" +commenters.get(i) + "%' " +
					"and (STR_TO_DATE(a.creation_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and (STR_TO_DATE(a.delta_ts, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"and (STR_TO_DATE(b.bug_when, '%Y-%m-%d %H:%i:%s') between '"+startDate+"' and '"+endDate+"') " +
					"group by b.bugid " +
					"order by timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 asc " 
					);
			
			float intSpan = 0.0f;
			
			while(rs.next())
			{
				intSpan = intSpan + rs.getFloat("interest_span");
				interestSpan.add(intSpan);
			}
			
			if(intSpan == 0.0f)
			{
				interestSpan.add(intSpan);
			}
			//find the median of the interest span
			int mid2 = interestSpan.size()/2; 
			float median2 = interestSpan.get(mid2); 
			if (interestSpan.size()%2 == 0) 
			{ 
				median2 = (median2 + interestSpan.get(mid2-1))/2; 
			}
		
			medianInterestSpan.add(""+median2);
			
		}
		
		
		StringBuilder matrix = new StringBuilder();
		
		matrix.append("developer, bugs-owned, bugs-commented, comment-span, comments-on-owned, comments-on-nonowned, noof-activities, average-elapsed-time, median-elapsed-time, average-interest-span, median-interest-span");
		matrix.append("\n");
		
		String tempString = "0";
		String tempString2= "0";
		for(int i = 0; i < commenters.size(); i++)
		{
			matrix.append(commenters.get(i).trim() + ", ");
			for(int j = 0; j < owners.size(); j++)
			{
				if(owners.get(j).equalsIgnoreCase(commenters.get(i)))
				{
					matrix.append(bugsOwned.get(j) + ", ");
				}else
				{
					matrix.append("0, ");
				}
			}
			
			matrix.append(bugsCommented.get(i) + ", ");
			matrix.append(bugsCommentSpan.get(i) + ", ");
			matrix.append(commentsOnOwned.get(i) + ", ");
			matrix.append(commentsOffOwned.get(i) + ", ");
			matrix.append(noOfActivities.get(i) + ", ");
			
			for(int j = 0; j < assignedTo.size(); j++)
			{
				if(commenters.get(i).equals(assignedTo.get(j)))
				{
					tempString = avgInterestSpan.get(j);
				}
			}
			//matrix.append(avgElapsedTime.get(i) + ", ");
			
			for(int j = 0; j < owners2.size(); j++)
			{
				if(commenters.get(i).equals(owners2.get(j)))
				{
					tempString2 = avgElapsedTime.get(j);
				}
			}
			matrix.append(tempString2 + ", ");
			matrix.append(medianElapsedTime.get(i) + ", ");
			matrix.append(tempString + ", ");
			matrix.append(medianInterestSpan.get(i));
			//matrix.append(degNBet.get(i));
			matrix.append("\n");
			tempString = "0";
			tempString2= "0";
		}
		
		fileName = product+"-("+startDate+")-("+endDate+")-Devs-Details.csv";
		fileContent = matrix.toString();
	}
	
	public void closeConnection() throws Exception
	{
		rs.close(); //close connections
		s.close();
		con.close();
	}


}
