/*
 * UsageItemTest.java
 * JUnit based test
 *
 * Created on March 12, 2007, 8:29 AM
 */

package org.codeviation.javac;

import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class UsageItemTest extends TestCase {
    
    public UsageItemTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testEquals() {
        UsageItem u1 = new UsageItem("a","A");
        UsageItem u2 = new UsageItem("a","A");
        UsageItem u3 = new UsageItem("a","B");
        UsageItem u4 = new UsageItem("b","A");
        UsageItem u5 = new UsageItem(null,"A");
        UsageItem u6 = new UsageItem(null,"A");
        UsageItem u7 = new UsageItem(null,"B");
        
        assertEquals(u1, u1);
        assertEquals(u1, u2);
        assertEquals(u5, u6);
        assertFalse(u1.equals(u5));
        assertFalse(u5.equals(u1));
        assertFalse(u7.equals(u6));
    }

}
