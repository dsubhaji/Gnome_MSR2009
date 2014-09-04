package ese.gnomemsr2009;

import java.util.ArrayList;

public class NetworkBuilder 
{
	public String networkBuilder(ArrayList<String> developers, ArrayList<String> developers2, ArrayList<String> developers3, ArrayList<Integer> edges, int num)
	{
		int vertexNumber = 1;
		String fileContent = "*Vertices " + num;
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
			fileContent = fileContent + "\r\n" + vertexNumber + " \"" + developers.get(i) +"\"";
			vertexNumber++;
			//append the vertices to variable 'vertices'
		}
		
		fileContent = fileContent + "\r\n*Edges";
		
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
				fileContent = fileContent + "\r\n" + dev1 + "\t" + dev2 + "\t" + newEdges.get(i);
			}
			
		}
		
		return fileContent;
	}

	public String bugsByDevs(ArrayList<String> distinctDev_email, ArrayList<String> distinctBug_id, ArrayList<String> dev_email, ArrayList<String> bug_id, ArrayList<Integer> numOfComments)
	{
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
		
		return matrix.toString();
	}
	
	
	public String devsByDevs(ArrayList<String> developers, ArrayList<String> developers2, ArrayList<String> developers3, ArrayList<Integer> edges)
	{
		StringBuilder matrix = new StringBuilder();
		
		System.out.println("Building DevsByDevs Matrix...");
		
		matrix.append("Developers, ");
		
		for(int i = 0; i < developers.size(); i++)
		{
			matrix.append(developers.get(i));
			matrix.append(", ");
		}
		
		for(int i = 0; i < developers.size(); i++)
		{
			matrix.append("\n");
			matrix.append(developers.get(i));
			matrix.append(", ");
			
			for(int j = 0; j < developers.size(); j++)
			{
				for(int k = 0; k < developers2.size(); k++)
				{
					if(	(developers2.get(k).equals(developers.get(i)))	&& (developers3.get(k).equals(developers.get(j))))
					{
						matrix.append(edges.get(k).toString());
						matrix.append(" ");
					}
				}
				matrix.append(", ");
			}
		}
		
		return matrix.toString();
	}
	
	public String bugsByDevsGithub(ArrayList<String> distinctDev_email, ArrayList<String> distinctBug_id, ArrayList<String> dev_email, ArrayList<String> bug_id, ArrayList<Integer> numOfComments)
	{
		StringBuilder matrix = new StringBuilder();
		
		System.out.println("Building BugsByDevelopers Matrix...");
		
		matrix.append("pull_request_id, ");
		
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
		
		return matrix.toString();
	}
	
	public String ownersModelGnome(ArrayList<String> owners, ArrayList<String> assignedTo, ArrayList<String> avgInterestSpan, ArrayList<String> mostParameters, ArrayList<String> otherParameters)
	{
		StringBuilder matrix = new StringBuilder();
		matrix.append("developer, bugs-owned, bugs-commented, comment-span, comments-on-owned, "
				+ "comments-on-nonowned, noof-activities, average-elapsed-time, median-elapsed-time, "
				+ "average-interest-span, median-interest-span, congruence");
		matrix.append("\n");
		String tempString = "0";
		for(int i = 0; i < owners.size(); i++)
		{
			matrix.append(owners.get(i) + ", ");
			matrix.append(mostParameters.get(i) + ", ");
			
			for(int j = 0; j < assignedTo.size(); j++)
			{
				if(owners.get(i).equals(assignedTo.get(j)))
				{
					tempString = avgInterestSpan.get(j);
				}
			}
			matrix.append(tempString + ", ");
			matrix.append(otherParameters.get(i) + ", ");
			matrix.append("\n");
			tempString = "0";
		}
		
		return matrix.toString();
	}
	
	
}
