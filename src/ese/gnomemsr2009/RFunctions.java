package ese.gnomemsr2009;




import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


import java.util.Scanner;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.RMainLoopCallbacks;

class TextConsole implements RMainLoopCallbacks
{
    public void rWriteConsole(Rengine re, String text, int oType) {
        //System.out.print(text);
    }
    
    public void rBusy(Rengine re, int which) {
        //System.out.println("rBusy("+which+")");
    }
    
    public String rReadConsole(Rengine re, String prompt, int addToHistory) 
    {
        return null;
    }
    
    public void rShowMessage(Rengine re, String message) {
        //System.out.println("rShowMessage \""+message+"\"");
    }
	
    public String rChooseFile(Rengine re, int newFile) {
	/*FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.show();
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());*/
	return null;
    }
    
    public void   rFlushConsole (Rengine re) {
    }
	
    public void   rLoadHistory  (Rengine re, String filename) {
    }			
    
    public void   rSaveHistory  (Rengine re, String filename) {
    }			
}

public class RFunctions 
{
	String textToAppend;
	
	public RFunctions()
	{
		textToAppend = "";
	}
	
	public String getTextToAppend()
	{
		return textToAppend;
	}
	
	public void rScript(String fileContent) throws Exception
	{
		if (!Rengine.versionCheck()) {
		    System.err.println("** Version mismatch - Java files don't match library version.");
		    System.exit(1);
		}
		String[] args = null;
		
		Rengine re = new Rengine(args, false, new TextConsole());
		
		if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }
		
		re.eval("if(\"blockmodeling\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"blockmodeling\")}");
		re.eval("if(\"igraph\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"igraph\")}");
		re.eval("library('blockmodeling')");
		re.eval("library('igraph')");
		

		IOFormatter io = new IOFormatter();
		
		
		String pajek = fileContent;
		String fN = "tempFile.net";
		
		io.writeFile(pajek, fN);
		
		re.eval("dcn = loadnetwork(\"tempFile.net\")");
		re.eval("dcnGraphWeighted = graph.adjacency(dcn, mode=c(\"undirected\"), weighted=TRUE)");
		re.eval("dcnGraph         = graph.adjacency(dcn, mode=c(\"undirected\"))");
		re.eval("Degree = degree(dcnGraphWeighted)");
		re.eval("Betweenness = betweenness(dcnGraph)");
		re.eval("bnd = merge(as.data.frame(Degree), as.data.frame(Betweenness), by=\"row.names\")");
		re.eval("colnames(bnd) <- c(\"Developers\", \"Degree\", \"Betweenness\")");
		
		re.eval("write.csv(bnd, file=\"tempFile2.csv\")");
		
		File file = new File("tempFile.net");
		File file2= new File("tempFile2.csv");
		
		Scanner input = new Scanner(file2);
		
		
		while(input.hasNext()) 
		{
			textToAppend = textToAppend + input.nextLine() + "\n";
		}

		input.close();
		
		file.delete();
		file2.delete();
	}
	
}
