/*
 * CVSMetricTest.java
 * JUnit based test
 *
 * Created on December 19, 2006, 5:02 PM
 */

package org.codeviation.model.vcs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.JavaFileTestUtil;
import org.codeviation.model.Line;
import org.codeviation.model.TestUtil;
import org.codeviation.model.Version;

/**
 *
 * @author pzajac
 */
public class CVSMetricTest extends TestCase {
    File prj;
    
    public CVSMetricTest(String testName) {
        super(testName);
    }
    protected void setUp() throws IOException, InterruptedException {
        TestUtil.clearCache();
        prj = ExamplesSetup.getTestJ2seProjectDir();
    }
    
    public void testPersistence() throws IOException {
        doFile("Main.java");
    }
    
    public void testAttic() throws IOException, InterruptedException {
        String version = "1.1";
        String args[] = new String[] {"cvs","update","-r",version,"InternalHandle.java"};
        CvsUtil.executeCvsCommand(args, new File(prj,"src/testj2seexample"));
        File internalHandler = new File(prj,"src/testj2seexample/InternalHandle.java");
        assertTrue(internalHandler.getPath(),internalHandler.exists());
        JavaFile jf = JavaFile.getJavaFile(internalHandler, "testj2seexample");
        CVSMetric cvs = jf.getCVSResultMetric();
        cvs.getRootVersion();
        Version v = jf.getCVSVersion();
        assertEquals(version,v.getRevision());
        List<Line> lines = jf.getLines(v);
        TestUtil.assertFile(lines, jf.getCVSFile());
    }
    
    public void testMenuBar () throws IOException {
        doFile("MenuBar.java");        
    }
    public void testNbTopManager () throws IOException {
        doFile("NbTopManager.java");
    }
    public void testBaseOptions () throws IOException {
        doFile("BaseOptionsBeanInfo.java");
    }
   public void testActionImplementationFactory() throws IOException {
        doFile("ActionsImplementationFactory.java");
    }
 
    public void testCatalogNode () throws IOException {
        // broken cvs :(
        
        //----------------------------
        //revision 1.24.30.1
        //date:tulach;  state: Exp;  lines: +13 -7
        //branches:  1.24.30.1.16;
        //Forgotten CDDL changes for xml
        //----------------------------   
        CVSMetric cvs = doFile("CatalogNode.java");
//        Version v = cvs.getRootVersion();
//        do {
//            System.out.println(v.getRevision() + " " + v.getUser() + " " + v.getDate() + " " + v.getComment());
//        } while ((v = v.getNext()) != null);

    }
    /** file commited with -kb paramater
     */
    public void testJavaBinaryFile () throws IOException, InterruptedException {
        String version = "1.2";
        String args[] = new String[] {"cvs","update","-r",version,"DynamicProperties.java"};
        CvsUtil.executeCvsCommand(args, new File(prj,"src/testj2seexample"));
        File internalHandler = new File(prj,"src/testj2seexample/DynamicProperties.java");
        assertTrue(internalHandler.getPath(),internalHandler.exists());
        JavaFile jf = JavaFile.getJavaFile(internalHandler, "testj2seexample");
        CVSMetric cvs = jf.getCVSResultMetric();
        cvs.getRootVersion();
        Version v = jf.getCVSVersion();
        assertEquals(version,v.getRevision());
        List<Line> lines = jf.getLines(v);
        // failing
        TestUtil.assertFile(lines, jf.getCVSFile());
    }

    private CVSMetric doFile(String name) throws IOException {
        File javaMainFile = new File(prj,"src/testj2seexample/" + name);
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "testj2seexample");
        // read and persists all diffs
        jf.getDiffs();
        CVSMetric cvs = jf.getCVSResultMetric();
        
        JavaFileTestUtil.clearCVSMetric(jf);
        // read serialized historry
        CVSMetric cvs2 = jf.getCVSResultMetric();
        
        assertEquals(cvs, cvs2);
        return cvs;
    }
    
}
