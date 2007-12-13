/*
 * RepositoryTest.java
 * JUnit based test
 *
 * Created on August 16, 2007, 2:06 PM
 */

package org.codeviation.model;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class RepositoryTest extends TestCase {
    
    public RepositoryTest(String testName) {
        super(testName);
    }

 
       public void testGetPutProperty() throws IOException {
        File cacheFolder = TestUtil.getTmpFolder("RepositoryTest.test1");
        cacheFolder.mkdirs();
        assertTrue(cacheFolder.isDirectory());
        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, cacheFolder.getAbsolutePath());
        PersistenceManager manager = PersistenceManager.getDefault();
        assertTrue(manager.getFolder().exists());
        File repository = TestUtil.getTmpFolder("testrep");
        repository.mkdirs();
        Repository rep = manager.getOrCreateRepository(repository, "testrep");
        String value = rep.getProperty("test1.property");
        assertNull(value);
        assertTrue(rep.getCacheRoot().exists());
        rep.setProperty("test1.property", "test");
        
        assertEquals("test property","test",rep.getProperty("test1.property"));
        // ignore cache
        rep.properties = null;
        assertEquals("test property","test",rep.getProperty("test1.property"));
    }
       
    public void testGetJavaClass() throws IOException {
        assertTrue(true);
    }

}
    