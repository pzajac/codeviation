package org.codeviation.tasks;

import java.io.File;
import org.codeviation.model.SourceRoot;

/**
 * Filter for sourcer roots. Usefull in process. 
 * @author pzajac
 */
public interface SourceRootFilter {
    /** @return true if source root is accepted
     */
    public boolean accept(SourceRoot srcRoot) ;

  
}
