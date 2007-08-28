/*
 * Repository.java
 *
 * Created on November 12, 2006, 3:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.main.PrepareNbTags;

/**
 *  It represents whole cvs repository.
 * @author pzajac
 */
public final class Repository {
    public static final String CONFIGURATION_FILE = "configuration.properties";
    private final File cvsRoot;
    private final String name;
    private static final String SOURCE_ROOTS_LIST_FILE = "sourceroots.lst";
    private static final String COMPILATION_STATUS_FILE = "CompilationStatus.ser";
    
    private static final Logger logger = Logger.getLogger(PersistenceManager.class.getName());
    /** rel_path_in_cvs -> SourceRoot */
    private Map<String,SourceRoot> sourceRoots;
    /** last modification of SOURCE_ROOTS_LIST_FILE*/
    private long sourceRootsModification;
    Properties properties;
    /** Creates a new instance of Repository */
    Repository(File cvsRoot,String name) {
        this.cvsRoot = cvsRoot;
        this.name = name;
        sourceRoots = new TreeMap<String, SourceRoot>();
          readSourceRoots();
    }
    
    private void readSourceRoots() {
        File sourceRootsListFile = new File (getCacheRoot(),SOURCE_ROOTS_LIST_FILE);
        try {
            if (sourceRootsListFile.exists()) {
                sourceRootsModification = sourceRootsListFile.lastModified();
                BufferedReader reader = new BufferedReader(new FileReader (sourceRootsListFile));
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        // XXX should be removed 
                        if (line.startsWith("/")){
                            line = line.substring(1);
                        }
                        sourceRoots.put(line, new SourceRoot(this,line));
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "reading source roots", ioe);
        }
    }
    
    private void writeSourceRoots() {
        File sourceRootsListFile = new File (getCacheRoot(),SOURCE_ROOTS_LIST_FILE);
        try {
            PrintWriter writer = new PrintWriter(sourceRootsListFile);
            try {
                for (String relPath : sourceRoots.keySet() ) {
                    writer.println(relPath);
                }
            } finally {
                writer.close();
            }
        } catch (IOException ioe ) {
            logger.log(Level.SEVERE, "writing source roots", ioe);
        }
    }
    public File getCvsRoot() {
        return cvsRoot;
    }
    
    
    /**
     * Get repository folder in pant.cache folder.
     * @return repository folder
     */
    public File getCacheRoot() {
        File f = new File(PersistenceManager.getDefault().getFolder(),getName());
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }
    public String getName() {
        return name;
    }
    @Override
    public int hashCode() {
        return cvsRoot.hashCode();        
    }
    
    @Override
    public boolean equals (Object obj) {
        return (obj instanceof Repository) ?
            getCvsRoot().equals(((Repository)obj).getCacheRoot()) : false;
    }
    
    public SourceRoot getOrCreateSourceRoot(File srcRootFile) {
        String relPath = getCvsRelPath(srcRootFile);
        SourceRoot sr = sourceRoots.get(relPath);
        if (sr == null) {
            sr = new SourceRoot(this,relPath);
            sourceRoots.put(relPath, sr);
            writeSourceRoots();
        }
        return sr;
    }
    
    public List<SourceRoot> getSourceRoots() {
        File sourceRootsListFile = new File (getCacheRoot(),SOURCE_ROOTS_LIST_FILE);
        if (sourceRootsListFile.exists() && sourceRootsListFile.lastModified() != sourceRootsModification) {
            readSourceRoots();
        }
        List<SourceRoot> roots = new ArrayList<SourceRoot>(sourceRoots.values());
        return roots;
    }
    
    /** Get already initialized source root. 
     * @param relPath is relative path in cvs
     * @return null if no appropriate src root was found.
     */
    public SourceRoot getSourceRoot(String relPath) {
       for (SourceRoot root : getSourceRoots()) {
           if (root.getRelPath().equals(relPath)) {
               return root;
           }
       } 
       return null;
    }
    /** get relative path in cvs
     */
    public String getCvsRelPath(File cvsFile) {
        String cfp = cvsFile.getAbsolutePath();
        String crp = cvsRoot.getAbsolutePath();
        if (cfp.indexOf(crp) == 0) {
            return cfp.substring(crp.length() + 1);
        }
        return null;
    }

    public Set<String> getAllTags() {
        Set<String> tags = new TreeSet<String>();
        for (SourceRoot srcRoot : getSourceRoots()) {
            tags.addAll(srcRoot.getCvsTags());
        }
        return tags;
    }
    
    /** Get date for tag or timestamp
     *  @param tag cvs tag or timestamp 'YYYY/MM/dd [hh:mm[:ss]]' 
     * @return null if tag was not resolved
     */
    public Date getTagDate(String tag) {
        try {
            return PrepareNbTags.parseTagDate(tag);
        } catch (ParseException ex) {
           logger.log(java.util.logging.Level.SEVERE,ex.getMessage(), ex);
        }
        return null;
    }
    /**
     * Add info about compilation for source root
     * @param srcRoot source root
     * @param status compilation status 
     * @param tagName cvs timestamp or tag name
     */ 
    public void addSourceRootCompilationStatus(SourceRoot srcRoot,boolean status,String tagName) {
        if (tagName == null) {
            logger.info("Null tagName for source root " + srcRoot.getRelPath());
            return ;
        }
        CompilationStatus cs = getCompilationStatus();
        cs.addSourceRootCompilationStatus(srcRoot.getRelPath(),status,tagName);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File (getCacheRoot(),COMPILATION_STATUS_FILE)));
            try {
                oos.writeObject(cs);
            } finally {
                oos.close();
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE,"Compilation status storing error", ioe);
        }
        
    }    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * get compilation status for this repository
     * @return compilation status
     */
    public CompilationStatus getCompilationStatus() {
        File csFile = new File (getCacheRoot(),COMPILATION_STATUS_FILE);
        if (csFile.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(csFile));
                try {
                    return (CompilationStatus) ois.readObject();
                } finally {
                    ois.close();
                }
            } catch (IOException ioe ) {
                logger.log(Level.SEVERE,"CompilationStatus reading error.",ioe);
            } catch (ClassNotFoundException cnfe) {
                logger.log(Level.SEVERE,"CompilationStatus reading error.",cnfe);
            }
        } 
        return new CompilationStatus();
    }
    
    /** Get property value of configuration
     * @return property value
     */
    public String getProperty(String name ) {
        return getProperties().getProperty(name);
    }

    /** Add or replace persistent property 
     */
    public void setProperty(String name,String value) {
        OutputStream  os = null;
        try {
            Properties props = getProperties();
            props.setProperty(name, value);
            os = new FileOutputStream(new File(getCacheRoot(), CONFIGURATION_FILE));
            props.store(os, "");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(),ex);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(),ex);
            }
        }
        
    }
    
    
    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            File propertiesFile = new File (getCacheRoot(),CONFIGURATION_FILE);
            if (propertiesFile.exists()) {
                FileReader reader = null ;
                try {
                    reader = new FileReader(propertiesFile);
                    properties.load(reader);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(),ex);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex2) {
                            logger.log(Level.SEVERE, ex2.getMessage(),ex2);
                        }
                    }
                    
                }
            }
        }
        return properties;
    }
}

