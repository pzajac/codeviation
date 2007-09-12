/*
 * ExamplesSetup.java
 *
 * Created on November 11, 2006, 7:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model.vcs;
import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.TestUtil;

/**
 *
 * @author pzajac
 */
public class ExamplesSetup {
    public static boolean checkoutExamples = false;
    /** Creates a new instance of ExamplesSetup */
    public ExamplesSetup() {
        
    }
    static {
       TestUtil.enableFileHandler(CvsUtil.class.getName());
    }
    
    public static String getPantCacheFolder() {
       String pantCacheFolder = System.getProperty("pant.cache.folder");
       if (pantCacheFolder == null) {
            System.setProperty("pant.cache.folder","/tmp/pant/cachefolder");
       }
       File f = new File (System.getProperty("pant.cache.folder"));
       f.mkdirs();
       return System.getProperty("pant.cache.folder");
        
    }
    public static String getDataDir() {
       final String propName = "test.pant.datadir";
       String pantCacheFolder = System.getProperty(propName);
       if (pantCacheFolder == null) {
            System.setProperty(propName,"/cvss/testpantdatadir");
       }
       File f = new File (System.getProperty(propName));
       f.mkdirs();
       return System.getProperty(propName);
    }

    public static String getCvsRoot() {
        getPantCacheFolder();
        String cvsRoot = System.getProperty("test.cvsroot"); 
        if (cvsRoot == null) {
            // initialize deafautl cvsroot 
            cvsRoot = "/cvss/examples/cvs";
        }
        Assert.assertNotNull(cvsRoot);
        return cvsRoot; 
    }
    public static void checkoutExamples() throws IOException, InterruptedException {
        TestUtil.dropPersistenceManager();
        System.out.println(getPantCacheFolder());
        if (!checkoutExamples) {
            String path = getCvsWork().getAbsolutePath();
            TestUtil.deleteFile(getCvsWork());
            new File(path).mkdirs();
            CvsUtil.executeCvsCommand(new String[]{"cvs","-d",getCvsRoot() ,"checkout","pantexamples"} , getCvsWork());
            Assert.assertTrue("${test.cvsroot}/pantexamples exists. " + getCvsRoot(), new File(getCvsWork(),"pantexamples").exists());
            checkoutExamples = true;
        }
        PersistenceManager.getDefault().getOrCreateRepository(getCvsWork(), "pantexamples");
    }
    
    public static File getPantProject() throws IOException, InterruptedException {
        TestUtil.dropPersistenceManager();
        System.out.println(getPantCacheFolder());
        CvsUtil.executeCvsCommand(new String[]{"cvs","-d",getCvsRoot() ,"checkout","pant"} , getCvsWork()); 
        Assert.assertTrue("${test.cvsroot}/pant exists.", new File(getCvsWork(),"pant").exists());
        checkoutExamples = true;
        PersistenceManager.getDefault().getOrCreateRepository(getCvsWork(), "pantexamples");
        return new File(getCvsWork(),"pant");
    }
    
    public static void updateFile(String  cvsRelPath,String version) throws IOException, InterruptedException {
         CvsUtil.executeCvsCommand(new String[]{"cvs","-d",getCvsRoot() ,"checkout","-r",version,cvsRelPath} , getCvsWork());
    }
    public static File getExamplesDir(String prjName) {
        File f = null;
        try {
            checkoutExamples();
            f =  new File(getCvsWork(),"pantexamples/" + prjName);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Assert.assertTrue("pantexamples/" + prjName + " is folder.", f.isDirectory());
        return f;
 
    }
    
    public static File getTestJ2seProjectDir() {
        return getExamplesDir("Testj2seExample");
        
    }
    public static File getUsageMetricsProjectDir() {
        return getExamplesDir("usagemetrics");
    }
    public static File getBlockTestDir() {
        return getExamplesDir("testblockmetrics");
    }
    
    public static File getCvsWork() {
        File rootPath =  new File (TestUtil.getWorkDir(),"examplescvs" );
        rootPath.mkdirs();
        if (!rootPath.isDirectory()) {
            throw new IllegalStateException("test.cvsroot is not folder");
        }
        
        return rootPath;
    }
    
      public static void initNbCvsTestingCache() {
        TestUtil.dropPersistenceManager();
        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, "/cvss/pantcache");
        CVSMetric.setUpdateCVS(false);        
    }
 }
