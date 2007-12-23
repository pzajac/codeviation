
package org.codeviation.model.configuration;

import org.codeviation.model.Repository;

/**
 * SPI for setting external resources (e.g. bug trackking system, etc.) to repository.
 * All the implemantations are registered to META-INF/services/org.codeviation.mode.configuration  
 * @author pzajac
 */
public interface ConfigurationProvider {
        /** Invoked everytime everytime is touch new Repository by sing PersistenceManager
         * 
         * @param context for this thread
         * @param oldRepository previus repository selected in current thread
         * @param newRepository new repository selected fot this 
         */
       public void register (ConfigurationContext context, Repository oldRepository,Repository newRepository) ; 
}
