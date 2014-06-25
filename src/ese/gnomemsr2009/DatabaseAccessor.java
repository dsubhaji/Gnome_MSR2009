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


public class DatabaseAccessor 
{
	Connection con;
	ResultSet rs;
	Statement s ;
	
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
			s = con.createStatement(); //Statements to issue sql queries
			
		} catch (SQLException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void createPajek(String product, String startDate, String endDate) throws Exception
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
	
	public void generateCSV(String productName) throws Exception
	{
		System.out.println("Extracting Data from Database...");
		
		rs = s.executeQuery("select distinct(trim(' ' from replace(a.product, '\n', ''))), count(distinct(b.bugid)), count(b.bug_when), count(distinct(b.who)), MIN(trim(' ' from replace(b.bug_when, '\n', ''))), MAX(trim(' ' from replace(b.bug_when, '\n', ''))) "
							+"from bugs a, comment b "
							+"where a.bug_id = b.bugid "
							+"and trim(' ' from replace(a.product, '\n', '')) like '"+productName+"'"
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
		
		//Column Headers
		matrix.append("Bug_ID, Owner, Elapsed-Time, Component, Version, Rep-Platform, Op-Sys, Bug-Status, Resolution, Priority, Severity, Target-Milestone, Duplicate, Activity-Level, Number-of-Comments, Number-Of-Commenter, Interest-Span, Number-of-Comments-by-Owner, Owner-Workload, Owner-Comment-Arc");
		
		for(int i = 0; i < bug_id.size(); i++)
		{
			matrix.append("\n");
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
			matrix.append(", ");
			
		}

		
		fileName = product+"-("+startDate+")-("+endDate+")-Bugs-Details.csv";
		fileContent = matrix.toString();
		System.out.println("");
		System.out.println("Building " + fileName);
	}
	
	public void generateDevModel(String product, String startDate, String endDate) throws Exception
	{
		ArrayList<String> owners		 		= new ArrayList<String>();
		ArrayList<String> bugsOwned		 		= new ArrayList<String>();
		
		ArrayList<String> bugsCommented		 	= new ArrayList<String>();
		ArrayList<String> bugsCommentSpan	 	= new ArrayList<String>();
		
		ArrayList<String> commentsOnOwned		= new ArrayList<String>();
		ArrayList<String> commentsOffOwned	 	= new ArrayList<String>();
		
		ArrayList<String> noOfActivities	 	= new ArrayList<String>();
		
		ArrayList<String> avgElapsedTime	 	= new ArrayList<String>();
		ArrayList<String> medianElapsedTime		= new ArrayList<String>();
		
		ArrayList<String> avgInterestSpan	 	= new ArrayList<String>();
		ArrayList<String> medianInterestSpan	= new ArrayList<String>();
		
		ArrayList<Double> elapsedTime 			= new ArrayList<Double>();
		ArrayList<Double> interestSpan 			= new ArrayList<Double>();
		
		
		System.out.println("");
		System.out.println("Finding the Number of Bugs Owned by Each Developers...");
		
		rs = s.executeQuery(
				"select distinct(trim(' ' from replace(a.assigned_to, '\n', ''))), count(a.bug_id) " +
				"from bugs a " +
				"where assigned_to in " +
				"(	select distinct(assigned_to) from bugs where trim(' ' from replace(product, '\n', '')) like '"+product+"'	) " +
				"and trim(' ' from replace(product, '\n', '')) like '"+product+"' " +
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
				"(select a.assigned_to as ownerz,  timestampdiff(second, MIN(b.bug_when), MAX(b.bug_when))/3600 as interest_span " +
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
			avgInterestSpan.add(rs.getString("avg(a.interest_span)"));
		}
		
		
		System.out.println("Retrieving Specific Owners' Data and Median Elapsed and Interest Span...");
		/* A product can have many bugs, and not every bugs are owned by a single developer
		 * The next few queries require it to be repeated N times.
		 * N is the number of distinct developers that owns the bugs in the specified product.
		 */
		for(int i = 0; i < owners.size(); i++)
		{
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
			
			while(rs.next())
			{
				elapsedTime.add(rs.getDouble("elapsed_time"));
			}
			
			//find the median of the elapsed time
			int mid = elapsedTime.size()/2; 
			double median = elapsedTime.get(mid); 
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
			
			while(rs.next())
			{
				interestSpan.add(rs.getDouble("interest_span"));
			}
			
			//find the median of the interest span
			int mid2 = interestSpan.size()/2; 
			double median2 = interestSpan.get(mid2); 
			if (interestSpan.size()%2 == 0) 
			{ 
				median2 = (median2 + interestSpan.get(mid2-1))/2; 
			}
		
			medianInterestSpan.add(""+median2);
			
		}
		
		StringBuilder matrix = new StringBuilder();
		
		RFunctions rf = Controller.rf;
		System.out.println("");
		System.out.println("Calculating Degree and Betweenness of the Developers...");
		createPajek(product, startDate, endDate);
		//Column Headers
		matrix.append("Developer, Bugs Owned, Bugs Commented, Comment Span, Comments On Owned, Comments On Not Owned, No. Of Activities, Avg. Elapsed Time, Median Elapsed Time, Avg. Interest Span, Median Interest Span, Degree, Betweenness");
		
		
		for(int i = 0; i < owners.size(); i++)
		{
			matrix.append("\n");
			rf.rScript(fileContent, owners.get(i));
			matrix.append(owners.get(i) + ", ");
			matrix.append(bugsOwned.get(i) + ", ");
			matrix.append(bugsCommented.get(i) + ", ");
			matrix.append(bugsCommentSpan.get(i) + ", ");
			matrix.append(commentsOnOwned.get(i) + ", ");
			matrix.append(commentsOffOwned.get(i) + ", ");
			matrix.append(noOfActivities.get(i) + ", ");
			matrix.append(avgElapsedTime.get(i) + ", ");
			matrix.append(medianElapsedTime.get(i) + ", ");
			matrix.append(avgInterestSpan.get(i) + ", ");
			matrix.append(medianInterestSpan.get(i) + ", ");
			matrix.append(rf.getTextToAppend());
			
		}
		
		
		
		fileName = product+"-("+startDate+")-("+endDate+")-Devs-Details.csv";
		fileContent = matrix.toString();
		
		System.out.println("");
		System.out.println("Building " + fileName);
	}
	
	public void closeConnection() throws Exception
	{
		rs.close(); //close connections
		s.close();
		con.close();
	}


}
