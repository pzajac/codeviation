/*
 * DiffRegressionPositionTest.java
 * JUnit based test
 *
 * Created on December 8, 2006, 7:23 PM
 */

package org.codeviation.model.vcs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Line;
import org.codeviation.model.TestUtil;
import org.codeviation.model.TestUtil;
import org.codeviation.model.Version;

/**
 *
 * @author pzajac
 */
public class DiffRegressionPositionTest extends TestCase {
    File prj;
    public DiffRegressionPositionTest(String testName) {
        super(testName);
    }
    protected void setUp() throws IOException, InterruptedException {
        prj = ExamplesSetup.getPantProject();
    }
    
    public void testPantFiles() throws IOException, InterruptedException {
         assertFile("src/org/netbeans/metrics/javac/Position.java","org.netbeans.metrics.javac");
         assertFile("src/org/netbeans/metrics/cvs/Version.java","org.netbeans.metrics.cvs");
    }
    
    private void assertFile(String name,String pkg) throws IOException {
        File javaMainFile = new File(prj,name);
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, pkg);
        assertNotNull(jf);
        Version v = jf.getCVSResultMetric().getRootVersion();
        while (v.getNext() != null) {
            v = v.getNext();
        }
        assertNotNull(v);
        List<Line> lines = v.getLines();
        TestUtil.assertFile(lines,javaMainFile);
        
    }
}
