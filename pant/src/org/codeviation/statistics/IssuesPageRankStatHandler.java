
package org.codeviation.statistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.tasks.PageRankMetric;
import org.codeviation.tasks.PageRankMetric.Value;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.statistics.math.Histogram;
import org.codeviation.statistics.math.Histogram.GraphType;
import org.codeviation.bugtracking.issuezilla.Issue;
import org.codeviation.bugtracking.issuezilla.IssuezillaUtil;
import org.codeviation.bugtracking.issuezilla.Priority;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Histogram:
 * X - Page rank in sourceroot
 * Y - number of issues
 * @author pzajac
 */
public final class IssuesPageRankStatHandler implements JavaFileHandler<GenericGraph>,ChartConfProvider{
    public  static final RecordType  PAGE_RANK_RECORD_TYPE = new RecordType("Issues ClassRank Histogram",0,false);
    public  static final RecordType  RATIO_PAGE_RANK_RECORD_TYPE = new RecordType("Fix/All Commits for ClassRank Histogram",0,false);
    
    
    private static final String BUGS_HISTOGRAM_FILE = "bugshistigram.ser"; 
    private static final String PB_HISTOGRAM_FILE_PREFIX = "pb";
    private static final String FILE_HISTOGRAM_FILE = "filehistogram.ser"; 
    private Histogram bugsHistogram;
    private Histogram priorityBugsHistogram[];
    private Histogram filesHistogram;
    private GenericGraph graph;

    private double minKey = Double.MIN_VALUE;
    private double maxKey = Double.MAX_VALUE;
    
    private boolean ignoreIssuesPriority;
    
    static Logger logger = Logger.getLogger(IssuesPageRankStatHandler.class.getName());

    private Histogram.GraphType graphType;
    

    public static enum PageRankValueType {
        PAGE_RANK,
        LOG_PAGE_RANK,
        POSITION,
        POSITION_RATIO
    } 
    /** experimental logaritmic axis for PageRank
     * rather use position
     */
    private PageRankValueType pageRankType = PageRankValueType.POSITION;


    /** divide all histograms by number of all integrations. 
     *  
     */
    public void normalizeCommits(int itemsCount) {
       filesHistogram.setSteps(itemsCount);
       bugsHistogram.divideByHistogram(filesHistogram); 
       for (int i = 0 ; i < priorityBugsHistogram.length ; i++) {
           priorityBugsHistogram[i].divideByHistogram(filesHistogram);
       } 
       filesHistogram.divideByHistogram(filesHistogram);
    }

    
    public void setPageRankValueType(PageRankValueType pageRankType) {
        this.pageRankType = pageRankType;
    }

    public PageRankValueType isLogPageRank() {
        return pageRankType;
    }
    public GraphType getGraphType() {
        return graphType;
    }

    public void setGraphType(GraphType graphType) {
        this.graphType = graphType;
    }

    public void init(GenericGraph graph) {
        bugsHistogram = new Histogram();
        filesHistogram = new Histogram();
        int plen = Priority.values().length;
        priorityBugsHistogram = new Histogram[plen];
        for (int i = 0 ; i < plen ; i++) {
            priorityBugsHistogram[i] = new Histogram();
        }
        this.graph = graph;
        ignoreIssuesPriority = true;
        try {
            IssuezillaUtil.getConnection();
            ignoreIssuesPriority = false;
        } catch (SQLException e) {
            
        }
    }
    public void setMaxKey(double maxKey) {
        this.maxKey = maxKey;
    }

    public void setMinKey(double minKey) {
        this.minKey = minKey;
    }

    public void addJavaFile( JavaFile jf) {
        PageRankMetric rankMetric = jf.getMetric(PageRankMetric.class);
        SourceRoot srcRoot = jf.getPackage().getSourceRoot();
        Repository rep = srcRoot.getRepository();
        CVSMetric cvs = jf.getCVSResultMetric();
        if (cvs != null) {
            if (rankMetric != null) {
                for (String tag : rankMetric.getTags()) {
                    double rank = 0;
                    Value val = rankMetric.getValue(tag);
                    if (val == null) {
                        continue;
                    }
                    switch (pageRankType) {
                    case LOG_PAGE_RANK:
                        rank = Math.log10(val.getRank());
                        break;
                    case PAGE_RANK:
                        rank = val.getRank();
                        break;
                    case POSITION:
                        rank = val.getPosition();
                        break;
                    case POSITION_RATIO:
                        rank = val.getRatio();
                        break;
                    default:
                        throw new IllegalStateException (jf.getName());
                    }
                     
                    
                    
                    Date tagDate = rep.getTagDate(tag);
                    if (tagDate == null) {
                        throw new IllegalStateException("Null tagDate for : " + tag + jf.getPackage().getName() + "." + jf.getName());
                    }
                    Version prevTag = null;
                  
                    Version ver = cvs.getVersion(tagDate);   
                    while (prevTag != ver) {
                        if (prevTag == null) {
                            prevTag = ver;
                        } else {
                            prevTag = ver.getNext();
                        }
                        if (prevTag != null) {
                            filesHistogram.addValue(rank, 1);
                            if (prevTag.getDefectNumbers().length > 0) {
                                bugsHistogram.addValue(rank, 1);
                                if (!ignoreIssuesPriority ) {
                                    for (int bug : prevTag.getDefectNumbers()) {
                                        // XXX  previous version should be better
                                        try {
                                            Issue issue = Issue.readIssue(bug);
                                            // for example is not available connection
                                            if (issue != null) {
                                                int priority = issue.getPriority().getPriority();
                                                priorityBugsHistogram[priority].addValue(rank,1);
                                            } else {
                                                // xxx
    //                                            ignoreIssuesPriority = true;
                                            }
                                        } catch (IOException ioe) {
                                            // probably connection doesn't exist
                                            logger.log(Level.FINE,ioe.getMessage(),ioe);
                                        } catch (SQLException sqe) {
                                            // probably connection doesn't exist
                                            logger.log(Level.FINE,sqe.getMessage(),sqe);
                                        }
                                    }
                                }
                            }
                        }
                   }
                }
            }
        }
    }
    
