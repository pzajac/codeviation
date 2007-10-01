
package org.codeviation.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    
    public static  interface ProcessListener<PARAM> {
        public void actionPerformed(RepositoryProcess process,Event<PARAM>  event);
    }
    public static final class Event<PARAM> {
        private PARAM param;
        public Event(PARAM t) {
            this.param = t;
        }
        PARAM getParam() {
            return param;
        }
    }
    private Set<String> tags = new TreeSet<String>();
    private File workDir;
    private SourceRootFilter srcFilter;
    
    private List<ProcessListener> listeners = new ArrayList<ProcessListener>();
    /** get tags precessed by pant and available for computation
     */
    public Set<String> getTags() {
        return new TreeSet<String>(tags);
    }
  
    public void addListener (ProcessListener listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void performRepositoryAction (RepositoryProcess proc,Event<?> event) {
       List<ProcessListener> copy = null;
       synchronized(this) {
           copy = new ArrayList<ProcessListener>(listeners);
       }
       for (ProcessListener listener : copy) {
           listener.actionPerformed(proc, event);
       }
    }
    public void removeListener(ProcessListener listener){
        synchronized(this) {
            listeners.remove(listener);
        }
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
