
package org.codeviation.tasks;

import java.awt.Event;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
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
import org.codeviation.math.AnotatedMatrix;
import org.codeviation.math.Matlab;

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
public class UsageOwnerIndexer implements  RepositoryProcess,Serializable {
     private static final long serialVersionUID = 1;
     
     static Logger logger = Logger.getLogger(UsageOwnerIndexer.class.getName());
     private Map<UsageItem,UsageItem> usages = new HashMap<UsageItem,UsageItem>();
     private Map<String,Map<UsageItem,Integer>> usagesOfUsers = new HashMap<String,Map<UsageItem,Integer>>();
     private File outDir;     
//     private static Matrix outMatrix[];
     
     static final String MATRIX_FILE_NAME = "SimpleMatrix";
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
    
    public AnotatedMatrix<String,ArrayList<UsageItem>>  generateMatrix(Map <UsageItem,Integer > usagesColumns,Map<String,Integer> userRows,String name) throws IOException {
        int columns = 0;
        for (Integer val : usagesColumns.values()) {
            if (columns < val ) {
                columns = val;
            }
        }
        columns++;
        
        FlexCompRowMatrix matrix = new FlexCompRowMatrix(userRows.size(),columns);
        for (Map.Entry<String,Map<UsageItem,Integer>> userEntry : usagesOfUsers.entrySet()) {
            int userRow = userRows.get(userEntry.getKey());
            for (Map.Entry<UsageItem,Integer> usageEntry : userEntry.getValue().entrySet()) {
                Integer column = usagesColumns.get(usageEntry.getKey());
                if (column != null) {
                    matrix.add(userRow, column, usageEntry.getValue());
                }
            }
        }
        
        // store matrix to m-file
        File matFile = new File(outDir,name);
        File rowsFile = new File (outDir,name + ".rows");
        File columnsFile = new File(outDir,name + ".columns");
        
        Matlab.toMFile(matrix, matFile);
        PrintWriter rowsWriter = new PrintWriter(new FileWriter(rowsFile));
        PrintWriter columnsWriter = new PrintWriter(new FileWriter(columnsFile));
        try {
            writeInversedMap(rowsWriter,userRows);
            writeInversedMap(columnsWriter,usagesColumns);

        } finally {
            rowsWriter.close();
            columnsWriter.close();
        }
        
        // create annotated matrix
        //
        List<ArrayList<UsageItem>> listColumns = new ArrayList<ArrayList<UsageItem>>(matrix.numColumns());
        for (int c = 0 ; c < matrix.numColumns(); c++) {
            listColumns.add(new ArrayList<UsageItem>());
        }
        for (Map.Entry<UsageItem, Integer> usageEntry : usagesColumns.entrySet()) {
            int val = usageEntry.getValue();
            if (val < matrix.numColumns()){
                listColumns.get(val).add(usageEntry.getKey());
            }
        }
        List<String> listUsers = new ArrayList<String>(userRows.size());
        for (int r = 0 ; r < matrix.numRows() ; r++) {
            listUsers.add("?");
        }
        for (Map.Entry<String, Integer> userEntry : userRows.entrySet()) {
            int val = userEntry.getValue();
            if (val < matrix.numRows()) {
                listUsers.set(val,userEntry.getKey());
            }
        }
        return new AnotatedMatrix<String,ArrayList<UsageItem>>(matrix,listUsers,listColumns);
       
    }
    
    enum ColumnType {
        CLASS,
        PACKAGE,
        METHOD
    }
    
    static interface  UsageFilter {
        public boolean match(UsageItem item) ;
    }
    private void preparaDate(Map <UsageItem,Integer > usagesColumns,Map<String,Integer> userRow,ColumnType type,UsageFilter filter) {
//     private Map<UsageItem,UsageItem> usages = new HashMap<UsageItem,UsageItem>();
//     private Map<String,Map<UsageItem,Integer>> usagesOfUsers = new HashMap<String,Map<UsageItem,Integer>>();
        int row = 0;
        for (String user : usagesOfUsers.keySet()) {
            userRow.put(user, row++);
        }
        
        int column = 0;
        Map<String,Integer> usageValues = new HashMap<String,Integer>(); 
        
        for (UsageItem usage : usages.keySet()) {
            if (filter == null || filter.match(usage)) {
                boolean found = false;
                String val = null;
                switch (type) {
                case CLASS:
                    val = usage.getClazz();
                    break;
                case METHOD:
                     val  = usage.getClazz() + "." + usage.getMethod();
                    break;
                case PACKAGE:
                    val = usage.getPackage();
                    break;
                }
                if (!usageValues.containsKey(val)) {
                    int colVal = column++;
                    usageValues.put(val,colVal);
                    usagesColumns.put(usage,colVal);
                } else {
                    usagesColumns.put(usage,usageValues.get(val));
                }
            }
        }
        
        
    }
    public boolean execute(Repository repository, RepositoryProcessEnv env) {
        try {
           // initialize out dir 
           clear();
           setOutDir(new File(env.getWorkDir(),
                   getName() + File.separator + repository.getName()));
           env.log(this, LogReason.START_PROCESS, repository.getName()); 
           for (SourceRoot srcRoot : repository.getSourceRoots()) {
               if (env.getSourceRootFilter() != null && !env.getSourceRootFilter().accept(srcRoot)) {
                   continue;
               }
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
           
           // XXX Matrix for Praks
           Map <UsageItem,Integer >  usagesColumns = new HashMap<UsageItem, Integer>();
           Map<String,Integer> userRow = new TreeMap<String, Integer>();
           
           preparaDate(usagesColumns,userRow,ColumnType.CLASS,new UsageFilter() {
                public boolean match(UsageItem item) {
                    // XXX
//                    return  item.getClazz().startsWith("java.") || item.getClazz().startsWith("javax.");
                    return true;
                }
           });
           // create matrix for datamining (LSI)
           AnotatedMatrix<String,ArrayList<UsageItem>> am = generateMatrix(usagesColumns, userRow, MATRIX_FILE_NAME);
           env.performRepositoryAction(this, new RepositoryProcessEnv.Event<AnotatedMatrix<String,ArrayList<UsageItem>>>(am));
           
        }  catch (IOException ioe) {
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
    
    
    private <T,KEY,VALUE extends Comparable> void writeInversedMap(PrintWriter writer,Map<KEY,VALUE> map) {
        List<Map.Entry<KEY,VALUE>> items = new ArrayList<Map.Entry<KEY, VALUE>>(map.entrySet());
        Collections.sort(items,new Comparator<Map.Entry<KEY,VALUE>>() {
            @SuppressWarnings("unchecked")
            public int compare(Entry<KEY, VALUE> o1, Entry<KEY, VALUE> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        VALUE val = null;
        for (Map.Entry<KEY,VALUE> item : items) {
            if (val != null) {
                if (val.equals(item.getValue())) {
                    writer.print(" , ");
                } else {
                    writer.println();
                }
            }
            val = item.getValue();
            writer.print(item.getKey());
        }
    }
}
