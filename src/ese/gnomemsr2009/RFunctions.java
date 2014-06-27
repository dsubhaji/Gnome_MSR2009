package ese.gnomemsr2009;




import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;





import java.util.ArrayList;

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
	
	public void setTextToAppend(String s)
	{
		textToAppend = s;
	}
	
	public void rScript(String fileContent, String devEmail) throws Exception
	{
		re.eval("if(\"blockmodeling\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"blockmodeling\")}");
		re.eval("if(\"igraph\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"igraph\")}");
		re.eval("if(\"Matrix\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"Matrix\")}");
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
		re.eval("write.csv(bnd, file=\"db-metrics.csv\")");
		
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
				
				if(i==0)
				{
					b = b + a + ", ";
				} else
				{
					b = b + a;
				}
				
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
	
	/*Input: Directory to csv files and product name
	 *Output: network-metrics.csv for each product I.E Degree and betweenness
	 */
	public void nwMatrix(String s, String prodName)
	{
		re.eval("if(\"blockmodeling\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"blockmodeling\")}");
		re.eval("if(\"igraph\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"igraph\")}");
		re.eval("if(\"Matrix\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"Matrix\")}");
		re.eval("library('blockmodeling')");
		re.eval("library('igraph')");
		re.eval("library('Matrix')");
		
		s=s.replaceAll("\\\\", "/");
		
		System.out.println("dcn = loadnetwork(\""+s+"\\\\"+prodName+"\\\\"+prodName+"-DCN.net\")");
		System.out.println("write.csv(bnd, file="+s+"\\\\"+prodName+"\\\\"+prodName+"-nw-metrics)");
		re.eval("dcn = loadnetwork(\""+s+"/"+prodName+"/"+prodName+"-DCN.net\")");
		re.eval("dcnGraphWeighted = graph.adjacency(dcn, mode=c(\"undirected\"), weighted=TRUE)");
		re.eval("dcnGraph         = graph.adjacency(dcn, mode=c(\"undirected\"))");
		re.eval("Degree = degree(dcnGraphWeighted)");
		re.eval("Betweenness = betweenness(dcnGraph)");
		re.eval("bnd = merge(as.data.frame(Degree), as.data.frame(Betweenness), by=\"row.names\")");
		re.eval("colnames(bnd) <- c(\"Developers\", \"Degree\", \"Betweenness\")");
		re.eval("write.csv(bnd, file=\""+s+"/"+prodName+"/"+prodName+"-nw-metrics.csv\")");
	}
	
	/* Input: Model type(Developer/bug), dependent and independent variable(s) and directory and product name
	 * Output: Summary of the regression in csv
	 */
	public void linRegression(String model, String dependentVar, ArrayList<String> independentVar, String s, String prodName)
	{
		int noOfVar = independentVar.size();
		String indVars = "";
		re.eval("if(\"blockmodeling\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"blockmodeling\")}");
		re.eval("if(\"igraph\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"igraph\")}");
		re.eval("if(\"Matrix\" %in% rownames(installed.packages()) == FALSE) {install.packages(\"Matrix\")}");
		re.eval("library('blockmodeling')");
		re.eval("library('igraph')");
		re.eval("library('Matrix')");
		
		re.eval(
				"fun1<-function(x){ "
				+ "res<-c(paste(as.character(summary(x)$call,collapse=\" \"), "
				+ "x$coefficients[1], "
				+ "x$coefficients[2], "
				+ "length(x$model), "
				+ "summary(x)$coefficients[2,2], "
				+ "summary(x)$r.squared, "
				+ "summary(x)$adj.r.squared, "
				+ "summary(x)$fstatistic, "
				+ "pf(summary(x)$fstatistic[1],summary(x)$fstatistic[2],summary(x)$fstatistic[3],lower.tail=FALSE)) "
				+ "names(res)<-c(\"call\",\"intercept\",\"slope\",\"n\",\"slope.SE\",\"r.squared\",\"Adj. r.squared\", \"F-statistic\",\"numdf\",\"dendf\",\"p.value\") "
				+ "return(res)}"
				);
		
		s=s.replaceAll("\\\\", "/");
		
		for(int i = 0; i < noOfVar; i++)
		{
			if(i < noOfVar-1)
				indVars = indVars + independentVar.get(i).replace("-", ".") + " + ";
			if(i == noOfVar-1)
				indVars = indVars + independentVar.get(i).replace("-", ".");
		}
		
		if(model.equals("developer"))
		{
			re.eval("deets = read.csv(\""+s+"/"+prodName+"/"+prodName+"-dev-details.csv\")");
			re.eval("m1 <- lm("+dependentVar.replace("-", ".")+" ~ "+indVars+", data=deets)");
		} else if(model.equals("bug"))
		{
			re.eval("deets = read.csv(\""+s+"/"+prodName+"/"+prodName+"-bug-details.csv\")");
			re.eval("m1 <- lm("+dependentVar+" ~ "+indVars+", data=deets)");
		}
		re.eval("res<-c(paste(as.character(summary(m1)$call),collapse=\" \"), m1$coefficients[1], m1$coefficients[2], length(m1$model), summary(m1)$coefficients[2,2], summary(m1)$r.squared, summary(m1)$adj.r.squared, summary(m1)$fstatistic, pf(summary(m1)$fstatistic[1],summary(m1)$fstatistic[2],summary(m1)$fstatistic[3],lower.tail=FALSE))");
		re.eval("names(res)<-c(\"call\",\"intercept\",\"slope\",\"n\",\"slope.SE\",\"r.squared\",\"Adj. r.squared\", \"F-statistic\",\"numdf\",\"dendf\",\"p.value\") ");
		re.eval("sumM1 <- res");
		re.eval("write.csv(sumM1, file=\""+s+"/"+prodName+"/"+prodName+"-"+model+"-model-output.csv\")");
		
		
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

