/*
 * MethodRankTest.java
 * 
 * Created on Sep 30, 2007, 10:21:56 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.tasks;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.codeviation.javac.MeasuringJavac;
import org.codeviation.javac.RunJavacTest;
import org.codeviation.main.ClassRankMatrixGenerator;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class MethodRankTest extends TestCase {
    
    public MethodRankTest(String testName) {
        super(testName);
    }            
    
    private static class PageRankRootFilter  implements SourceRootFilter {

        public boolean accept(SourceRoot srcRoot) {
            return (srcRoot.getRelPath().equals("pantexamples/PageRank/src"));
        }
    }
    public void testSimple() throws IOException, InterruptedException {
        ExamplesSetup.checkoutExamples = false;
        TestUtil.clearCache();
 
        ExamplesSetup.checkoutExamples();
        Repository rep = PersistenceManager.getDefault().getRepository(ExamplesSetup.getCvsWork());
        String relPath = "pantexamples/PageRank/src";
        ExamplesSetup.updateFile(relPath, "conf1");
        File prjDir = ExamplesSetup.getExamplesDir("PageRank");
        System.setProperty(MeasuringJavac.CVS_TAG_PROP_NAME, "conf1");
        RunJavacTest.runJavac(prjDir);
        
        // CLASS RANK
        ClassRankMatrixGenerator generator = new ClassRankMatrixGenerator(ClassRankMatrixGenerator.ElementType.CLASS);
        generator.computeAndStore(rep, "conf1",new PageRankRootFilter());
 
        // METHOD RANK
        generator = new ClassRankMatrixGenerator(ClassRankMatrixGenerator.ElementType.METHOD);
        generator.computeAndStore(rep, "conf1",new PageRankRootFilter());
    }
    

}
