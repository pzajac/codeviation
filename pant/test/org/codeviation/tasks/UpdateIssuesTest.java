/*
 * UpdateIssuesTest.java
 * JUnit based test
 *
 * Created on September 3, 2007, 6:29 PM
 */

package org.codeviation.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;
import org.codeviation.model.TestUtil;

/**
 *
 * @author pzajac
 */
public class UpdateIssuesTest extends TestCase {
    
    public UpdateIssuesTest(String testName) {
        super(testName);
    }

    public void testLogComponents() throws  IOException {
        File f = new File(TestUtil.getWorkDir(),"UpdateIssuesTest.components");
        System.out.println(f.getAbsolutePath());
        if (f.exists()) {
            f.delete();
        }
        UpdateIssues.logComponents(f);
    }
    
    
}
