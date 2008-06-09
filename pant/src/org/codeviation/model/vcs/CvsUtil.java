/*
 * Main.java
 *
 * Created on June 20, 2003, 4:59 PM
 */

package org.codeviation.model.vcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.Version;

/**
 *
 * @author  pz97949
 */
public class CvsUtil {
    private File cvsDirectory;
    private File cvsFile;
    
    static Logger log = Logger.getLogger(CvsUtil.class.getName());
    
    /** Creates a new instance of Main 
     * @param cvsDirectory is directory in cvs repository
     * @param cvsFile is path to cvs command file
     */
    public CvsUtil(File cvsDirectory,File cvsFile) throws IllegalArgumentException {
        this.cvsDirectory = cvsDirectory;
        this.cvsFile = cvsFile;
        if (cvsDirectory.isDirectory() == false ) {
            throw new IllegalArgumentException (cvsDirectory + " is not directory");
        }
        if (cvsFile.exists() == false) {
            throw new IllegalArgumentException("The " + cvsFile + " doesn't exist.");
        }
    }
   
    /** returns versions tree for specified file 
     */
    public Version getRootVersinTree (String fileName) throws IOException {
        String commandParameters[] = new String[] {cvsFile.getAbsolutePath(), "log", fileName};
        BufferedReader reader = null;
        try {
            reader = executeCvsCommand(commandParameters,cvsDirectory);
        } catch (InterruptedException ie) {
            throw new IOException("error on execution of " + commandParameters);
        }
        List<Version> versions =  parseLog(reader);
        // versions doesn't
        if (versions.isEmpty()) {
            return new Version("1.0"," no cvs" , new Date(), "?",Version.State.DEAD);
        }
        Version root =  Version.sortVersions(versions);
        if (root.getState() != Version.State.DEAD) { 
            // create imaginary revision
            versions.add(new Version("1.0","" , new Date(root.getDate().getTime() - 500), "?",Version.State.DEAD));
            root = Version.sortVersions(versions);
        }
        return  root; 
    }
    
    /**  execute CVS command
     * @param commandParameters parameters of cvs command on commandline 
     * @return stdOut of executed command
     */ 
    public static BufferedReader executeCvsCommand(String commandParameters[],File cvsDirectory) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder logParameters = new StringBuilder("Cvs parameters: ");
        for (int i = 0 ;i < commandParameters.length ; i++ ) {
            logParameters.append('\'');
            logParameters.append(commandParameters[i].toString());
            logParameters.append("\' ");
        }
        log.fine(logParameters.toString());
        log.fine("CVS dir: " + cvsDirectory.getAbsolutePath());
        
