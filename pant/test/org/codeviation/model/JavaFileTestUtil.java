/*
 * JavaFileTestUtil.java
 *
 * Created on December 19, 2006, 5:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;

/**
 *
 * @author pzajac
 */
public class JavaFileTestUtil {
    
    /** Creates a new instance of JavaFileTestUtil */
    public JavaFileTestUtil() {
    }
    
    public static void clearCVSMetric(JavaFile jf) {
        jf.cvsMetric = null;
    }
}
