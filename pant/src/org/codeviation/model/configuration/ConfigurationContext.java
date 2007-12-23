
package org.codeviation.model.configuration;

import java.util.HashMap;
import java.util.Map;
import org.codeviation.model.Repository;

/**
 * Context for repository configuration in current thread.
 * @author pzajac
 */
public final class ConfigurationContext {

        /** unique reference counter */
        private static int maxId;
        
        private int id;
        
        /** previously selected repository
         */
        private Repository prevRepository;
        
        private Map<Class,Object> parameters = new HashMap<Class,Object>(); 
        
        private static ConfThreadLocal threadLocal = new ConfThreadLocal();
        /** Get current configuration Context. ThreadLocal is used for configuration now
         * 
         * @return
         */
        public static ConfigurationContext getCurrent() {
            return threadLocal.get();
        }

        public ConfigurationContext() {
            synchronized (ConfigurationContext.class) {
                id = maxId++;
            }
        }

    public void setPrevRepository(Repository prevRepository) {
        this.prevRepository = prevRepository;
    }

    public Repository getPrevRepository() {
        return prevRepository;
    }

    
        public int getId() {
            return id;
        }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return  (obj instanceof ConfigurationContext && ((ConfigurationContext)obj).getId() == getId());
    }
        
    
    public void setParam(Class configurationProviderClass,Object value) {
        parameters.put(configurationProviderClass, value);
    }
    public Object getParam(Class configurationProviderClass) {
        return parameters.get(configurationProviderClass);
    }
        
        ///////////////////
        // inner classes
        ///////////////////
        private static class ConfThreadLocal extends ThreadLocal<ConfigurationContext> {

            @Override
            protected ConfigurationContext initialValue() {
                return new ConfigurationContext();
            }

        }
}
