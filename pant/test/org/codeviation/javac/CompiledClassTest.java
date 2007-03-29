
package org.codeviation.javac;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import junit.framework.TestCase;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author Petr Zajac
 */
public class CompiledClassTest extends TestCase {
    private File testPrjF;
    
    /** Creates a new instance of CountsMetricTest */
    public CompiledClassTest(String name) {
        super(name);
    }
    protected void setUp() {
        TestUtil.clearCache();
        TestUtil.enableMetricLogger();
        testPrjF = ExamplesSetup.getUsageMetricsProjectDir();
        MetricsRunner.clearMetrics();
        MetricsRunner.addMetric(new CountsBuilder());
    }
    
    public void testSimple() throws IOException, InterruptedException, ClassNotFoundException {
       String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
       System.out.println(System.getProperty(PersistenceManager.PANT_CACHE_FOLDER));
       ExamplesSetup.updateFile(filePath, "1.3");
       System.out.println("updated: 1.3");
       RunJavacTest.runJavac(testPrjF);   
       File buildDir = RunJavacTest.getBuildDir(testPrjF);
       URLClassLoader classLoader = new URLClassLoader(new URL[]{buildDir.toURL()});
       Class clazz = classLoader.loadClass("usagemetrics.Main");
       assertNotNull(clazz);
    }
    
    
}
