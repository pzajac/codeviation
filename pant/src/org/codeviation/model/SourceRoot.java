/*
 * SourceRoot.java
 *
 * Created on October 29, 2006, 10:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.javac.CVSVersionsByPant;
import org.codeviation.javac.MetricUtil;
import org.codeviation.model.Metric;
import java.util.logging.Logger;
import org.codeviation.main.PrepareNbTags;

/**
 * Representation of one source root processed by javac. 
 */
public final  class SourceRoot {
    Repository rep;
    /** relpath in repositry cvs
     */
    String cvsRelPath;
    /** Last used cvs tag for better performance
     */
    String lastCvsTag; 
   
   
    private static String PACKAGE_EXT = "_package_";
    private static String JAVA_METRICS_EXT = "_javametric_";
    private static String PACKAGE_METRICS_EXT = "_packagemetric_";
    private static String ROOT_METRICS_EXT = "_rootmetric_";
    private static  String CVS_TAGS_FILE_NANE = "cvstags.ser";
    
    private static final Logger log = Logger.getLogger(MetricUtil.LOGGER);
    
    /** packageName -> package */
    Map<String,Package> packages = null;
    /** Creates a new instance of SourceRoot 
     */
    SourceRoot(Repository rep,String cvsRelPath) {
        this.rep = rep;
        this.cvsRelPath = cvsRelPath;
        File cvsDir = getCvsDir();
// cvs is dead        
//        if (!cvsDir.isDirectory()) {
//            throw new IllegalStateException("no folder for source root cvs " + rep.getCvsRoot() + "," + cvsRelPath);
//        }
        File cacheDir = getCacheDir();
        cacheDir.mkdirs();
        if (!cacheDir.isDirectory()) {
            throw new IllegalStateException("No cache dir for " + cacheDir);
        }
    }

    public String getRelPath() {
        return cvsRelPath;
    }
    public File getCvsDir() {
        return  new File(rep.getCvsRoot(),cvsRelPath);
    }
    
    public File getCacheDir() {
        return new File(rep.getCacheRoot(),cvsRelPath);
    }
    /** @return cache folder for package
     */ 
    File getPackageFile(String name) {
        File pkg =  new File(getCacheDir(),name + PACKAGE_EXT);        
        pkg.mkdirs();
        return pkg;
    }
    
    public Package getPackage(String name) {
        
        Package p = getPackagesMap().get(name);
        if (p == null) {
            File pFile = getPackageFile(name);
            if (pFile != null) {
                p = new Package(this,name);
                getPackagesMap().put(name,p);
            }
        }
        return p;
    }
    
    private Map<String,Package> getPackagesMap() {
        if (packages == null) {
            packages = new TreeMap<String,Package>();
            // read packages
            for (File file :getCacheDir().listFiles()) {
                if (file.getName().endsWith(PACKAGE_EXT)) {
                    String name = file.getName();
                    name = name.substring(0,name.length() - PACKAGE_EXT.length());
                    packages.put(name,new Package(this,name));
                }
            }
        }
        return packages;
    }
    public List<Package> getPackages() {
        return new ArrayList<Package>(getPackagesMap().values());
    }
    public List<String> getPackageNames() {
        String fileNames [] =  getCacheDir().list();
        List<String> packageNames = new ArrayList<String>();
        for(String name : fileNames) {
            if (name.endsWith(PACKAGE_EXT)) {
                packageNames.add(name.substring(0,name.length() - PACKAGE_EXT.length()));
            }
        }
        return packageNames;
    }  
    
