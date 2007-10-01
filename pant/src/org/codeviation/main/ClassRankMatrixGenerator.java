

package org.codeviation.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.PositionVersionIntervalResultContainer;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.tasks.PageRankMetric;
import org.codeviation.tasks.SourceRootFilter;
import org.codeviation.javac.CVSVersionsByPant;
import org.codeviation.javac.UsageItem;
import org.codeviation.javac.UsagesMetric;
import org.codeviation.javac.impl.blocks.BlocksMetric;
import org.codeviation.model.PositionIntervalResultGraph;

/**
 * Generates sparse matrix for  Class -> Class mappings
 * @author pzajac
 */
public class ClassRankMatrixGenerator {
    private static final double CONVERGENCE_EPSILON = 1e-12;
    private  Map<String,Item> usedItems = new HashMap<String,Item>();
    private FlexCompRowMatrix matrix;
    private int index = 0;
    /** if (exists link from a class the row[i] = true
     */
    private boolean rows [];  
    private double alpha;
    private String tagName;
    
    final ElementType type;
    public static enum ElementType {
        CLASS,
        METHOD
    }
    /** Index in matrix
     */
    private static class Item implements Comparable<Item>{
        String className;
        String methodName;
        int index;
        double rank;
        Package pack;
        ElementType type = ElementType.CLASS;
        
