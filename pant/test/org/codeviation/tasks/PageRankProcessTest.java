
package org.codeviation.tasks;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import junit.framework.TestCase;
import org.jfree.chart.JFreeChart;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;
import org.codeviation.main.PrepareNbTags;
import org.codeviation.statistics.ChartConf;
import org.codeviation.statistics.JavaFileHandler;
import org.codeviation.statistics.PageRankStatHandler;
import org.codeviation.statistics.Statistics;
import org.codeviation.table.DefaultTable;
import org.codeviation.table.PageRankTableConf;

/**
 *
 * @author pzajac
 */
public class PageRankProcessTest extends TestCase {
    
    public PageRankProcessTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        ExamplesSetup.initNbCvsTestingCache();
    }

    public void testOpenideFs() throws ParseException {
        final String SRC_ROOT = "openide/fs/src";
        Repository rep =  PersistenceManager.getDefault().getRepository("nbcvs");
        Set<String> tags = rep.getSourceRoot(SRC_ROOT).getCvsTags();
        RepositoryProcess process = new PageRankProcess();
        File pwd = new File(TestUtil.getWorkDir(),"PageRankProcessTest.testOpenideFs");
        TestUtil.deleteFile(pwd);
        pwd.mkdirs();
        RepositoryProcessEnv env = new RepositoryProcessEnv();
        env.setWorkDir(pwd);
        env.setTags(tags);
        env.setSourceRootFilter(new SourceRootFilter() {
            public boolean accept(SourceRoot srcRoot) {
                return srcRoot.getRelPath().equals(SRC_ROOT);
            }
        });
        process.execute(rep, env);
        
        SourceRoot srcRoot = rep.getSourceRoot(SRC_ROOT);
        Package pack = srcRoot.getPackage("org.openide.filesystems");
        JavaFile jf = pack.getJavaFile("FileObject.java");
        PageRankMetric prm = jf.getMetric(PageRankMetric.class);
        assertFalse("Not empty tags count", prm.getTags().isEmpty());
        
        // test graph generation
        ChartConf chartConf = new PageRankStatHandler().getChartConfs()[0];
        
       Date minDate = PrepareNbTags.parseDate("200501011900");
       Date maxDate = PrepareNbTags.parseDate("200701011900");

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(minDate);
        assertEquals("year ",2005,calendar.get(Calendar.YEAR));
        assertEquals("month ",0,calendar.get(Calendar.MONTH));
        assertEquals("day ",1,calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("hour ",19,calendar.get(Calendar.HOUR_OF_DAY));
        Statistics stats = new Statistics(minDate,maxDate); 
        stats.setItemsCount(10);
        JavaFileHandler handler = chartConf.getStatHandler();
        handler.init(stats);
        stats.setJavaFileHandler(handler);
        stats.addSourceRoot(srcRoot);
        JFreeChart chart = stats.getChart(chartConf, false);
        assertNotNull(chart);
        
        // test PageRankTableConf
        PageRankTableConf prtf = new PageRankTableConf();
        prtf.addSourceRoot(srcRoot);
        DefaultTable table = (DefaultTable)prtf.getTable();
        assertNotNull(table);
        String rows [] = table.getRowsHeader();
        for (String row : rows) {
            System.out.println(row);
        }
        for (String column : table.getColumnsHeader()) {
            System.out.println(column);
        }
        boolean found = false;
        for (int r = 0 ; r < table.getRows() ; r++) {
            for (int c = 0 ; c < table.getColumns() ; c++) {
                if (table.getData(r, c) != null) {
                    found = true;
                }
            }
        }
        assertTrue(found);
 
    }
}