   public void initGraphPaint(ChartConf conf) {
        XYSeriesCollection allSeries = new XYSeriesCollection();
        int count = graph.getItemsCount();
        if (!bugsHistogram.isEmpty()) {
            if (count > 0) {
                bugsHistogram.setSteps(count);
            }
            allSeries.addSeries(bugsHistogram.getXYSeries(true,"All Bugs",minKey,maxKey,graphType));
        }
        if (!filesHistogram.isEmpty()) {
            if (count > 0) {
                filesHistogram.setSteps(count);
            }
            allSeries.addSeries(filesHistogram.getXYSeries(true,"All Changes",minKey,maxKey,graphType));
        }
        for (int i = 0 ; i < priorityBugsHistogram.length ; i++) {
            if (!priorityBugsHistogram[i].isEmpty()) {
                if (count > 0) {
                    priorityBugsHistogram[i].setSteps(count);
                }
                allSeries.addSeries(priorityBugsHistogram[i].getXYSeries(true,Priority.get(i).toString(),minKey,maxKey,graphType));
            }
        }
       
        JFreeChart chart = ChartFactory.createXYLineChart(null,
                                                  conf.getXAxisTitle(), 
                                                  conf.getYAxisTitle(), 
                                                  allSeries,
                                                  PlotOrientation.VERTICAL,
                                                  true,
                                                  true,
                                                  false);
        graph.setChart(chart);
   }
        
    public float getValue(Record rec, RecordType type) {
        //return rec.getValue(0);
        return 0;
    }
        
    public ChartConf[] getChartConfs() {
        ChartConf<GenericGraph> conf = new ChartConf<GenericGraph>(null,"Classes", 
             "Probability ",null,
            Arrays.asList(PAGE_RANK_RECORD_TYPE,RATIO_PAGE_RANK_RECORD_TYPE), new IssuesPageRankStatHandler());
        conf.setGraphClass(GenericGraph.class);
        return new ChartConf[] {conf};
    }

    public void storeHistograms(File workDir) throws  IOException {
        File f = new File(workDir,BUGS_HISTOGRAM_FILE);
        writeHist(workDir,BUGS_HISTOGRAM_FILE,bugsHistogram);
        writeHist(workDir,FILE_HISTOGRAM_FILE,filesHistogram);
        for (int i = 0 ; i < priorityBugsHistogram.length ; i++) {
            writeHist(workDir, PB_HISTOGRAM_FILE_PREFIX + i + ".ser", priorityBugsHistogram[i]);
        }
        
    }
    public void restoreHistograms(File workDir) throws IOException {
        bugsHistogram = restoreHist(workDir,BUGS_HISTOGRAM_FILE);
        filesHistogram = restoreHist(workDir,FILE_HISTOGRAM_FILE);
        for (int i = 0 ; i < priorityBugsHistogram.length ; i++) {
            priorityBugsHistogram[i] = restoreHist(workDir, PB_HISTOGRAM_FILE_PREFIX + i + ".ser" );
        }
    }

    private Histogram restoreHist(File workDir, String fileName) throws IOException {
        File file = new File(workDir,fileName);
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            try {
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                   return (Histogram) ois.readObject();
                } catch (ClassNotFoundException cnfe) {
                   logger.log(Level.SEVERE, cnfe.getMessage(), cnfe); 
                } finally {
                    ois.close();
                }  
            } finally {
                fis.close();
            }
        }
        return null;
    }

    private void writeHist(File workDir, String fileName, Histogram hist) throws IOException {
        if (hist != null) {
            FileOutputStream fos = new FileOutputStream(new File(workDir,fileName));
            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                try {
                    oos.writeObject(hist);
                } finally {
                    oos.close();
                }  
            } finally {
                fos.close();
            }
        }
    }

    public Histogram getBugsHistogram() {
        return bugsHistogram;
    }

    public Histogram getFilesHistogram() {
        return filesHistogram;
    }

    public Histogram[] getPriorityBugsHistogram() {
        return priorityBugsHistogram;
    }

    
}

