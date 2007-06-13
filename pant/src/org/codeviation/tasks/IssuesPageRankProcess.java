
package org.codeviation.tasks;

import com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.cipr.matrix.Vector;
import org.codeviation.math.LinearRegression;
import org.codeviation.math.PolynomialLinearRegression;
import org.codeviation.math.SingleLinearRegresion;
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
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.data.xy.XYSeries;

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
 
        
        
       int items[] = new int[]{20,50,400};
 //       int items[] = new int []{50};
        boolean createHistograms = false;
        
        for (int i = 0 ; i < items.length ; i++) {
            IssuesPageRankStatHandler handler = new IssuesPageRankStatHandler();
            ChartConf conf = handler.getChartConfs()[0];
            GenericGraph graph = (GenericGraph)conf.createGraph();
            graph.setItemsCount(items[i]);
            graph.setJavaFileHandler(handler);
            handler.init(graph);
            File workdir = new File(env.getWorkDir(),"dir" + i);
            workdir.mkdirs();
            SourceRootFilter filter = env.getSourceRootFilter();
            try {
                if (createHistograms) {
                    for (SourceRoot srcRoot : rep.getSourceRoots()) {
                            if (filter == null || filter.accept(srcRoot)) {
                                for (Package pack : srcRoot.getPackages()) {
                                    for (JavaFile jf : pack.getJavaFiles()) {
                                        handler.addJavaFile(jf);
                                    }
                                }
                            }
                    } 
                    handler.storeHistograms(workdir);
                } else {
                    handler.restoreHistograms(workdir);
                }
//                handler.normalizeCommits(items[i]);
                handler.setMaxKey(8000);

                conf.setTitle("Defects Probability Density");
                handler.initGraphPaint(conf);
                JFreeChart chart = graph.getChart(conf, false);
                ChartUtils.makeSeriesChartPrintable(chart,7);
                ChartUtils.chartToFile(new File(workdir,"AllSrcRootIssuesDensity.png"),chart, XSIZE,YSIZE);

                handler.setGraphType(Histogram.GraphType.PROBABILITY_DISTRIBUTION);
                conf.setTitle("Defects Probability Distribution");
                handler.initGraphPaint(conf);
                chart = graph.getChart(conf, false);
                ChartUtils.makeSeriesChartPrintable(chart,7);
                ChartUtils.chartToFile(new File(workdir,"AllSrcRootIssuesDistribution.png"),chart, XSIZE,YSIZE);

                handler.setMaxKey(10000);
                int deg = 4;
                handler.getBugsHistogram().setRegressionDeg(deg);
                handler.getFilesHistogram().setRegressionDeg(deg);
                for (Histogram hist : handler.getPriorityBugsHistogram()) {
                    hist.setRegressionDeg(deg);
                }
  
                conf.setTitle("Defects Probability Density - LReg  for class in  <0 - 8000> interval");
                handler.setGraphType(Histogram.GraphType.REGRESSION);
                
                handler.initGraphPaint(conf);
               
                chart = graph.getChart(conf, false);
                ChartUtils.makeSeriesChartPrintable(chart,7);
                ChartUtils.chartToFile(new File(workdir,"AllSrcRootIssuesLinearRegression.png"),chart, XSIZE,YSIZE);

                printResiduum(handler,deg);
                // 
                //
            } catch  (IOException ioe) {
               Logger.getLogger(IssuesPageRankStatHandler.class.getName()).log(Level.SEVERE,ioe.getMessage(),ioe);
            }
        }
        
        return true;
        
    }

    public String getName() {
        return "IssuesPageRank";
    }

    public String getDescription() {
       return "Maps PageRank to issues";
    }
    
    private  void printResiduum (IssuesPageRankStatHandler handler,int polyNomDef) {
        System.out.println();
        Histogram hist = handler.getBugsHistogram();
        printResiduum(hist,"All bugs",polyNomDef);
        
        hist = handler.getFilesHistogram();
//        series = hist.getXYSeries(true,"All files", 0, 8000, Histogram.GraphType.PROPABILITY_DENSITY);
        printResiduum(hist,"All files",polyNomDef);
        
        for (int i = 0 ; i < handler.getPriorityBugsHistogram().length ; i ++ ) {
            hist = handler.getPriorityBugsHistogram()[i];
  //          series = hist.getXYSeries(true,"P" + (i + 1),0,8000,Histogram.GraphType.PROPABILITY_DENSITY);
            printResiduum(hist,"P" + (i + 1),polyNomDef);
        }
    }

    private void printResiduum(Histogram hist,String name,int polynomDeg) {
        XYSeries series = hist.getXYSeries(true,name, 0, 8000, Histogram.GraphType.PROPABILITY_DENSITY);
        
        Vector vec[] = LinearRegression.convertSeriesToVectors(series);
        if (polynomDeg < 2 ) {
        SingleLinearRegresion slr = new SingleLinearRegresion(vec[0],vec[1]);
//        double coefs[] = LinearRegression.solve(vec[0], vec[1]);
//        double residuum = LinearRegression.residuumSquareSum(vec[0], vec[1], coefs[0], coefs[1]);
            System.out.println("Linear Regr: (" + hist.getGroups().length  + ")" + name + ", varianceEst: " + slr.getVarianceEstimation() + "," 
             + "Determinance Index: " +  slr.getDeterminanceIndex() + ", b0 EstimationEr: " + slr.getB0EstimatedError() + ", b1 EstimationEr:  " + slr.getB1EstimatedError() + ", test b1 = 0 (1%): " + slr.testB1(0, 0.01));
        } else {
            PolynomialLinearRegression slr = new PolynomialLinearRegression(polynomDeg,vec[0],vec[1]);
            System.out.println("Linear Regr: (" + hist.getGroups().length  + ")" + name 
             + "Determinance Index: " +  slr.getDeterminanceIndex() + ", Total integrations:" + hist.getCounts());
        }
             
    }
}
