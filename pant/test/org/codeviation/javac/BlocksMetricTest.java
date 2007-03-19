/*
 * BlocksMetricTest.java
 * JUnit based test
 *
 * Created on March 14, 2007, 8:07 PM
 */

package org.codeviation.javac;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.PositionVersionIntervalResultContainer;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.ExamplesSetup;
import org.codeviation.javac.impl.blocks.BlocksBuilder;
import org.codeviation.javac.impl.blocks.BlocksItem;
import org.codeviation.javac.impl.blocks.BlocksMetric;

/**
 *
 * @author pzajac
 */
public class BlocksMetricTest extends TestCase {
   private File testPrjF;
     
    public BlocksMetricTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        ExamplesSetup.checkoutExamples = false;
        TestUtil.clearCache();
        TestUtil.enableMetricLogger();
        testPrjF = ExamplesSetup.getBlockTestDir();
        MetricsRunner.clearMetrics();
        MetricsRunner.addMetric(new BlocksBuilder());
     }
    
    public void testSimple() throws IOException, InterruptedException {
       String filePath = "pantexamples/testblockmetrics/src/testblockmetrics/Simple.java";
       ExamplesSetup.updateFile(filePath, "1.2");
       RunJavacTest.runJavac(testPrjF);
       
       File file = new File(testPrjF,"src/testblockmetrics/Simple.java");
       JavaFile jf = JavaFile.getJavaFile(file, "testblockmetrics");
       BlocksMetric bm = jf.getMetric(BlocksMetric.class);
       assertNotNull(bm);
        CVSMetric cvsm = jf.getCVSResultMetric();
       PositionVersionIntervalResultContainer<BlocksItem> container = bm.getStorage(); 
       Set<PositionIntervalResult<BlocksItem>> results = container.getAllObjects();
       for (PositionIntervalResult<BlocksItem> result : results) {
           PositionInterval i = result.getInterval();
           System.out.println(i);
           System.out.println(result.getObject());
           System.out.println(cvsm.getContent(i.getStartPosition(),i.getEndPosition()));
       }
    }
  }
