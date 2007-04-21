/*
 * IssuesPageRankStatHandlerTest.java
 * JUnit based test
 *
 * Created on February 22, 2007, 8:22 AM
 */

package org.codeviation.statistics;

import junit.framework.TestCase;
import org.jfree.chart.JFreeChart;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class IssuesPageRankStatHandlerTest extends TestCase {
    
    public IssuesPageRankStatHandlerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        ExamplesSetup.initNbCvsTestingCache();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPageRank() {
        ChartConf confs[] = new IssuesPageRankStatHandler().getChartConfs();
        for (ChartConf conf : confs) {
            //doStatistics(conf);
            Graph graph = conf.createGraph();
            JavaFileHandler handler = conf.getStatHandler();
            graph.setJavaFileHandler(handler);
            handler.init(graph);

            Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
            assertNotNull(rep);
            SourceRoot srcRoot = rep.getSourceRoot("apisupport/project/src");
            assertNotNull(srcRoot);
            
            graph.addJavaFiles(srcRoot);
            handler.initGraphPaint(conf);
            JFreeChart chart = graph.getChart(conf, false);
                
        }
    }
}
