
package org.codeviation.tasks;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.JFreeChart;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.statistics.math.Histogram;
import org.codeviation.statistics.ChartConf;
import org.codeviation.statistics.ChartUtils;
import org.codeviation.statistics.GenericGraph;
import org.codeviation.statistics.IssuesPageRankStatHandler;

/**
 * Input PageRank , Issues 
 * 
 * Output: 
 *   Sum all pageranks/classes for revision for:
 *   P1
 *   P2
 *   P3
 *   P4
 *   P5
 *   Other
 * 
 * Maybe it's better to show histogram for page rank. How to show histogram for PageRank:
 * Sort files by pageRank:
 *  
 */
public class IssuesPageRankProcess implements RepositoryProcess {
    static final int XSIZE = 800;
    static final int YSIZE = 640;
    
    
    /** Creates a new instance of PageRankProcess */
    public IssuesPageRankProcess() {
    }
    
    public boolean execute(Repository rep, RepositoryProcessEnv env) {
       
        IssuesPageRankStatHandler handler = new IssuesPageRankStatHandler();
        ChartConf conf = handler.getChartConfs()[0];
        GenericGraph graph = (GenericGraph)conf.createGraph();
        graph.setItemsCount(400);
        graph.setJavaFileHandler(handler);
        handler.init(graph);
        
        SourceRootFilter filter = env.getSourceRootFilter();
        for (SourceRoot srcRoot : rep.getSourceRoots()) {
            if (filter == null || filter.accept(srcRoot)) {
                for (Package pack : srcRoot.getPackages()) {
                    for (JavaFile jf : pack.getJavaFiles()) {
                        handler.addJavaFile(jf);
                    }
                }
            }
        } 
        
        try {
            conf.setTitle("Defects Probability Density");
            handler.initGraphPaint(conf);
            JFreeChart chart = graph.getChart(conf, false);
            ChartUtils.makeSeriesChartPrintable(chart,0);
            ChartUtils.chartToFile(new File(env.getWorkDir(),"AllSrcRootIssuesDensity.png"),chart, XSIZE,YSIZE);
                    
            handler.setGraphType(Histogram.GraphType.PROBABILITY_DISTRIBUTION);
            conf.setTitle("Defects Probability Distribution");
            handler.initGraphPaint(conf);
            chart = graph.getChart(conf, false);
            ChartUtils.makeSeriesChartPrintable(chart,0);
            ChartUtils.chartToFile(new File(env.getWorkDir(),"AllSrcRootIssuesDistribution.png"),chart, XSIZE,YSIZE);
            
            handler.setMaxKey(8000);

            conf.setTitle("Defects Probability Density - LReg  for class in  <0 - 8000> interval");
            handler.setGraphType(Histogram.GraphType.REGRESSION);
            handler.initGraphPaint(conf);
            chart = graph.getChart(conf, false);
            ChartUtils.makeSeriesChartPrintable(chart,0);
            ChartUtils.chartToFile(new File(env.getWorkDir(),"AllSrcRootIssuesLinearRegression.png"),chart, XSIZE,YSIZE);
            
        } catch  (IOException ioe) {
           Logger.getLogger(IssuesPageRankStatHandler.class.getName()).log(Level.SEVERE,ioe.getMessage(),ioe);
        }
        
        return true;
        
    }

    public String getName() {
        return "IssuesPageRank";
    }

    public String getDescription() {
       return "Maps PageRank to issues";
    }
}
