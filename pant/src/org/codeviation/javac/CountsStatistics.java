
package org.codeviation.javac;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;

/**
 *
 * @author pzajac
 */
public class CountsStatistics {
    int itemsCount;
    Date fromDate;
    Date toDate;
    // time step
    long step;
    CountsItem items[]; 
    
    /** Creates a new instance of CountsStatistics */
    public CountsStatistics(int itemsCount,Date from,Date to) {
        this.itemsCount = itemsCount;
        this.fromDate = from;
        this.toDate = to;
        init();
    }
    
    public void init() {
        items = new CountsItem[itemsCount];
        step = (toDate.getTime() - fromDate.getTime()) / (long)itemsCount;
        
        for (int i = 0 ; i < items.length ; i++) {
            items[i] = new CountsItem();
        }
    }
    
    public void addJavaFile(JavaFile jf) {
        CountsMetric cm = jf.getMetric(CountsMetric.class);
        for (Map.Entry<Version,CountsItem> entry :    cm.getEntries()) {
            int index = getIndex(entry.getKey());
            items[index].addCountsItem(entry.getValue());
        }
    }
    
    public void addPackage(Package pkg) {
        for (JavaFile jf : pkg.getJavaFiles()) {
            addJavaFile(jf);
        }
    }
    public void addSourceRoot(SourceRoot srcRoot) {
        for (Package pkg : srcRoot.getPackages()) {
            addPackage(pkg);
        }
    }
    private int getIndex(Version v) {
        long offset = v.getDate().getTime() - fromDate.getTime();
        return (int) (offset/step);
    }
    
    public JFreeChart getChart(Set<CountsItem.Type> types) {
       TimeSeriesCollection data = new TimeSeriesCollection();
         data.setDomainIsPointsInTime(false);
         long minDate = fromDate.getTime();
         long maxDate = toDate.getTime();
         long interval = maxDate - minDate;
         
         if (items.length > 0) {
             for (CountsItem.Type type : types) {
                 TimeSeries series = new TimeSeries(type.toString());
                 for (int i = 0 ; i < items.length ; i++ ) {
                     CountsItem rec = items[i];
                     float value = rec.getValue(type);
                     long timeLong = (long)(step*i + minDate);
                     Date time = new Date(timeLong);
                     series.add(new Day(time),value);
                 }
                 data.addSeries(series); 
             }
         }
       JFreeChart chart = ChartFactory.createTimeSeriesChart(
                                                         "Elements Counts",  // chart title
                                                         "Date",       // domain axis label
                                                         "Count",       // range axis label
                                                         data,      // data
                                                         true,      // include legend
                                                         true,
                                                         false
                                                     );
            StandardLegend sl = (StandardLegend) chart.getLegend();
            sl.setDisplaySeriesShapes(true);

            XYPlot plot = chart.getXYPlot();
            XYItemRenderer renderer = plot.getRenderer();
            if (renderer instanceof StandardXYItemRenderer) {
                StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
                rr.setPlotShapes(true);
                rr.setDefaultShapeFilled(true);
            }
            DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
       
         return chart;
    }
        

}
