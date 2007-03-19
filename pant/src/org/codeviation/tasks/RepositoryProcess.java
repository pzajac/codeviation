
package org.codeviation.tasks;

import org.codeviation.model.Repository;

/**
 * Computation process. New implementatations can be registered
 * to META-INF/services/org.codeviation.computed.RepositoryProcess 
 * 
 */
public interface RepositoryProcess {
    /** Execution of process for repository and evironmet. Environment
     *  contains workdir, processed tags with pant
     */
    public boolean execute(Repository rep,RepositoryProcessEnv env); 
    /** Name of process allows to filter run of precesses
     */
    public String getName();
    /** Useful for unteractive filtering 
     */ 
    public String getDescription();
}
