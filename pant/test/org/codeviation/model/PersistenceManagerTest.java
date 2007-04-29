/*
 * PersistenceManagerTest.java
 * JUnit based test
 *
 * Created on November 1, 2006, 9:06 PM
 */

package org.codeviation.model;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.codeviation.javac.UsagesMetric;

/**
 *
 * @author pzajac
 */
public class PersistenceManagerTest extends TestCase {
    private static final String TEST_JAVA = "Test.java" ;
    private static final String PKG_NAME = "pkg1";
    private static final String TEST_REP = "testingrep";
    private static final String TEST_JAVA_CLASS = "pkg1.Test.java";
    public PersistenceManagerTest(String testName) {
        super(testName);
    }
    
    public void test1() throws IOException {
        File cacheFolder = TestUtil.getTmpFolder("cache");
        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, cacheFolder.getCanonicalPath());
        PersistenceManager manager = PersistenceManager.getDefault();
        File repository = TestUtil.getTmpFolder(TEST_REP);
        File srcRoot1 = new File (repository,"src1");
        srcRoot1.mkdir();
        File pkgInRep = new File("srcRoot1",PKG_NAME);
        pkgInRep.mkdir();
        File testJavaFile = new File(pkgInRep,TEST_JAVA);
        manager.getOrCreateRepository(repository, TEST_REP);
        SourceRoot src = manager.getOrCreateSourceRoot(srcRoot1);
        Package pack = src.getPackage(PKG_NAME);
        File pkgFile = new File(pack.getCVSPath());
        assertNull(src.getMetric(PKG_NAME, TEST_JAVA,UsagesMetric.class));
        assertTrue(src.getMetricClasses(PKG_NAME, TEST_JAVA).isEmpty());
        
        // add a metric to the file 
        UsagesMetric result = new UsagesMetric();
        src.setMetric(PKG_NAME, TEST_JAVA, result);
        
        UsagesMetric result2 =  src.getMetric(PKG_NAME, TEST_JAVA, UsagesMetric.class);
        assertNotNull(result2);
        assertTrue(src.getMetricClasses(PKG_NAME, TEST_JAVA).size() == 1);        
    }
}
