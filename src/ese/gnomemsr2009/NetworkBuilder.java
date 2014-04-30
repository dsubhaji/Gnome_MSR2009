package ese.gnomemsr2009;

import java.util.ArrayList;

//no longer used
//building of matrix/csv/pajek file all done in DatabaseAccessor.


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
	}
}
