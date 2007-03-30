/*
 * JavaFile.java
 *
 * Created on August 12, 2003, 2:56 PM
 */

package org.codeviation.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalStateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.CvsUtil;
import org.codeviation.model.vcs.Diff;
import org.codeviation.model.Version;
import org.codeviation.model.Position;

/**
 * JavaFile in package
 * @author  pz97949
 */
public final class JavaFile {
    String name;
    Package pack;
    Map<Class, Metric> staticMetrics = new HashMap<Class,Metric>();
    // We have to hold cvs history for other metrics
    CVSMetric cvsMetric; 
    public static final Logger logger = Logger.getLogger(JavaFile.class.getName()); 
    
    /** offsets of windows two byte line separators
     */
    private int winSeparators[];

    /** last computed lines with version
     */
    private List<Line> cachedLines;
    private Version cachedVersion;
    
    /** Creates a new instance of JavaFile */
    JavaFile(Package pack,String name) {
        this.pack = pack;
        this.name = name;
    }
    
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof JavaFile && obj != null) {
            JavaFile jf = (JavaFile)obj;
            ret = getName().equals(jf.getName()) && pack.equals(jf.getPackage());
        }
        return ret;
    }
    
    
    public int hashCode() {
        return name.hashCode() + (getPackage().hashCode() >> 3);
    }
    /** @return file name with extension
     */ 
    public String getName() {
        return name;
    }
    
    public String getClassName() {
        return getPackage().getName() + "." + name.substring(0,name.length() - ".java".length());
    }
    
    public Package getPackage() {
        return pack;
    }
  

    /** get all diffs for this java file 
     */
    public Diff [] getDiffs() throws IOException {
        CVSMetric cvsm = getCVSResultMetric();
        return  cvsm.getAllDiffs();
    }
    
    /** @return relative path in cvs 
     */
    public String getCVSPath() {
        return pack.getCVSPath() + name;
    }
    /** get RealFile on disk in cvs 
     */
    public File getCVSFile() {
        return new File (pack.getCVSPath(),getName());
    }        
    
    public int[] getBugs() throws IOException {
        Set<Integer> bugs = new TreeSet<Integer>();
        for (Diff diff : getDiffs()) { 
            diff.getVersion2().getDefectNumbers();
            for (int bug : diff.getVersion2().getDefectNumbers()) {
                bugs.add(bug);
            }
        }
        int ret[] = new int[bugs.size()];
        int i = 0;
        for (Integer bug : bugs) {
            ret[i++] = bug;
        }
        return ret;
    }
    
    public CVSMetric getCVSResultMetric() {
          if (cvsMetric == null) {
              cvsMetric = getMetric(CVSMetric.class);
          }  
          return cvsMetric;
    }
    public void setCVSMetric(CVSMetric cvsMetric) {
        this.cvsMetric = cvsMetric;
    }
    public <T extends Metric> T  getMetric(Class<T> type) {
        //  JavaFileUtil.currentJavaFile is shared for storing metrics!!! 
        synchronized (JavaFile.class) {
            if (type == CVSMetric.class && cvsMetric != null) {
                return (T) cvsMetric;
            } 
            JavaFileUtil.setCurrentJavaFile(this);
            T mr =  getPackage().getSourceRoot().getMetric(getPackage().getName(),getName(),type);
            if (mr == null) {
               // try get non persistent metric
                mr = (T) staticMetrics.get(type);
                if (mr == null) {
                    // try create new
                    mr = (T)StaticMetricFactory.getMetric(this,type);
                    if (mr != null) {
                        staticMetrics.put((Class<Metric>)type,mr);
                        if (mr instanceof StaticMetric) {
                            ((StaticMetric)mr).setJavaFile(this);
                        }
                        setMetric(mr);
                    }
                } 
            } else {
                if (mr instanceof StaticMetric) {
                    ((StaticMetric)mr).setJavaFile(this);
                }
            }
            return mr;
        }
    }
    
    public List<Metric> getAllMetrics() {
        //only alredy initialized non presinsten Metrics are returned
        List<Metric> mrs = new ArrayList<Metric>();
        mrs.addAll(staticMetrics.values());
        mrs.addAll(getPackage().getSourceRoot().getMetrics(getPackage().getName(),getName()));
        return mrs;
    }
    
    
    public void setMetric(Metric mr) {
        if (mr.isPersistent()) {
            getPackage().getSourceRoot().setMetric(getPackage().getName(),getName(),mr);
        }
    }
    
    /** @return null if the file is not available in current cvs
     */
    public String getCVSVersionName() {
        File cvsFile = getCVSFile();
        String verName = null;
        if (cvsFile.exists()) {
            CvsUtil cvsUtil = CvsUtil.getCvsUtil(getCVSFile().getParentFile());
            verName =  cvsUtil.getVersion(getName());
            if (verName == null) {
                CVSMetric cvsm = getCVSResultMetric();
                if (cvsm != null) {
                    Version v = cvsm.getRootVersion();
                    while (v.getNext() != null) {
                        v = v.getNext();
                    }
                    verName = v.getRevision();
                }
            }
        }
        return verName;
    }
    /** @return null if version doesn't exist, for example the file is not available in CVS.
     */
    public Version getCVSVersion() {
      String cvsVersion = getCVSVersionName();
      return (cvsVersion != null ) ? getCVSResultMetric().getRootVersion().getVersion(cvsVersion) : null;
    }
    
    public synchronized List<Line>  getLines(Version v) {
        try {
            if (cachedVersion == v) {
                return new ArrayList<Line>(cachedLines);
            }
            CVSMetric cvsm = getCVSResultMetric();
            if (cvsm.getRootVersion().equals(v)) {
                return new ArrayList<Line>();
            }
            Diff diffs[] = getDiffs();
            // reconstruct file file  for version v
            List<Line> lines = new ArrayList<Line>(); 
            for (Diff d : diffs) {
                lines = d.patch(lines);
                if (d.getVersion2().equals(v)) {
                      cachedVersion = v;
                      cachedLines = new ArrayList<Line>(lines);
                      return lines;
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        throw new IllegalStateException("Version " + v + " not found");
    }
    
    /** @return  position for current version
     *  Converts absolute file position to internal Position
     */
    public Position getPosition(int offset) {
        offset = unifyAbsolutePosition(offset);
        Version v = getCVSVersion();
        List<Line> lines = getLines(v);
        return Line.getPosition(lines,v,offset);
    }

    /** size of( [0A,0d]) = 1
     */ 
    private  int unifyAbsolutePosition(int offset) {
        if (winSeparators == null) {
            List<Integer> separators = new ArrayList<Integer>();
            byte data[] = null;
            try {
                InputStream fis = new FileInputStream(getCVSFile());
                 data = new byte[(int)getCVSFile().length()];
                fis.read(data);
                fis.close();
 
                int size = data.length - 1;
                for (int i  = 0 ; i <  size ; i++) {
                    if (data[i] == 0x0d && data[i+1] == 0x0a) {
                        separators.add(Integer.valueOf(i));
                    }
                }
                
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(),e);
            } 
            // copy Integers to ints
            winSeparators = new int[separators.size()];
            for (int i = 0 ; i < separators.size() ; i++) {
                winSeparators[i] = separators.get(i);
            }
        }
        int retOffset = offset;
        for (int i = 0 ; i < winSeparators.length ; i++) {
            if (offset > winSeparators[i]) {
                retOffset--;
            } else {
                break;
            }
        }
        return retOffset;
    }
    /** get javaFile from cvs
     */
    public static JavaFile getJavaFile(File file,String packageName) {
        try {
            SourceRoot srcRoot = PersistenceManager.getDefault().getOrCreateSourceRoot(file,packageName);
            String path = file.getParentFile().getCanonicalPath(); 
            String rootPath = srcRoot.getCvsDir().getAbsolutePath(); 
            assert path.startsWith(rootPath);
            return srcRoot.getPackage(packageName).getJavaFile(file.getName()); 
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public String toString() {
        return name;
    }
}
