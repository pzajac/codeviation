
package org.codeviation.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Line;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Package;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.Version;
import org.codeviation.model.VersionInterval;
import org.codeviation.tasks.RepositoryProcess;
import org.codeviation.tasks.RepositoryProcessEnv;
import org.codeviation.tasks.RepositoryProcessEnv.LogReason;
import org.codeviation.javac.UsageItem;
import org.codeviation.javac.UsagesMetric;

/**
 * It scans UsageMetrics and files for all developer in work directory.  
 * <br>
 * File name: "cvsusername"
 * <br>
 * For format of file: <br>
 * Class.method XX
 * <br>
 * Where<br>
 * Class - class name of usage
 * method - method name of usage
 * xx - number of usages 
 * @author pzajac
 */
public class UsageOwnerIndexer implements  RepositoryProcess {
     static Logger logger = Logger.getLogger(UsageOwnerIndexer.class.getName());
     private Map<UsageItem,UsageItem> usages = new HashMap<UsageItem,UsageItem>();
     private Map<String,Map<UsageItem,Integer>> usagesOfUsers = new HashMap<String,Map<UsageItem,Integer>>();
     private File outDir;     
     
     /** for these users you want to generate diffs
      */  
     private static Set<String> diffUsers = new HashSet<String>();
     /** for these usages you want to generate diffs
      */ 
     private static Set<UsageItem> diffUsages = new HashSet<UsageItem>();
     
    private  void addUsage(UsageItem ui,String user) {
        // xxx
        if (user != null) {
            UsageItem normUi = usages.get(ui);
            if (normUi == null) {
                usages.put(ui,ui);
                normUi = ui;
            }
            Map<UsageItem,Integer> uiOfUser = usagesOfUsers.get(user);
            if (uiOfUser == null) {
                uiOfUser = new HashMap<UsageItem,Integer>();
                usagesOfUsers.put(user, uiOfUser);
            } 
            Integer oldVal = uiOfUser.get(normUi);
            int newVal = 1;
            if (oldVal != null) {
                newVal += oldVal;
            }
            uiOfUser.put(normUi, newVal);
        }
    }
    
    private void storeUsages() throws FileNotFoundException {
        File dir = new File(getOutDir(),"usages");
        dir.mkdirs();
        
        List<String> lines = new ArrayList<String>(1000);
        for (String user : usagesOfUsers.keySet()) {
            Map<UsageItem,Integer> uiOfUser = usagesOfUsers.get(user);
            File file =  (dir == null) ? new File(user + ".log") : new File (outDir,user); 
            PrintWriter pw = new PrintWriter(file);
            try {
                lines.clear();
                for (Map.Entry<UsageItem,Integer> entry : uiOfUser.entrySet()) {
                    UsageItem ui = entry.getKey();
                    lines.add(ui.getClazz() + "." + ui.getMethod() + " : " + entry.getValue());
                }
                Collections.sort(lines);
                for (String line : lines) {
                    pw.println(line);
                }
            } finally {
                pw.close();
            }
        }
    }
    
    protected boolean prepareFile(JavaFile jf) {
        return true;
    }

    public boolean execute(Repository repository, RepositoryProcessEnv env) {
        try {
           // initialize out dir 
           clear();
           setOutDir(new File(env.getWorkDir(),
                   getName() + File.separator + repository.getName()));
           env.log(this, LogReason.START_PROCESS, repository.getName()); 
           for (SourceRoot srcRoot : repository.getSourceRoots()) {
               System.out.println(srcRoot.getRelPath());
               env.log(this, LogReason.START_SOURCE_ROOT,srcRoot.getRelPath()); 
               for (Package pack : srcRoot.getPackages()) {
                   System.out.println(pack.getName());
                   for (JavaFile jf : pack.getJavaFiles()) {
                       if (prepareFile(jf)) {
                           UsagesMetric um = jf.getMetric(UsagesMetric.class);
                           if (um != null && um.getStorage() != null) {
                               for (PositionIntervalResult<UsageItem> usagePos : um.getStorage().getAllObjects()) {
                                   UsageItem ui =  usagePos.getObject();
                                   String user = getUser(usagePos);

                                   addUsage(ui,user);
                                   generateDiffs(user, ui, um, jf, usagePos);

                               }
                           }
                       } // prepareFile
                   }
               }
           env.log(this, LogReason.END_SOURCE_ROOT, srcRoot.getRelPath());
           }
           storeUsages();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(),ioe);
            return false;
        } finally {
           env.log(this, LogReason.END_PROCESS, repository.getName());
           clear();
        }
        return true;
    }
    protected String getUser(PositionIntervalResult<UsageItem> usagePos ) {
       return usagePos.getInterval().getStartPosition().getLine().getPosition().getVersion().getUser();
    }
    public void setOutDir(File dir) {
        dir.mkdirs();
        this.outDir = dir;
    }
    public File getOutDir()  {
        return outDir;
    } 
//    public static void main (String args[]) throws IOException {
//        new UsageOwnerIndexer().execute();
//    }

    private void clear() {
        usages.clear();
        usagesOfUsers.clear();
    }
    public String getName() {
        return "UsageOwnerIndexer";
    }

    public String getDescription() {
        return "Stores all java method usages per developer";
    }
    
    private void generateDiffs(String user,UsageItem ui,UsagesMetric um,JavaFile jf,PositionIntervalResult<UsageItem> usagePos) throws IOException {
        // XXX hardcoded tulach
       if (diffUsers.contains(user) && diffUsages.contains(ui)) {
          File dir = new File (getOutDir(),"diffs");
          dir.mkdirs();
          PrintWriter pw  = new PrintWriter(new FileWriter(new File(dir,user + ".txt"),true));
          try {
              VersionInterval vi = um.getStorage().get(usagePos);
              Version v = vi.getFrom();
              pw.println("------------------------------------");
              pw.println(jf.getPackage().getName() + "." + jf.getName());
              pw.println(ui.getClazz() + "." + ui.getMethod() + ":" + vi.getFrom().getRevision() + " -> " + vi.getTo().getRevision() );
              pw.println("--------------------------------------");
              List<Line> lines =  vi.getFrom().getLines();
              Line startLine = usagePos.getInterval().getStartPosition().getLine();
              Line endLine = usagePos.getInterval().getEndPosition().getLine();
              int index1 = lines.indexOf(startLine) - 5;
              if (index1 < 0 ) {
                  index1 = 0;
              }
              int index2 = lines.indexOf(endLine) + 5;
              if (index2 >= lines.size()) {
                  index2 = lines.size() - 1;
              }
              pw.println("@" + index1 + " -> @" + index2);
              for (int i = index1 ; i < index2 ; i++) {
                  Line line = lines.get(i); 
                  v = line.getPosition().getVersion();
                  String annot = "[" + v.getRevision() + "," + v.getUser() + "]";
                  pw.println(annot + line.getNewContent());
              }
          } finally {
            pw.close();
          }

       } // generate diffs
    }
}
