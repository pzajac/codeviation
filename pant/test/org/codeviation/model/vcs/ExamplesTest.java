/*
 * ExamplesTest.java
 * JUnit based test
 *
 * Created on November 11, 2006, 8:11 PM
 */

package org.codeviation.model.vcs;

import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class ExamplesTest extends TestCase {
    
    public ExamplesTest(String testName) {
        super(testName);
    }
    
    public void testCheckoutExamples() throws IOException, InterruptedException {
       ExamplesSetup.checkoutExamples();
    }
    
}
