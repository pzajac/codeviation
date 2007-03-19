/*
 * JavaFileUtil.java
 *
 * Created on November 21, 2006, 4:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;

import org.codeviation.model.vcs.CVSMetric;

/**
 *
 * @author pzajac
 */
public final class JavaFileUtil {
    /** used for deserialization metrics
     * XXX Can be replaced by using special ObjectInput an ObjectOutput
     */ 
    private  static JavaFile currentJavaFile;
    /** Creates a new instance of JavaFileUtil */
    public JavaFileUtil() {
    }
    
    public static JavaFile getCurrentJavaFile() {
        return currentJavaFile;
    }
    
    public static void setCurrentJavaFile(JavaFile javaFile) {
        currentJavaFile = javaFile;
    }
    
    public static CVSMetric getCVSResultMetric(JavaFile javaFile) {
        return javaFile.getMetric(CVSMetric.class);
    }
    
}
