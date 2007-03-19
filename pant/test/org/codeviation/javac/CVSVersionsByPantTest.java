package org.codeviation.javac;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.TestCase;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class CVSVersionsByPantTest extends TestCase {
    private File testPrjF;
    private TestHandler handler;
    private Logger logger;
    public CVSVersionsByPantTest(String testName) {
        super(testName);
    }

    protected void setUp() {
        TestUtil.clearCache();
        TestUtil.enableMetricLogger();
        testPrjF = ExamplesSetup.getUsageMetricsProjectDir();
        MetricsRunner.clearMetrics();
        MetricsRunner.addMetric(new CountsBuilder());
        handler = new TestHandler();
        logger = Logger.getLogger(MetricsRunner.class.getName());
        logger.addHandler(handler);
        logger.setLevel(Level.FINE);
    }

    class TestHandler extends Handler {
        String message;
        int count;
    
        public void publish(LogRecord rec) {
            if (rec.getMessage().equals(message)) {
                count++;
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
    }    
    public void testSimple() throws IOException, InterruptedException {
        String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
        ExamplesSetup.updateFile(filePath, "1.3");
        handler.message = "Already processed: Main.java " + CountsBuilder.class;
        RunJavacTest.runJavac(testPrjF);          
        File javaMainFile = new File(testPrjF,"src/usagemetrics/Main.java");
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        CountsMetric metric = jf.getMetric(CountsMetric.class);
        assertNotNull(metric);
        assertEquals("versions.size() = 1" , 1, metric.getVersions().size());

        CVSVersionsByPant cvspant =  jf.getMetric(CVSVersionsByPant.class);
        assertTrue(cvspant.containsRevision("1.3"));
        assertEquals("No version processed ",0,handler.count);
        System.out.println("---------------xxxx-----------");
        RunJavacTest.runJavac(testPrjF);          
        assertEquals(" version processed ",1,handler.count);
    }
}
