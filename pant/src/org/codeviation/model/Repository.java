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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.main.PrepareNbTags;

/**
 *
 * @author pzajac
 */
public final class Repository {
    private final File cvsRoot;
    private final String name;
    private static final String SOURCE_ROOTS_LIST_FILE = "sourceroots.lst";
    private static final Logger logger = Logger.getLogger(PersistenceManager.class.getName());
    /** rel_path_in_cvs -> SourceRoot */
    private Map<String,SourceRoot> sourceRoots;
    /** last modification of SOURCE_ROOTS_LIST_FILE*/
    private long sourceRootsModification;
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
    
    
    public File getCacheRoot() {
        return new File(PersistenceManager.getDefault().getFolder(),getName());
    }
    public String getName() {
        return name;
    }
    public int hashCode() {
        return cvsRoot.hashCode();        
    }
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
    
    public String toString() {
        return name;
    }
}
