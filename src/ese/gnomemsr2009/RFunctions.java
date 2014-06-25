package ese.gnomemsr2009;




import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;





import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.REngine.REngine;


class TextConsole implements RMainLoopCallbacks
{
    public void rWriteConsole(Rengine re, String text, int oType) {
        System.out.print(text);
    }
    
    public void rBusy(Rengine re, int which) {
        System.out.println("rBusy("+which+")");
    }
    
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        System.out.print(prompt);
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            String s=br.readLine();
            return (s==null||s.length()==0)?s:s+"\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: "+e.getMessage());
        }
        return null;
    }
    
    public void rShowMessage(Rengine re, String message) {
        System.out.println("rShowMessage \""+message+"\"");
    }
	
    public String rChooseFile(Rengine re, int newFile) {
	FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.show();
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
	return res;
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
	Rengine re;
	TextConsole tc;
	
	public RFunctions()
	{
		textToAppend = "";
	}
	
	public String getTextToAppend()
	{
		return textToAppend;
	}
	
	public void rScript(String fileContent, String devEmail) throws Exception
	{
		re.eval("if(\"blockmodeling\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"blockmodeling\")}");
		re.eval("if(\"igraph\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"igraph\")}");
		//re.eval("if(\"Matrix\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"Matrix\")}");
		re.eval("library('blockmodeling')");
		re.eval("library('igraph')");
		re.eval("library('Matrix')");
		
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
		
		re.eval("devInf <- subset(bnd, bnd$Developers == '"+devEmail+"')");
		
		String b = "";
		
		REXP x = re.eval("devInf[, 2:3]");
		RList vl = x.asList();
		String[] k = vl.keys();
		String[] m = new String[k.length];
		
		if (k!=null) {
			int i=0; while (i<k.length) m[i] = "" + vl.at(k[i++]);
		}	
		
		if (m!=null)
		{
			int i=0;
			while(i<m.length)
			{
				String a = "";
				
				a = m[i].substring(8, m[i].length()-2);
				
				if(a.isEmpty())
					a = "0";
				
				b = b + a + ", ";
				i++;
			}
		}
		
		
		
		File file = new File("tempFile.net");
		File file2= new File("tempFile2.csv");
		
		
		textToAppend = b;

		
		file.delete();
		file2.delete();
		//re.end();
	}
	
	public void bugModel()
	{
		
	}
	
	
	/* Method Name: startRengine
	 * INPUT: NONE
	 * OUTPUT: NONE
	 * Function: Create an REngine object so we can send R commands
	 */
	
	public void startRengine()
	{
		/*if (!Rengine.versionCheck()) {
	    System.err.println("** Version mismatch - Java files don't match library version.");
	    System.exit(1);
		}*/
		String[] args = null;
		tc = new TextConsole();
		
		re = new Rengine(args, false, null);
		//re.DEBUG = 10;
		if (!re.waitForR()) 
		{
	        System.out.println("Cannot load R");
	        return;
	   	}
	}
}