    public <T extends Metric> T getMetric(String packageName,String fileName,Class<T> clazz){
        File pfile = getPackageFile(packageName);
        File f = new File(pfile,fileName + "_" + clazz.getName() + JAVA_METRICS_EXT);
        log.fine("getMetric:" + f.getAbsolutePath());
        if (f.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                try {
                  return (T)ois.readObject();  
                } finally {
                    ois.close();
                }
            } catch (Exception e) {
                log(e);
            } 
        }
        return null;
    }
    
    public List<Metric> getMetrics(String packageName,String file) {
        File pfile = getPackageFile(packageName);
        File metrics[] = pfile.listFiles();
        List<Metric> results = new ArrayList<Metric>();
        for (File f : metrics) {
            String name = f.getName();
            if (name.startsWith(file + "_") && name.endsWith(JAVA_METRICS_EXT)) {
                try {
                    log.fine("readMetric:" + f.getPath());
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                    try {
                      results.add((Metric)ois.readObject());  
                    } finally {
                        ois.close();
                    }
                } catch (Exception e) {
                    log(e);
                } 

            } 
        }
        return results;        
    }
    
    public <T extends Metric> void setMetric(String packageName,String name, T mr) {
           File pn = getPackageFile(packageName);
           File of = new File(pn,name + "_" + mr.getClass().getName() + JAVA_METRICS_EXT);
           log.fine("writeMetric: " + of );
           try {
               ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(of));
               try {
                   oos.writeObject(mr);
               } finally {
                   oos.close();
               }
           } catch (IOException ioe) {
               log(ioe);
           }
    }
    private void log(Exception e) {
        Logger.getLogger(MetricUtil.LOGGER).log(Level.SEVERE, "SourceRoot", e);
    }
    public String toString() {
        return cvsRelPath;
    }
    
    /** Metrics runner add global cvs tag if exists
     */
    public void addCvsTag(String cvsTag) {
        if (cvsTag != null && !cvsTag.equals(lastCvsTag)) {
            Set<String> tags = getCvsTags();
            tags.add(cvsTag);
            // store tags
            try {
                ObjectOutputStream oos  = new ObjectOutputStream(new FileOutputStream( new File(getCacheDir(),CVS_TAGS_FILE_NANE)));
                try {
                    oos.writeObject(tags);
                    lastCvsTag = cvsTag;
                } finally  {
                    oos.close();
                }
            } catch (IOException ioe) {
                log.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }
    }
    
    /** get all cvs tags processed by path
     */
    public Set<String> getCvsTags() {
        Set<String> tags = null;
        try {
            File tagsFile = new File(getCacheDir(),CVS_TAGS_FILE_NANE);
            if (tagsFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tagsFile ));
                tags =  (Set<String>) ois.readObject();
                if (tags.contains(null)) {
                    log.log(Level.SEVERE,"null tags " + getRelPath());
                    tags = null;
                }
            } 
            
        } catch (ClassNotFoundException cnfe) {
            log.log(Level.SEVERE, cnfe.getMessage(), cnfe);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
        if (tags == null) {
                // create default 
                tags = new HashSet<String>();
                for (Package pack :getPackages()) {
                    for (JavaFile jf : pack.getJavaFiles()) {
                        CVSVersionsByPant cvbp = jf.getMetric(CVSVersionsByPant.class);
                        if (cvbp != null) {
                            tags.addAll(cvbp.getTags());
                        }
                    }
                }
                try {
                    ObjectOutputStream oos  = new ObjectOutputStream(new FileOutputStream( new File(getCacheDir(),CVS_TAGS_FILE_NANE)));
                    oos.writeObject(tags);
                   oos.close();
                } catch (IOException ioe) {
                     log.log(Level.SEVERE, ioe.getMessage(), ioe);
                }
        }    
        return tags;
    }
    
    /** get cvs tag for actual branch
     */ 
    public String getCvsTag(Date date) {
        // XXX supported only nbcvs
        if (rep.getName().equals("nbcvs")) {
             Set<String> tags = getCvsTags();
             Date usedTagDate = null; 
             String usedTag = null;
             for (String tag : tags) {
                try {
                    Date tagDate = PrepareNbTags.parseDate(tag.substring(3));
                    if (date.compareTo(tagDate) > 0) {
                        if (usedTagDate == null || usedTagDate.compareTo(tagDate) < 0) {
                            usedTagDate = tagDate;
                            usedTag = tag;
                        }
                    }
                    
                } catch (ParseException ex) {
                    log(ex);
                }
             }
             return usedTag;
        } else {
            throw new UnsupportedOperationException("Not supported for non nborg repositories");
        }
    }
 
    /** XXX suported only nbcvs
     */
    public Date getMinTagDate() {    
        Set<String> tags = getCvsTags();
        Date usedTagDate = null; 
        for (String tag : tags) {
            Date tagDate = rep.getTagDate(tag);
            if (usedTagDate == null || usedTagDate.compareTo(tagDate) < 0) {
                usedTagDate = tagDate;
            }
        }
        return usedTagDate;
    }
    /** XXX suported only nbcvs
     */
    public Date getMaxTagDate() {
        Set<String> tags = getCvsTags();
        Date usedTagDate = null; 
        for (String tag : tags) {
            Date tagDate = rep.getTagDate(tag);
            if (usedTagDate == null || usedTagDate.compareTo(tagDate) > 0) {
                usedTagDate = tagDate;
            }
         }
         return usedTagDate;
    }
    
    public Repository getRepository() {
        return rep;
    }
    
}
