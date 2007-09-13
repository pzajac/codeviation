/*
 * VersionIntervalTest.java
 * JUnit based test
 *
 * Created on August 30, 2007, 8:17 PM
 */

package org.codeviation.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class VersionIntervalTest extends TestCase {
    
    public VersionIntervalTest(String testName) {
        super(testName);
    }
    
    public void testContains() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date d1 = format.parse("20040101");
        Date d2 = format.parse("20040201");
        Date d3 = format.parse("20040301");
        Date d4 = format.parse("20040401");
        Date d5 = format.parse("20040501");
        
        Version v1 = new Version("1.1", "xx", d1, "adam", Version.State.EXP) ;
        Version v2 = new Version("1.2", "xx", d2, "adam", Version.State.EXP) ;
        Version v3 = new Version("1.3", "xx", d3, "adam", Version.State.EXP) ;
        Version v4 = new Version("1.4", "xx", d4, "adam", Version.State.EXP) ;
        Version v5 = new Version("1.5", "xx", d5, "adam", Version.State.EXP) ;


        VersionInterval vi = new VersionInterval(v2, v4);
        assertTrue("v3 is in",vi.contains(v3));
        assertTrue("v3 is in",vi.contains(v3.getDate()));
        assertTrue("v2 is in",vi.contains(v2));
        assertTrue("v2 is in",vi.contains(v2.getDate()));
        assertTrue("v4 is in",vi.contains(v4));
        assertTrue("v4 is in",vi.contains(v4.getDate()));
        
        assertTrue("v1 is not in",!vi.contains(v1.getDate()));
        assertTrue("v1 is not in",!vi.contains(v1));
        assertTrue("v5 is not in",!vi.contains(v5.getDate()));
        
        
            
        
    }
    
}