        public Item(String className,String methodName, int index,Package pack,ElementType type) {
            this.className = className;
            this.index = index;
            this.pack = pack;
            this.type = type;
            this.methodName = methodName;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Item test = (Item) o;

            if ( this.className != null &&
                !this.className.equals(test.className)) {
                return false;
            }
            if (this.methodName != null && !this.methodName.equals(test.methodName)) {
                return false;
            }
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
        
        @Override
        public int hashCode() {
            int hc = className.hashCode();
            if (methodName != null) {
                hc += methodName.hashCode();
            }
            return hc ;
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
        public int compareTo(Item ci) {
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
       
    } // class Item

    public ClassRankMatrixGenerator(ElementType type) {
        this.type = type;
    }
    
    public ClassRankMatrixGenerator() {
        this.type = ElementType.CLASS;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public void initItem(String itemName,String methodName) {
        if (!usedItems.containsKey(itemName)) {
            usedItems.put(itemName,new Item(itemName,methodName,index++,null,type));
        }
    }
    public void initItem(String itemName,String methodName, Package pack) {
        if (!usedItems.containsKey(itemName)) {
            usedItems.put(itemName,new Item(itemName,methodName,index++,pack,type));
        }
    }
    
    public int getItemIndex(String itemName) {
        Item item = usedItems.get(itemName);
        return  (item != null) ? item.getIndex() : -1; 
    }

  
    private void initItem(SourceRoot srcRoot) {
        for (Package pack : srcRoot.getPackages()) {
            for (JavaFile jf : pack.getJavaFiles()) {
                CVSVersionsByPant cvbp = jf.getMetric(CVSVersionsByPant.class);
                Version v = null;
                if (cvbp != null) {
                    v = cvbp.getVersion(tagName) ;
                }
                if (type == ElementType.CLASS) {
                    // classses
                    if (tagName == null) {
                        initItem(jf.getClassName(),null,pack);
                    } else {
                        if (v != null) {
                            initItem(jf.getClassName(),null,pack);
                        }
                    }
                } else {
                    // methods
                    if (tagName == null) {
                        throw new IllegalStateException("not supported for multirevisions ClassRank");
                    } else {
                        BlocksMetric bm = jf.getMetric(BlocksMetric.class);
                        PositionVersionIntervalResultContainer<String> classes = bm.getClasses();
                        PositionVersionIntervalResultContainer<String> methods = bm.getMethods();
                        List<PositionVersionIntervalResultContainer<?>> list = new ArrayList<PositionVersionIntervalResultContainer<?>>();
                        list.add(classes);
                        list.add(methods);
                        PositionIntervalResultGraph pirg = PositionIntervalResultGraph.createGraph(list, v, 1);
                        for (PositionIntervalResultGraph.Item item : pirg.getItems(1)) {
                            PositionIntervalResultGraph.Item parent = item.getParent();
                            if (parent != null) {
                                initItem((String)parent.getPir().getObject(), (String)item.getPir().getObject());
                            }
                        }
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
                    PositionIntervalResultGraph pirg = null;
                    Map <PositionIntervalResult<String>,String> method2Class = null;
                    if (type == ElementType.METHOD) {
                        BlocksMetric bm = jf.getMetric(BlocksMetric.class);
                        // init method to class
                        if (bm != null) {
                            PositionVersionIntervalResultContainer<String> classes = bm.getClasses();
                            PositionVersionIntervalResultContainer<String> methods = bm.getMethods();
                            List<PositionVersionIntervalResultContainer<?>> list = new ArrayList<PositionVersionIntervalResultContainer<?>>();
                            list.add(classes);
                            list.add(methods);
                            list.add(um.getStorage());
                            pirg = PositionIntervalResultGraph.createGraph(list, ver, 1);
                        }
                    }
                    if (type == ElementType.CLASS) {
                        for (PositionIntervalResult<UsageItem> pir : um.getStorage().getAllObjects()) {
                            if (ver != null && um.getStorage().get(pir).contains(ver)) {
                                if (type == ElementType.CLASS) {
                                    addClassUsage(jf.getClassName() , pir.getObject().getClazz());
                                } 
                            }
                        }
                    } else {
                        if (pirg != null) {
                            for (PositionIntervalResultGraph.Item item : pirg.getItems(2) ) {
                                PositionIntervalResultGraph.Item method = item.getParent();
                                if (method != null) {
                                    PositionIntervalResultGraph.Item clazz = method.getParent();
                                    if (clazz != null) {
                                        addMethodUsage((PositionIntervalResult<UsageItem>)item.getPir(),
                                                (String) clazz.getPir().getObject(),
                                                (String) method.getPir().getObject());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void normalizeMatrix(double alpha) {
        double emptyRation = 1.0 /usedItems.size();
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
    public void addClassUsage(String className,String toClass) {
            if (rows == null ) {
                rows = new boolean[usedItems.size()];
            }
            if (matrix == null) {
                matrix = new FlexCompRowMatrix(usedItems.size(),usedItems.size());
            }    
            int row = getItemIndex(className);
            int column = getItemIndex(toClass);
            if (row != -1 && column != -1 && row != column) {
               matrix.set(row, column, 1.0);
            }
    }
    
   public  void addMethodUsage(PositionIntervalResult<UsageItem> pir, String clazz,String method) {
        UsageItem usage = pir.getObject();
        String usageStr = usage.toString();
        String key = clazz + "." + method;
        int row = getItemIndex(usageStr);
        int column = getItemIndex(key);
        if (row != -1 && column != -1 ) {
            matrix.set(row,column,1.0);
        }
    }
    public Matrix getMatrix() {
        return matrix;
    }
    
    /** @return total number of nodes in graph
     */ 
    public int size() {
       return usedItems.size();  
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
               for (Item ci : usedItems.values()) {
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
          initItem(srcRoot);
        }
        if (matrix == null) {
            matrix = new FlexCompRowMatrix(usedItems.size(),usedItems.size());
        }    
        
       for (SourceRoot srcRoot : roots) {
            addUsage(srcRoot);
       }
       normalizeMatrix(0.9);
       Vector vec = new DenseVector (usedItems.size());
       double value = 1.0/ usedItems.size();
       for (int i = 0 ; i < usedItems.size() ; i++ ) {
            vec.set(i, value);
       }
       if (usedItems.isEmpty()) {
           throw new IllegalStateException("No class for pageRank");
       }
       if (compute(vec) == -1 ) {
           throw new IllegalStateException("Convergence problem");
       }
       return vec;
    }
    
    public void computeAndStore(Repository rep, String tag,SourceRootFilter filter) {
       index = 0;
       matrix = null;
       rows = null;
       setTagName(tag);
       usedItems.clear();
       List<SourceRoot> roots = new ArrayList<SourceRoot>();
       for (SourceRoot root : rep.getSourceRoots()) {
           if (filter == null || filter.accept(root)) {
              roots.add(root);
           }
       }
       Vector vec = compute(roots);
       // store results
       if (type == ElementType.CLASS) {
           List<Item> allItems = new ArrayList<Item>(usedItems.values());
           Collections.sort(allItems);
           for (int i = 0 ; i < allItems.size() ; i++) {
               Item item = allItems.get(i);
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
       } else {
           // store methods
           
       }
              
    } 
//    public static void main(String args[]) throws NotConvergedException {
//        if (System.getProperty(PersistenceManager.PANT_CACHE_FOLDER) == null) {
//           // XXX
//            System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, "/cvss/pantcache");
//       }
//       CVSMetric.setUpdateCVS(false);
//       System.out.println("Creating matrix");
//       Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
//       List<SourceRoot> roots = new ArrayList<SourceRoot>();
//       for (SourceRoot root : rep.getSourceRoots()) {
//           roots.add(root);
//       }
////       roots.add(rep.getSourceRoot("openide/fs/src"));
////       roots.add(rep.getSourceRoot("openide/masterfs/src"));
////       roots.add(rep.getSourceRoot("openide/loaders/src"));
////       roots.add(rep.getSourceRoot("openide/nodesop/src"));
////       roots.add(rep.getSourceRoot("openide/explorer/src"));
////       roots.add(rep.getSourceRoot("editor/src"));
////       roots.add(rep.getSourceRoot("apisupport/project/src"));
//    
//       ClassRankMatrixGenerator generator = new ClassRankMatrixGenerator();
//       Vector vec = generator.compute(roots);
//       
//       // sort 
//       List<ClassItem> allItems = new ArrayList<ClassItem>(generator.usedClasses.values());
//       Collections.sort(allItems);
//
//       for (ClassItem item : allItems) {
//           System.out.println(item.className + ", " + item.getRank() + " " );
//       }
//       System.out.println("total classes:" + allItems.size());
//    }
}
