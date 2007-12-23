
package org.codeviation.model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.configuration.ConfigurationContext;
import org.codeviation.model.configuration.ConfigurationProvider;
import org.openide.util.Lookup;


/**
 *
 * stores all metrics
 */
public class PersistenceManager implements Serializable{
    File folder;
    private static final String REPOSITORIES_LIST_FILE = "repositories.lst";
    
    private static final Logger logger = Logger.getLogger(PersistenceManager.class.getName());
    /** property name with cache folder location
     */
    public static final String PANT_CACHE_FOLDER = "pant.cache.folder";    
    private static  PersistenceManager pm;
    /** repository root -> repository name 
     */
    private final Map<File,Repository> repositories = new TreeMap<File,Repository>();
    /** Creates a new instance of PersistenceManager */
    private PersistenceManager(File folder) {
        this.folder = folder;
        if (!folder.exists()) {
            throw new IllegalStateException("Folder doesn't exist: " + folder);
        }
    }
    
    void readRepositories() {
        File repositoriesListFile = new File (getFolder(),REPOSITORIES_LIST_FILE);
        try {
            if (repositoriesListFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader (repositoriesListFile));
                try {
                    String name = null,fileName = null;
                    while ((name = reader.readLine()) != null) {
                        if ((fileName = reader.readLine()) == null ) {
                            throw new IllegalStateException();
                        }
                        File file = new File(fileName);
                        if (!file.exists()) {
                            logger.log(Level.SEVERE,"CVSROOT for repository " + name + " and folder " + fileName + "doesn't exist.");
                        } else {
                            repositories.put(file, new Repository(file,name));
                        }
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "reading repositories", ioe);
        }
    }

    private Repository touchRepository(Repository rep) {
         ConfigurationContext current = ConfigurationContext.getCurrent();
         Repository prevRep = current.getPrevRepository();
        if (prevRep != rep) {
            Collection<? extends ConfigurationProvider> providers = Lookup.getDefault().lookupAll(ConfigurationProvider.class);
            for (ConfigurationProvider provider : providers) {
                provider.register(current, prevRep, rep);
            }
            current.setPrevRepository(rep);

        }
        return rep;
    }
    
    private void writeRepositories() {
        File repositoriesListFile = new File (getFolder(),REPOSITORIES_LIST_FILE);
        try {
            PrintWriter writer = new PrintWriter(repositoriesListFile);
            try {
                for (Repository rep : repositories.values()) {
                    writer.println(rep.getName());
                    writer.println(rep.getCvsRoot().getAbsolutePath());
                }
            } finally {
                writer.close();
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "writing repositories", ioe);
        }
    }
    
    public SourceRoot getOrCreateSourceRoot(File srcRoot) throws IOException { 
        Repository rep = getRepository(srcRoot);
        if (rep == null) {
            throw new IllegalStateException("Mising repository. Please add new repository root record to repositories.lst for srcroot " + srcRoot);
        }
        return rep.getOrCreateSourceRoot(srcRoot);
    }
    public SourceRoot getOrCreateSourceRoot(File javaFile,String packageName) throws IOException {
  //      String packageName = MetricUtil.getPackageName(className); 
        if (packageName == null) {
            return getOrCreateSourceRoot(javaFile.getParentFile());
        } 
        String relPath = packageName.replace('.', File.separatorChar); 
        String absPath = javaFile.getParentFile().getCanonicalPath();
        if (!absPath.endsWith(relPath)) {
            throw new IllegalArgumentException("Invalid package " + javaFile + ",'" + packageName + "'");
        }
        return getOrCreateSourceRoot(new File(absPath.substring(0,absPath.length() - relPath.length())));
    } 
    
    public synchronized Repository getOrCreateRepository(File root,String name) {
        Repository rep = repositories.get(root);
        if (rep == null) {
           System.out.println("Creating repository:" + root +"," + name);
           addRepository(root,name);
           rep = repositories.get(root);
        } else if (!name.equals(rep.getName())) {
            throw new IllegalStateException ("Found repository with the same root = " + root + 
                    " but different name, " + name + "!=" + rep.getName());
        }
        return touchRepository(rep);
    }
    /**
     * @throws IllegalArgumentException if repository name or root has been
     * already registered
     */
    private void addRepository(File root,String name) {
       for (Map.Entry<File,Repository> entry : repositories.entrySet()) {
           if (entry.getKey().equals(root) ) {
               throw new IllegalArgumentException("Repository root already exists."); // NOI18N
           }
           if (entry.getValue().equals(name)) {
               throw new IllegalArgumentException("Repository name already exists."); // NOI18N
           }
       } 
       // register new
       repositories.put(root, new Repository(root,name));
       writeRepositories();
    }
    
    /** @return folder for metrics of CvsRepository
     */
    public synchronized Repository getRepository(File cvsFile) {
        for (Repository rep : repositories.values()) {
            if (cvsFile.getAbsolutePath().indexOf(rep.getCvsRoot().getAbsolutePath()) == 0) {
                    return touchRepository(rep);
            }
        }
        return touchRepository(null);
    }
    
    public synchronized Repository getRepository(String name) {
        for (Repository rep : repositories.values()) {
             if (name.equals(rep.getName())) {
                 return touchRepository(rep);
             }
        }
        return touchRepository(null);
    }
    
//    File getRepositoryFolderForSrcRoot(File srcRoot) throws IOException {
//        Repository rep = getRepository(srcRoot);
//        if (rep == null) {
//            throw new IllegalStateException("No repository found for " + srcRoot);
//        }
//        String canonicalPath = srcRoot.getCanonicalPath();
//        for (File repRoot : repositories.keySet()) {
//            String canRep = repRoot.getCanonicalPath();
//            if (canonicalPath.indexOf(canRep) == 0 ) {
//                return repRoot;
//            }
//        }
//        throw new IllegalArgumentException("root not found for srcroot " + srcRoot.getAbsolutePath());
//    }
    
    public static PersistenceManager getDefault() {
        if (pm == null) {
            String folder = System.getProperty(PANT_CACHE_FOLDER);
            if (folder == null) {
                throw new IllegalStateException("Define pant.cache.folder property");
            }
            pm = new PersistenceManager(new File(folder));
            pm.readRepositories();

        }
        return pm;
    }
    
//    public List<Repository> getRepositories() {
//        return new ArrayList<Repository>(repositories.values());
//    }
//    
    public File getFolder() {
        return folder;
    }
    
    /** reset for testing 
     */
    static void dropPersistenceManager() {
       pm = null; 
    }
}
