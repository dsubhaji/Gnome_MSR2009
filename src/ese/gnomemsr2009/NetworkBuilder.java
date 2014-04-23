package ese.gnomemsr2009;

import java.util.ArrayList;


public class NetworkBuilder 
{
	private String dcn;
	
	public NetworkBuilder()
	{
		dcn = "";
	}
	
	public String getDCN()
	{
		return dcn;
	}
	
	public void networkBuilder(ArrayList<String> developers, ArrayList<String> developers2, ArrayList<String> developers3, ArrayList<Integer> edges, int num)
	{
		int vertexNumber = 1;
		dcn = "*Vertices " + num;
		int dev1 = 0;
		int dev2 = 0;
		
		for(int i = 0; i<developers.size();i++)
		{
			dcn = dcn + "\r\n" + vertexNumber + " \"" + developers.get(i) +"\"";
			vertexNumber++;
			//append the vertices to variable 'vertices'
		}
		
		dcn = dcn + "\r\n*Edges";
		
		for(int i = 0; i < developers2.size(); i++)
		{
			for(int j = 0; j < developers.size(); j++)
			{
				if(developers2.get(i).equals(developers.get(j)))
				{
					dev1 = j+1;
				}
				if(developers3.get(i).equals(developers.get(j)))
				{
					dev2 = j+1;
				}
			}
			
			if((dev1 > 0) && (dev2 > 0))
			{
				dcn = dcn + "\r\n" + dev1 + "\t" + dev2 + "\t" + edges.get(i);
			}
			
		}
	}
}
