package org.codeviation.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.bugtracking.issuezilla.IssuezillaUtil;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.bugtracking.issuezilla.Component;
import org.codeviation.bugtracking.issuezilla.Issue;


/** Iterate trought all java file's versions a update connection between issuezilla and cvs log.
 *  Don't call this class externaly (packageToComponent is static TreeMap). 
 *  stores mapping between package and issuezilla component to "/tmp/componentmapping.txt"
 * @author Petr Zajac
 */
public class UpdateIssues implements RepositoryProcess{
    private  Map <String,Map<Component,Component>> packageToComponent = new TreeMap<String,Map<Component,Component>>();
    int noIssue ;
    int enhacements;
    int defects;
    int features;

    static Logger logger = Logger.getLogger(UpdateIssues.class.getName());
   
   private static class ComponentComparator implements Comparator<Component> {
            public int compare(Component comp1, Component comp2) {
                return  comp2.getIssuesSize() - comp1.getIssuesSize();
            }
   }

   public void addRecursiveIssue(Package pack,Version ver) throws IOException, SQLException {
       if (ver != null) {
           ver.clearIssueType();
           // just only get 
           Version.IssueType issueType = ver.getIssueType();
           switch (issueType) {
               case DEFECT:
                   addIssue(pack,ver);
                   defects++;
                   break;
               case ENHANCEMENT:
                   enhacements++;
                   break;
               case NO_ISSUE:    
                   noIssue++;
                   break;
               case FEATURE:
                   features++;
                   break;
           }
           addRecursiveIssue(pack,ver.getMyBranch());
           addRecursiveIssue(pack,ver.getNext());
       }
   }
//   public static void main(String arg[]) throws SQLException, IOException {
//       if (System.getProperty(PersistenceManager.PANT_CACHE_FOLDER) == null) {
//           // XXX
//        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, "/cvss/pantcache");
//       }
//       UpdateIssues ui = new UpdateIssues();
//       Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
//       UpdateIssues uissues = new UpdateIssues();
//       RepositoryProcessEnv env = new RepositoryProcessEnv();
//       env.setWorkDir(new File("/tmp"));
//       uissues.execute( rep, env);
//   }    
   public  void addIssue(Package pack,Version ver) throws SQLException, IOException {
        for (int id :ver.getDefectNumbers()) {
            Issue issue = Issue.readIssue(id);
            if (issue != null) {
                Map<Component,Component>  comps =  packageToComponent.get(pack.getName());
                if (comps == null) {
                    comps = new HashMap<Component,Component>();
                    packageToComponent.put(pack.getName(),comps);
                }
                Component c = new Component(issue.getComponent(),issue.getSubComponent());
                Component comp = comps.get(c);
                if (comp == null) {
                    comps.put(c,c);
                    comp = c;
                }
                comp.addIssue(id);
            }
        }
   }

   /** log components for exception reporter
    */
   static void logComponents(File file) throws  IOException {
        try {
            Connection connection = IssuezillaUtil.getConnection();
            Statement stmt = connection.createStatement();
            PreparedStatement pstmt = connection.prepareStatement("select distinct(subcomponent) from issue where component = ?");
            Properties props = new Properties();
            ResultSet rs = stmt.executeQuery("select distinct(component) from issue");
            StringBuilder subcomponents = new StringBuilder();
            while (rs.next()) {
                subcomponents.setLength(0);
                String component = rs.getString(1);
                pstmt.setString(1,component );
                ResultSet rs2 = pstmt.executeQuery();
                while (rs2.next()) {
                    if (subcomponents.length() != 0) {
                        subcomponents.append(",");
                    }
                    subcomponents.append(rs2.getString(1));
                }
                props.put(component, subcomponents.toString());
             }
            
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps  = new PrintStream(fos);
            try {
                props.store(ps,null);
            } finally {
                ps.close();
            }
        } catch (SQLException ex) {
            // rather ignore exceptions
            logger.log(Level.INFO, "Generation components list",ex);
        }
    }

   private  void logPackagesMapping(File outFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(outFile));
        for (String pack :  packageToComponent.keySet()) {
            writer.write(pack + ";");
            Map<Component,Component> components = packageToComponent.get(pack);
            int totalIssue = 0;
            for (Component comp : components.values()) {
                totalIssue += comp.getIssuesSize();
            }
            List<Component> sortedComps = new ArrayList<Component>(components.values());
            Collections.sort(sortedComps,new ComponentComparator());
            writer.append(totalIssue + ";");
            for (Component comp : sortedComps) {
                writer.print(comp.getComponent() + ";" + comp.getSubComponent() + ";" + comp.getIssuesSize() + ";");
            }
            writer.println("");
        }
        writer.close();
    }

    public boolean execute(Repository rep, RepositoryProcessEnv env) {
        try {
           for (SourceRoot srcRoot : rep.getSourceRoots()) {
               System.out.println("Source root: " + srcRoot.getRelPath());
               for (Package pack : srcRoot.getPackages()) {
                   System.out.println("Package: " + pack);
                   noIssue = 0;
                   enhacements = 0;
                   defects = 0;
                   features = 0;
                   for (JavaFile jf : pack.getJavaFiles()) {
                       CVSMetric cvs = jf.getCVSResultMetric();
                       if (cvs != null) {
                           Version ver = cvs.getRootVersion();
                           addRecursiveIssue(pack,ver);
                           jf.setMetric(cvs);
                       }
                   }
//                   System.out.println("Def: " + ui.defects + ", Enh: " + ui.enhacements + ", NoIssue: " + ui.noIssue + "Features: " + ui.features);
               }
           }
           // log mapping package -> issuezilla's component + subcomponent for Sedek's Excpetions
           // project
           logPackagesMapping(new File(env.getWorkDir(),"componentMappings.txt"));
           logComponents(new File(env.getWorkDir(),"components.properties"));
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(),ioe);
            
        } catch (SQLException sqle) {
            logger.log(Level.SEVERE, sqle.getMessage(),sqle);
            
        }
        return true;
    }

    public String getName() {
        return "UpdateIssue";
    }

    public String getDescription() {
        return "Update and persists issues id's to CVSMetrics";
    }

   
}
