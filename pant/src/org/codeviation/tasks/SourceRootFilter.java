package org.codeviation.tasks;

import org.codeviation.model.SourceRoot;

/**
 * Filter for sourcer roots. Usefull in process. 
 * @author pzajac
 */
public interface SourceRootFilter {
    /** @param srcRoot tested source root
     * @return true if source root is accepted
     */
    public boolean accept(SourceRoot srcRoot);  
}
