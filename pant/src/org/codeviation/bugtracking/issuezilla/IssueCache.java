/*
 * IssueCache.java
 * 
 * Created on Aug 12, 2007, 10:52:34 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.bugtracking.issuezilla;

import org.openide.util.Lookup;

/**
 *
 * @author pzajac
 */
public abstract class IssueCache {
    private static IssueCache defaultCache;
    
    public static IssueCache getDefault() {
        return (defaultCache != null) ?
            defaultCache : Lookup.getDefault().lookup(IssueCache.class);
    }
    
    /** Use only for quick testing. Otherwise use rathet META-INF/services
     */
    public static void setCache(IssueCache cache) {
       IssueCache.defaultCache = cache; 
    }
    
    /** Read issue from default cache
     */ 
    public  static Issue getIssue(int issueId)  {
        IssueCache cache = getDefault();
        Issue issue = null;
        if (cache != null) {
            issue = cache.read(issueId);
        }
        return  issue;
    }
    
    /** custom implementation of cache
     * @param issueId issueId of the required issue
     * @return null if not IssueCache exists or issue was nol found
     */
    protected abstract Issue read(int issueId) ;
    
}
