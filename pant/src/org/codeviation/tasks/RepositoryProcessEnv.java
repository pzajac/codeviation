
package org.codeviation.tasks;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * Environment for repository process
 */
public final class RepositoryProcessEnv {
    public static enum LogReason {
       START_PROCESS,
       END_PROCESS,
       START_SOURCE_ROOT,
       END_SOURCE_ROOT,
       START_TAG
    }
    private Set<String> tags = new TreeSet<String>();
    private File workDir;
    private SourceRootFilter srcFilter;
    
    /** get tags precessed by pant and available for computation
     */
    public Set<String> getTags() {
        return new TreeSet<String>(tags);
    }
  
    public void setTags(Set<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }
    
    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }
    public File getWorkDir() {
        return workDir;
    }
    public void setSourceRootFilter (SourceRootFilter filter) {
        this.srcFilter = filter;
    }
    public SourceRootFilter getSourceRootFilter() {
        return srcFilter;
    }
    /** monitoring process
     */
    public void log(RepositoryProcess process,LogReason reason,String message) {
        // XXX 
    }
}
