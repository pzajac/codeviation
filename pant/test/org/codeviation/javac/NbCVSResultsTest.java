/*
 * NbCVSResultsTest.java
 * JUnit based test
 *
 * Created on February 16, 2007, 9:53 AM
 */

package org.codeviation.javac;

import java.util.Set;
import junit.framework.TestCase;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class NbCVSResultsTest extends TestCase {
    
    public NbCVSResultsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() {
        ExamplesSetup.initNbCvsTestingCache();
    }

    public void testTagsForAntDebugger() {
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        Set<String> cvsTags = rep.getSourceRoot("ant/debugger/src").getCvsTags();
        assertFalse(cvsTags.isEmpty());
    }
    public void testTagsForRepository() {
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        assertFalse(rep.getAllTags().isEmpty());
    }

}
