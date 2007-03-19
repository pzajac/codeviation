/*
 * MetricUtil.java
 *
 * Created on October 30, 2006, 10:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.javac;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pzajac
 */
public class MetricUtil {
    public static final String LOGGER = "org.netbeans.ant.metrics.javac";
    /** Creates a new instance of MetricUtil */
    public MetricUtil() {
    }
    
    public static void notify(Exception e ) {
        Logger.getLogger(LOGGER).log(Level.SEVERE, "unkown", e);
    }
    
    /** get package name for a class. If the package is default package
     *  it will return null
     */
    public static String getPackageName(String className) {
       int lastDot = className.lastIndexOf(".");
       return (lastDot == -1) ?  "" : className.substring(0,lastDot);
    }
    
}
