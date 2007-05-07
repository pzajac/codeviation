/*
 * CompilationStatus.java
 * 
 * Created on May 7, 2007, 10:16:06 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *compilation status by javc compiler
 * @author pzajac
 */
public class CompilationStatus implements Serializable{
    private static final long serialVersionUID = 1;
    
    // tagName -> (srcRootName -> compilationStatus)
    private Map<String,Map<String,Boolean>> records = new HashMap<String,Map<String,Boolean>>();
    
    /**
     * Add compilation status for a SourceRoot 
     * @param srcRootName relative source root name 
     * @param status compilatin status
     * @param tagName tag name or timestamp
     */
    void addSourceRootCompilationStatus(String srcRootName,boolean status,String tagName) {
       
       // reduce stored size
       srcRootName = srcRootName.intern();
       Map<String,Boolean> build = records.get(tagName);
       if (build == null) {
           build = new HashMap<String,Boolean>();
           records.put(tagName, build);
       }
       build.put(srcRootName, status);
    }

    /** get supported tags
     *  @return set of available tags
     */
    public Set<String> getTags() {
        return records.keySet();
    }
    /** Get compilation statuses for a tag or timestamp
     * @param tag tag or timestamp
     * @return map sourceRootName  to compilationStatus
     */
    public Map<String,Boolean> getSourceRootCompilationStatuses(String tag) {
        return Collections.unmodifiableMap(records.get(tag));
    }   
}
