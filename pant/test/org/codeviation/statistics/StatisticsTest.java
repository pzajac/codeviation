
package org.codeviation.statistics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class StatisticsTest extends TestCase {
    
    public StatisticsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        ExamplesSetup.initNbCvsTestingCache();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void doStatistics(ChartConf conf) throws ParseException {
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        assertNotNull(rep);
        SourceRoot srcRoot = rep.getSourceRoot("core/progress/src");
        assertNotNull(srcRoot);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date from = format.parse("2005/1/1");
        Date to = format.parse("2006/5/1");
          
        Statistics stats = new Statistics(from,to); 
        stats.setItemsCount(10);
        JavaFileHandler handler = conf.getStatHandler();
        assertNotNull(handler);
        stats.setJavaFileHandler(handler);
        handler.init(stats);
        stats.addSourceRoot(srcRoot);
        JFreeChart chart = stats.getChart(conf, false);
        assertNotNull(chart);
        
//        for (int i = 0 ; i <= stats.getLastIndex() ; i++ ) {
//            Record rec = stats.getItemAt(i);
//            System.out.println("rec : " + rec.getDimensions());
//            for (int d = 0 ; d < rec.getDimensions() ; d++) {
//                System.out.print(rec.getValue(d) + ",");
//                System.out.print("");
//            }
//            rec.getDimensions();
//        }
     }
    
    public void testCountsStatistics() throws ParseException {
        doStatistics(new CountsStatHandler().getChartConfs()[0]);
    }
   
    public void testCVSStats() throws ParseException {
        ChartConf confs[] = new CVSStatHandler().getChartConfs();
        for (ChartConf conf : confs) {
            doStatistics(conf);
        }
    }
    
    public void testPageRankStats() throws ParseException {
        ChartConf confs[] = new PageRankStatHandler().getChartConfs();
        for (ChartConf conf : confs) {
            doStatistics(conf);
        }
    }
}
