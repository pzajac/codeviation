
package org.codeviation.model;

import junit.framework.TestCase;
import org.codeviation.tasks.RepositoryProcess;
import org.codeviation.javac.MetricBuilder;
import org.codeviation.statistics.ChartConfProvider;
import org.codeviation.table.TableConfProvider;
import org.openide.util.Lookup;

/**
 * Verification syntax of META-INF/services
 * @author pzajac
 */
public class MetaInfServicesTest extends TestCase {
    
    public MetaInfServicesTest(String name) {
        super(name);
    }

    private void checkService(Class<?> clazz) {
        boolean found = false;
        for (Object obj : Lookup.getDefault().lookupAll(clazz)) {
            assertNotNull(obj);
            found = true;
        }
        assertTrue(found);
    }
    
    public void testRepositoryProcess() {
        checkService(RepositoryProcess.class);
    }
    
    public void testMetricBuilder() {
        checkService(MetricBuilder.class);
    }
    
    public void testCharConfProvider() {
        checkService(ChartConfProvider.class);
    }
    
    public void testTableConfProvider() {
        checkService(TableConfProvider.class);
    }
}
