package ese.gnomemsr2009;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;


public class IOFormatter 
{
	private String dbN, mysqlUserName, mysqlPass;
	private String product, startDate, endDate;
	private Scanner user_input = new Scanner( System.in );
	
	
	public IOFormatter()
	{
		dbN = "";
		mysqlUserName = "";
		mysqlPass = "";
		product = "";
		startDate = "";
		endDate = "";
	}
	
	public String getDBN()
	{
		return dbN;
	}
	
	public String getMysqlUserName()
	{
		return mysqlUserName;
	}
	
	public String getMysqlPass()
	{
		return mysqlPass;
	}
	
	public String getProduct()
	{
		return product;
	}
	
	public String getStartDate()
	{
		return startDate;
	}
	
	public String getEndDate()
	{
		return endDate;
	}
	
	
	
	
	public void inputConString()
	{
		System.out.print("Please Enter Database Name: ");
		//examples of connection strings
		//jdbc:mysql://127.0.0.1:3306/database?
		//jdbc:mysql://localhost:3306/database?
		
		dbN = user_input.nextLine();
		
		System.out.print("Please Enter User Name: ");
		mysqlUserName = user_input.nextLine();
		
		System.out.print("Please Enter User Password: ");
		mysqlPass = user_input.nextLine();
	}
	
	public void inputData()
	{
		//while(product.trim().isEmpty())
		//{
			System.out.print("Please Input Product Name:");
			product = user_input.nextLine();
		//}
		
		System.out.print("Enter Start Date(e.g 2002-05-28):");
		startDate = user_input.nextLine();
		
		System.out.print("Enter End Date(e.g 2002-06-28):");
		endDate = user_input.nextLine();
		
		user_input.close();
		
		if(startDate.trim().isEmpty())
		{
			startDate = "0000-00-00";
		}
		if(endDate.trim().isEmpty())
		{
			endDate = "9999-12-31";
		}
	}
	
	public int inputChoice()
	{
		int choice = 0;
		
		do
		{
			System.out.println("Available Services");
			System.out.println("1. Generate PAJEK File");
			System.out.println("2. Generate .CSV File");
			System.out.print  ("Please Enter Your Choice (1 or 2): ");
			
			try
			{
				choice = user_input.nextInt();
			} catch(InputMismatchException e)
			{
				System.out.println("Error! Only Integers Are Accepted.");
			}
			
			System.out.println("");
			
			user_input.nextLine();
			if(choice == 1)
			{
				return 1;
			}else if(choice == 2)
			{
				return 2;
			}
		} while((choice<1)||(choice>2));
		
		return 0;
	}
	
	
	public void writePajekFile(String product, String content)
	{
		DateFormat df = new SimpleDateFormat("YYYYMMdd-HHmmss"); 
		File fileName = new File("DCN-gnomemsr2009-"+product+"-"+df.format(new Date())+".net");
		try{
		    FileWriter fileWriter = new FileWriter(fileName);

		    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		    bufferedWriter.write(content);
		    bufferedWriter.close();
		    
		    System.out.println("\nPAJEK File Generated!");
		} catch(IOException e) {
		    System.out.println("COULD NOT WRITE!!");
		}
	}
	
	
	public void writeCSVFile(String text)
	{ 
		File fileName = new File("ProductsAndBugs.csv");
		try{
		    FileWriter fileWriter = new FileWriter(fileName);

		    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		    bufferedWriter.write(text);
		    bufferedWriter.close();
		    
		    System.out.println("\n.CSV File Generated!");
		} catch(IOException e) {
		    System.out.println("COULD NOT WRITE!!");
		}
	}
	
}