        assert cvsDirectory.exists();
        Process process = runtime.exec(commandParameters,null,cvsDirectory);
        ReadThread it = new ReadThread(process.getInputStream());
        new Thread(it,"STD Out").start();
        ReadThread et = new ReadThread(process.getErrorStream());
        new Thread(et,"STD Error").start();
        if (process.waitFor() != 0) {
            log.severe(logParameters.toString());
            log.severe("CVS dir: " + cvsDirectory.getAbsolutePath());
        }
        String output = it.getOutput();
        log.fine("cvs stdout: " + output);
        log.fine("cvs stderr: " + et.getOutput());
        return new BufferedReader (new StringReader(output)  ) ;
    }
    
    /** Parse output of cvs log command
     * @param breader output of cvs command
     * @return Collection of unsorted Version 
     */
    public List<Version> parseLog (BufferedReader breader) throws IllegalStateException, IOException {
        List<Version> revisions = new ArrayList<Version>();
        
        /* revisions begins with description */

        //description:
        //----------------------------
        //revision 1.32
        //date: 2003/03/14 22:54:13;  author: jglick;  state: Exp;  lines: +3 -3
        //[Javadoc] HTML parse error was making whole page hard to read.
        //----------------------------

        //description:
        
        // revision number ((\\d\)(.\\d)+)
        String revString = null;
        String line = null;
        StringBuffer comment = new StringBuffer();
        while (((line = breader.readLine() ) != null) && !line.startsWith("description"));
            
        //----------------------------
        line = breader.readLine();
        Date prevDate = null;
        while ((line != null)) {
             if (line.startsWith("--------------") == false) {
                 throw new IllegalStateException("revisions must begin with ---");
             }
            line = breader.readLine();
            // sometimes there are two lines
            if (line.startsWith("--------------")) {
                line = breader.readLine();
            }
            if (line == null) {
                break;
            }
            //revision 1.32
            
            if (line.startsWith("revision ")) {
                int revIndex = "revision ".length();
                revString = line.substring(revIndex);
            }

            //date: 2003/03/14 22:54:13;  author: jglick;  state: Exp;  lines: +3 -3
            //date: 2003-03-14 22:54:13;  author: jglick;  state: Exp;  lines: +3 -3
            line = breader.readLine(); 
            Date date = null;
            String user = null;
            String state = null;
            for (StringTokenizer tokenizer = new StringTokenizer(line,";");
                   tokenizer.hasMoreTokens();) {             
               String token = tokenizer.nextToken().trim();
               if (token.startsWith("date:")) {
                   date = parseDate(token.substring("date:".length()));
               } else if (token.startsWith("author:")) {
                   user = token.substring("author:".length()).trim();
               } else if (token.startsWith("state:")) {
                   state = token.substring("state:".length()).trim();
               }
            }   
                        
            //[Javadoc] HTML parse error was making whole page hard to read.
            //----------------------------
            comment.setLength(0); 
            while ((line = breader.readLine()) != null && 
                    line.startsWith("---------------") == false) {
                comment.append(line);
                comment.append('\n');
            }
//            if (revisions.isEmpty()) {
//                // workaround before first commit ..
//                revisions.add(new Version("1.0","",new Date(date.getTime() - 1000),user,Version.State.DEAD)); 
//            }
            // check if cvs is broken
            // look at CVSMetricTest.testCatalogNode ()
            if (state == null) {
                state = "Exp";
            }
            if (user == null) {
                user = "??";
            }
            if (date == null) {
                Logger.getLogger(CvsUtil.class.getName()).log(Level.SEVERE,"null date for log: " + line);
                // XXX fake
                if (prevDate != null) {
                    date = new Date(prevDate.getTime() + 10);
                } else {       
                    date = new Date();
                }       
            }
            prevDate = date;
          
            revisions.add(new Version(revString,comment.toString(),date,user,Version.State.parse(state)));
        } // revision
        return revisions;
    }
    
    static class ReadThread implements Runnable {
        
        public volatile BufferedReader reader;
        
        String output ;
        
        public ReadThread(InputStream is ) {
            if (is != null) {
                reader = new BufferedReader(new InputStreamReader(is));
            }
        }
        
        public  void  run() {
            String line;
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            try {
                while ((line = reader.readLine()) != null) {
                   pw.println( line);
                   log.fine(line);
                }
            } catch (IOException ioe ) {
                ioe.printStackTrace();
            }
            pw.close();
            output = stringWriter.toString();
            if (output == null) {
                output = "";
            }
            synchronized (this) {
                this.notifyAll();
            }
            log.fine("notify");
        }
        
        synchronized String getOutput () {
            
            if (output == null) {
                try {
                    log.fine("wait");
                    this.wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            return output;
        }
    }
 // parseLog
       
    public Diff diff(String fileName, Version v1,Version v2) throws IOException {
           log.fine(fileName);
           String cvsDirPath = cvsDirectory.getAbsolutePath();
           // use rather rel path 
           if (fileName.indexOf(cvsDirPath) == 0) {
               fileName = fileName.substring(cvsDirPath.length());
               if (fileName.startsWith("/")) {
                   fileName = fileName.substring(1);
               }
           }
           String params[] = new String[] {cvsFile.getAbsolutePath(),
               "diff","-N","-r",v1.getRevision(),"-r",v2.getRevision(),fileName};
             String line = null;
             StringBuffer buffer = new StringBuffer();
             try {  
                BufferedReader reader = executeCvsCommand(params,cvsDirectory);
            
                 while ( (line = reader.readLine() ) != null ) {
                     buffer.append(line);
                     buffer.append("\n");
                 }
              } catch (InterruptedException e) {
                 e.printStackTrace();
             }

          String output = buffer.toString();
//          if (output.length() == 0 ) {
//              throw new IllegalStateException("Empty output for diff " + fileName + ","  + v1 + "," + v2);
//          }  
       return  new Diff(output,v1, v2);       
    }
    
    String streamToString (InputStream is ) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer ();
        String line = null;
        try {
            while ( (line = reader.readLine()) != null ) {
                buffer.append(line);
                buffer.append("\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return buffer.toString();
    }
    /** 
     * @return all Diffs from branch of version
     */
    public Diff [] getAllDiffs(String fileName,Version version,Diff oldDiffs[]) throws IOException {
        Version v1 = version;
        
        // number of diffs
        int count = -1;
        do  {
             v1 =  v1.getNext();
            count++;
        } while ( v1 != null) ;
        
        Diff diffs [] = new Diff[count];
         v1 = version;
        for (int i = 0 ; i  < count ; i++ ) {
            Version v2 = v1.getNext();
            if (oldDiffs != null && i < oldDiffs.length ) {
                diffs[i] = oldDiffs[i];
                diffs[i].updateVersions(v1,v2);
            } else {
                diffs[i] = diff (fileName,  v1, v2 );
            }
             v1 =  v2;
        }
        return diffs ;
    }  
    public File getCVSDirectory() {
        return cvsDirectory;
    }
    
    /** @return cvs version of file if exists. Otherwise it returns null.
     */
    public String getVersion(String fileName) {
        File f = new File(getCVSDirectory(),"CVS");
        if (!f.exists()) {
            // file is not part of cvs
            return null;
        }
        assert f.isDirectory();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(f,"Entries")));
            try {
                String line = null;
                String prefix = "/" + fileName + "/";
                while ((line = reader.readLine()) != null ) {
                    if (line.startsWith(prefix)) {
                        line = line.substring(prefix.length());
                        return line.substring(0,line.indexOf("/"));
                    }
                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe) {
            CvsUtil.error(ioe);
        } 
        return null;
    }
    
    static void error(Exception e ) {
        Logger.getLogger("CVSUtil").log(Level.SEVERE, null, e); 
    }
          
    public static CvsUtil getCvsUtil(File folder) { 
        return new CvsUtil(folder, getCVSExecutable());
    }
    
    static Date parseDate(String date) {
        StringTokenizer tokenizer = new StringTokenizer(date,"-:;/. ");
        int year = Integer.parseInt(tokenizer.nextToken().trim());
        int month = Integer.parseInt(tokenizer.nextToken().trim()) - 1;
        int day = Integer.parseInt(tokenizer.nextToken().trim());
        int hour = Integer.parseInt(tokenizer.nextToken().trim());
        int minutes = Integer.parseInt(tokenizer.nextToken().trim());
        int seconds = Integer.parseInt(tokenizer.nextToken().trim());
        return new GregorianCalendar(year, month,day,hour,minutes,seconds).getTime();        
    }
    
    private static final String CVS_EXECUTABLE_PROP_NAME = "pant.cvs.executable";
    private static File cvsExecutable;
    
    private static File  getCVSExecutable() {
        if (cvsExecutable == null) {
            String cvsExecutableName = System.getProperty(CVS_EXECUTABLE_PROP_NAME);
            if (cvsExecutableName == null) {
                // default linux
                cvsExecutableName = "/usr/bin/cvs";
            }
            cvsExecutable = new File(cvsExecutableName);
            if (!cvsExecutable.exists()) {
                throw new IllegalStateException("Cvs executable doesn't exist: "  + cvsExecutableName);
            }
        }
        return cvsExecutable;
    }
}

