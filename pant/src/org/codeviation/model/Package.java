/*
 * Package.java
 *
 * Created on August 13, 2003, 10:26 AM
 */

package org.codeviation.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.vcs.CvsUtil;

/**
 * Package reprezents java package. Contains group of javafiles
 * @author  pz97949
 */
public final class Package {
    final private String name;
    WeakReference<List<JavaFile>> files;
    final private SourceRoot sourceRoot;
    private static final String JAVA_FILES_LIST_NAME = "javafiles.lst";
    /** Creates a new instance of Package */
    Package(SourceRoot sourceRoot,String name) {
        if (sourceRoot == null) {
            throw new NullPointerException();
        }
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.sourceRoot = sourceRoot;
    }
    /** return package name 
     * org.neco.neco
     */
    public String getName() {
        return name;
    }
    /** @return path of this package in cvs 
     * the path ends with File.separator
     */
    public String getCVSPath() {
        return sourceRoot.getCvsDir().getPath() + File.separatorChar +  name.replace('.',File.separatorChar) + File.separator;
    }
    
    public File getCVSFile() {
        return new File (getCVSPath());
    }
    /** get all javaFiles in package  
     *  @return Collection of JavaFile 
     */
    public List<JavaFile> getJavaFiles() {
        if (this.files != null ) {
             List<JavaFile> files = this.files.get();
             if (files != null) {
                 return files;
             }
        }
        File fileSourceDir = new File (getCVSPath());
//        if (!fileSourceDir.exists()) {
////            throw new IllegalStateException(fileSourceDir + ", exists: " );
//            // XXX IllegalStateException("/nbcvs/xml/core/src/org/w3c/dom, exists:");
//            // dynamically generated sources therefore return empty list
//            return Collections.emptyList();
//        }
        File files[] = fileSourceDir.exists() ? fileSourceDir.listFiles()
                                 : new File[0];
        List<JavaFile> javaFiles = new ArrayList<JavaFile>();
        
        Set<String> names = new HashSet<String>();
        
        for (File file : files ) {
            if (file.getName().endsWith(".java") ) {
                javaFiles.add(new JavaFile(this,file.getName()));
                names.add(file.getName());
            }
        }
        Set<String> cachedNames = readJavaFileNames();
        Set<String> tmpCache = new HashSet<String>(cachedNames);
        cachedNames.removeAll(names);
        for (String name : cachedNames) {
             javaFiles.add(new JavaFile(this,name));
        }
        cachedNames.addAll(names);
        if (!tmpCache.equals(cachedNames)) {
            storeJavaFileNames(cachedNames);
        }
        this.files = new WeakReference<List<JavaFile>>(javaFiles);
        return javaFiles;
    }
    
    public JavaFile getJavaFile(String name) {
        List<JavaFile> files = getJavaFiles();
        for (JavaFile f : files) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
    /** find JavaFile in this Package 
     */
    public JavaFile findJavaFile(String fileName) {
        Collection<JavaFile> files = getJavaFiles();
        for (JavaFile file :  files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }
    
    
    public int[] getBugs() throws IOException {
        Set<Integer> allBugs = new TreeSet<Integer>();
        for (JavaFile javaFile : getJavaFiles()) {
            for (int bug : javaFile.getBugs()) {
                allBugs.add(bug);
            }
        }
        int ret[] = new int[allBugs.size()];
        int i = 0;
        for (Integer bug : allBugs) {
            ret[i++] = bug;
        }
        return ret;
    }

    public int hashCode() {
        return getCVSPath().hashCode();
    }

    public SourceRoot getSourceRoot() {
        return sourceRoot;
    }
       
    public boolean equals(Object obj) {
       return  (obj instanceof Package) &&
               getCVSPath().equals(((Package)obj).getCVSPath());
    }
     
    public CvsUtil getCvsUtil() {
      return CvsUtil.getCvsUtil(getCVSFile());          
    }
    
    public String toString() {
        return name;
    }

    public File getCacheFolder() {
        return sourceRoot.getPackageFile(getName());
    }
    private void storeJavaFileNames(Set<String> cachedNames) {
         try {
             File f = new File(getCacheFolder(),JAVA_FILES_LIST_NAME);
             PrintWriter pw = new PrintWriter(new FileWriter(f));
             try {
                 for (String item : cachedNames) {
                     pw.println(item);
                 }
             } finally {
                 pw.close();
             }
         } catch (IOException ioe) {
             Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE,"Error during creating java files names list.",ioe);
         }
    }
    
    private Set<String> readJavaFileNames() {
        Set<String> lines = new HashSet<String>();
        try {
            File f = new File(getCacheFolder(),JAVA_FILES_LIST_NAME);
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                try { 
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (IOException ioe) {
             Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE,"Error during creating java files names list.",ioe);
        }
        return lines;
    }
    
}
