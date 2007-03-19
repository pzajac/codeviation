

package org.codeviation.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.tasks.PageRankMetric;
import org.codeviation.tasks.SourceRootFilter;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.javac.CVSVersionsByPant;
import org.codeviation.javac.UsageItem;
import org.codeviation.javac.UsagesMetric;

/**
 * Generates sparse matrix for  Class -> Class mappings
 * @author pzajac
 */
public class ClassRankMatrixGenerator {
    private static final double CONVERGENCE_EPSILON = 1e-12;
    private  Map<String,ClassItem> usedClasses = new HashMap<String,ClassItem>();
    private FlexCompRowMatrix matrix;
    private int index = 0;
    /** if (exists link from a class the row[i] = true
     */
    private boolean rows [];  
    private double alpha;
    private String tagName;
    /** Index in matrix
     */
    private static class ClassItem implements Comparable<ClassItem>{
        String className;
        int index;
        double rank;
        Package pack;
        public ClassItem(String className, int index,Package pack) {
            this.className = className;
            this.index = index;
            this.pack = pack;
        }
        
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final ClassItem test = (ClassItem) o;

            if (this.className != test.className && this.className != null &&
                !this.className.equals(test.className))
                return false;
            return true;
        }
        public JavaFile getJavaFile() {
             String name = className.substring(pack.getName().length() + 1);
             int dot = name.indexOf('.');
             if (dot != -1 ) {
                 name = name.substring(0,dot);
             }
             return pack.getJavaFile(name + ".java");
        }
        
        public int hashCode() {
            return  className.hashCode();
        }
        public int getIndex() {
            return index;
        }
        
        public void setRank(double rank) {
            this.rank = rank;
        }
        public double getRank() {
            return rank;
        }
        public int compareTo(ClassItem ci) {
            double diff = ci.rank - rank;
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }
        Package getPackage() {
            return pack;
        }
       
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public void initClass(String className) {
        if (!usedClasses.containsKey(className)) {
            usedClasses.put(className,new ClassItem(className,index++,null));
        }
    }
    public void initClass(String className,Package pack) {
        if (!usedClasses.containsKey(className)) {
            usedClasses.put(className,new ClassItem(className,index++,pack));
        }
    }
    
    public int getClassIndex(String className) {
        ClassItem item = usedClasses.get(className);
        return  (item != null) ? item.getIndex() : -1; 
    } 
    public void initClasses(SourceRoot srcRoot) {
        for (Package pack : srcRoot.getPackages()) {
            for (JavaFile jf : pack.getJavaFiles()) {
                if (tagName == null) {
                    initClass(jf.getClassName(),pack);
                } else {
                    CVSVersionsByPant cvbp = jf.getMetric(CVSVersionsByPant.class);
                    if (cvbp.getVersion(tagName) != null) {
                        initClass(jf.getClassName(),pack);
                    }
                }
            }
        }
    }
    
    public void addUsage(SourceRoot srcRoot) {
        for (Package pack : srcRoot.getPackages()) {
            for (JavaFile jf : pack.getJavaFiles()) {
                UsagesMetric um = jf.getMetric(UsagesMetric.class);
                CVSVersionsByPant cvbp = jf.getMetric(CVSVersionsByPant.class);
                Version ver = null;
                if (cvbp != null) {
                    ver = cvbp.getVersion(tagName);
                }
                if (um != null) {
                    for (PositionIntervalResult<UsageItem> pir : um.getStorage().getAllObjects()) {
                        if (ver != null && um.getStorage().get(pir).contains(ver)) {
                            addUsage(jf.getClassName() , pir.getObject().getClazz());
                        }
                    }
                }
            }
        }
    }
    public void normalizeMatrix(double alpha) {
        double emptyRation = 1.0 /usedClasses.size();
        this.alpha = alpha;
        for (int r = 0 ; r < matrix.numRows() ; r++ ) {
            SparseVector row = matrix.getRow(r);
            int used = row.getUsed();
            if (used != 0) {
                double ratio = alpha * 1.0/used;
                for (int c : row.getIndex()) {
                    matrix.set(r, c, ratio);
                }
                rows[r] = true;
            } else {
                rows[r] = false;
            }
        }
    }
    public void addUsage(String className,String toClass) {
            if (rows == null ) {
                rows = new boolean[usedClasses.size()];
            }
            if (matrix == null) {
                matrix = new FlexCompRowMatrix(usedClasses.size(),usedClasses.size());
            }    
            int row = getClassIndex(className);
            int column = getClassIndex(toClass);
            if (row != -1 && column != -1 && row != column) {
               matrix.set(row, column, 1.0);
            }
    }
    
    public Matrix getMatrix() {
        return matrix;
    }
    
    /** @return total number of nodes in graph
     */ 
    public int size() {
       return usedClasses.size();  
    }
    
