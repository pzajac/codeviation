
package org.codeviation.statistics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.jfree.chart.JFreeChart;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
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
        stats.addJavaFiles(srcRoot);
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
    
    public void testAddValues() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        Date min = format.parse("2001/01/01 00:00");
        Date max = format.parse("2002/02/02 00:00");
        Date d1 = format.parse("2000/10/10 00:00");
        Date d2 = min;
        Date d3 = format.parse("2001/02/01 00:00");
        Date d4 = format.parse("2001/03/01 00:00");
        Date d5 = max;
        Date d6 = format.parse("2002/03/03 00:00");
        
        
        Version v1 = new Version("1.1","a",d1,"zajo",Version.State.EXP);
        Version v2 = new Version("1.2","a",d2,"zajo",Version.State.EXP);
        Version v3 = new Version("1.3","a",d3,"zajo",Version.State.EXP);
        Version v4 = new Version("1.4","a",d4,"zajo",Version.State.EXP);
        Version v5 = new Version("1.5","a",d5,"zajo",Version.State.EXP);
        Version v6 = new Version("1.6","a",d6,"zajo",Version.State.EXP);

        Statistics<String> stats = new Statistics<String>(min,max);
        
        stats.setItemsCount(20);
        
        Map<Version,Vector> entries = new HashMap<Version, Vector>();
        
        entries.put(v1, createVector(1));
        entries.put(v2, createVector(2));
        entries.put(v3, createVector(1));
        entries.put(v4, createVector(5));
        entries.put(v5, createVector(4));
        entries.put(v6, createVector(5));
        
        stats.init();
        stats.addValues(entries);
        
        for (int i = 0 ; i < stats.getItemsCount(); i++) {
            Record rec = stats.getItemAt(i);
            System.out.println(rec.getValue(0));
        }
        fail();
        
    }
    private Vector createVector(double val) {
        Vector vec = new DenseVector(1);
        vec.set(0, val);
        return vec;
    }
}
