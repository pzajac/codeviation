/*
 * TestUtil.java
 *
 * Created on November 9, 2006, 10:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class TestUtil {
    private static Map<String,FileHandler> logHandlers = new HashMap<String, FileHandler>();
    /** Creates a new instance of TestUtil */
    public TestUtil() {
    }
    
    public static File getRepositoryRoot() {
        // XXX Examples setup???
        File f = new File ("/tmp/RepositoryRoot");
        deleteFile(f);
        f.mkdir();
        return f;
    }
    public static void deleteFile (File f) {
        if (f.isDirectory()) {
            for (File f2 : f.listFiles()) {
               deleteFile(f2);   
            }
        } 
        f.delete();            
    }
    
    public static File getWorkDir() {
        File f = new File ("/tmp/testpant");
        f.mkdirs();
        Assert.assertTrue("Work dir exists.",f.isDirectory());
        return f;
    }
    
    public static void enableParentHandler(boolean value) {
        ParentHandler.fail = value;
    }
    static class ParentHandler extends  Handler {
        Level level;
        static boolean fail = true;
        ParentHandler(Level level) {
            this.level = level;
        }
        
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= level.intValue()) {
                String msg = record.getLevel() + " :" + record.getMessage()  + "," + record.getThrown();
                if (record.getThrown() != null) {
                   record.getThrown().printStackTrace(System.err);
                }
                if (fail) {
                    Assert.fail(msg);
                }
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
        
    }
    static boolean initializedMetricLogger;
    
    public static void enableMetricLogger() {
        if (!initializedMetricLogger) {
            initializedMetricLogger = true;
            Logger metricLogger = Logger.getLogger("org.codeviation");
            metricLogger.addHandler(new ParentHandler(Level.WARNING));
        }
    }
            
    public static void enableFileHandler(String name) {
       if (logHandlers.get(name) == null) {
           File logFolder = new File (getWorkDir(),"logs");
           logFolder.mkdir();
           try {
                FileHandler fh = new FileHandler(logFolder.getAbsolutePath() + File.separatorChar + name);
                Logger logger = Logger.getLogger(name);
                logger.addHandler(fh);
                logger.setLevel(Level.FINE);
           } catch(Exception e) {
               throw new IllegalStateException (e);
           }           
       }
    }    
    public static File getTmpFolder(String folderName) {
        File tmpDir = getWorkDir();
        File file = new File(tmpDir,folderName);
        TestUtil.deleteFile(file);
        file.mkdirs();
        return file;
    }
    public static void clearCache() {
        File f = new File(ExamplesSetup.getPantCacheFolder());
        if (f.exists()) {
            System.out.println("delete:" + f);
            deleteFile(f);
            f.mkdirs();
        }
    }
    
    public static void dropPersistenceManager() {
        PersistenceManager.dropPersistenceManager();
    }
    
    public static void assertFile(List<Line> lines, File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            int i = 0;
            for (Line l : lines) {
                Assert.assertEquals(file.getPath() + (i++),reader.readLine(), l.getNewContent());
            }
            Assert.assertNull(reader.readLine());
        } finally {
            reader.close();
        }
    }
    
    public static  void deleteAllCvsFolder(File dir) {
        if (dir.getName().equals("CVS")) {
            TestUtil.deleteFile(dir);
        } else {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    deleteAllCvsFolder(f);
                }
            }
        }
    }

}