    /** computes first eigen vector
     * @return number of iteration or -1 
     * @param initV initial eigen value vector. Value of last  
     */
    public int compute(Vector initV) {
        Vector workV1 = new DenseVector(initV);  
        int size = initV.size();
        double beta = (1 - alpha) /size;
        double prevAbsVal = 1e6;
        for (int it = 0 ; it < 200 ; it++) {
            // initV = (matrix*workv + ee*workv)/A
            
            //initV*matrix 
            matrix.transMult(initV,workV1);
            
            //initV*ee
            double sumEmpty = 0.0;
            double sumUsed = 0.0;
            for (int i = 0 ; i < initV.size() ; i++) {
                double value = initV.get(i); 
                if (rows[i]) {
                    sumUsed += value;
                } else {
                    sumEmpty += value; 
                }
            }
            sumUsed *= beta;
            sumEmpty *= 1./rows.length;
            double sum = sumEmpty + sumUsed;
            for(int i = 0 ; i < workV1.size() ; i++) {
                workV1.add(i, sum );
            }
            double absVal = 0;
            
            for (int i = 0  ; i < size ; i++) {
                double value = workV1.get(i);
                absVal += Math.abs(value);
            }
            workV1.scale(1.0/absVal); 
                      
            initV.set(workV1);
            if (Math.abs(prevAbsVal - absVal) < CONVERGENCE_EPSILON && it > 70) {
                // set rank
               for (ClassItem ci : usedClasses.values()) {
                 ci.setRank(initV.get(ci.getIndex()));  
               }
               return it;
            }
            prevAbsVal = absVal;
            
        }
        return -1;
    }
    public Vector compute (List<SourceRoot> roots) {
        for (SourceRoot srcRoot : roots) {
          initClasses(srcRoot);
        }
        if (matrix == null) {
            matrix = new FlexCompRowMatrix(usedClasses.size(),usedClasses.size());
        }    
        
       for (SourceRoot srcRoot : roots) {
            addUsage(srcRoot);
       }
       normalizeMatrix(0.9);
       Vector vec = new DenseVector (usedClasses.size());
       double value = 1.0/ usedClasses.size();
       for (int i = 0 ; i < usedClasses.size() ; i++ ) {
            vec.set(i, value);
       }
       long startTime = System.currentTimeMillis();
       if (usedClasses.isEmpty()) {
           throw new IllegalStateException("No class for pageRank");
       }
       if (compute(vec) == -1 ) {
           throw new IllegalStateException("Convergence problem");
       }
       long endTime = System.currentTimeMillis();
       return vec;
    }
    
    public void computeAndStore(Repository rep, String tag,SourceRootFilter filter) {
       index = 0;
       matrix = null;
       rows = null;
       setTagName(tag);
       usedClasses.clear();
       List<SourceRoot> roots = new ArrayList<SourceRoot>();
       for (SourceRoot root : rep.getSourceRoots()) {
           if (filter == null || filter.accept(root)) {
              roots.add(root);
           }
       }
       Vector vec = compute(roots);
       // store results
       List<ClassItem> allItems = new ArrayList<ClassItem>(usedClasses.values());
       Collections.sort(allItems);
       for (int i = 0 ; i < allItems.size() ; i++) {
           ClassItem item = allItems.get(i);
           JavaFile jf = item.getJavaFile();
           PageRankMetric prm = jf.getMetric(PageRankMetric.class);
           if (prm == null ) {
               prm = new PageRankMetric();
               prm.setJavaFile(jf);
           }
           CVSVersionsByPant cvsbp = jf.getMetric(CVSVersionsByPant.class);
           if (cvsbp == null) {
               cvsbp = new CVSVersionsByPant();
               cvsbp.setJavaFile(jf);
           }
           Version v = cvsbp.getVersion(tag);
           if (v != null) {
              prm.put(tag, item.getRank(),i,((double)i)/allItems.size());
              jf.setMetric(prm);
           }
       }
              
    } 
    public static void main(String args[]) throws NotConvergedException {
        if (System.getProperty(PersistenceManager.PANT_CACHE_FOLDER) == null) {
           // XXX
            System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, "/cvss/pantcache");
       }
       CVSMetric.setUpdateCVS(false);
       System.out.println("Creating matrix");
       Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
       List<SourceRoot> roots = new ArrayList<SourceRoot>();
       for (SourceRoot root : rep.getSourceRoots()) {
           roots.add(root);
       }
//       roots.add(rep.getSourceRoot("openide/fs/src"));
//       roots.add(rep.getSourceRoot("openide/masterfs/src"));
//       roots.add(rep.getSourceRoot("openide/loaders/src"));
//       roots.add(rep.getSourceRoot("openide/nodesop/src"));
//       roots.add(rep.getSourceRoot("openide/explorer/src"));
//       roots.add(rep.getSourceRoot("editor/src"));
//       roots.add(rep.getSourceRoot("apisupport/project/src"));
    
       ClassRankMatrixGenerator generator = new ClassRankMatrixGenerator();
       Vector vec = generator.compute(roots);
       
       // sort 
       List<ClassItem> allItems = new ArrayList<ClassItem>(generator.usedClasses.values());
       Collections.sort(allItems);

       for (ClassItem item : allItems) {
           System.out.println(item.className + ", " + item.getRank() + " " );
       }
       System.out.println("total classes:" + allItems.size());
    }
}
