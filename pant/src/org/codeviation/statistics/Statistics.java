
package org.codeviation.statistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Statistics chart 
 *  
 * @author pzajac
 */
public final class Statistics implements Graph {
    Date fromDate;
    Date toDate;
    // time step
    long step;
    Record items[];
    List keys;
    
//    Map<String,Object> params = new HashMap<String,Object>();
        Map<Object,Record> objToRecord ;
     JavaFileHandler statHandler;
    /** Creates a new instance of CountsStatistics */
    public Statistics(Date from,Date to) {
        this.fromDate = from;
        this.toDate = to;
    }
    public void setItemsCount(int count ) {
        this.items = new Record[count];
        init();
    }
    public void initKeys(List keys,Date from,Date to) {
        this.fromDate = from;
        this.toDate = to;
        this.keys = new ArrayList(keys);
        this.items = new Record[keys.size()];
        init();
        
        objToRecord = new HashMap<Object,Record>();
        for (int i = 0 ; i < keys.size() ; i++) {
            objToRecord.put(keys.get(i),items[i]);
        }
    }
    public void init() {
       int itemsCount = items.length;
       step = (toDate.getTime() - fromDate.getTime()) / (long)itemsCount;
        
        for (int i = 0 ; i < items.length ; i++) {
            items[i] = new Record();
        }
//        params.clear();
    }
//    public Object getParam(String name) {
//        return params.get(name);
//    }
//    public void putParam(String name,Object value) {
//        params.put(name,value);
//    }
    
    public  void addJavaFile(JavaFile jf) {
        statHandler.addJavaFile( jf);
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
//    public int getLastIndex() {
//        return items.length - 1;
//    }
    public int getIndex(long time) {
        long offset = time  - fromDate.getTime();
        int index =  (int) (offset/step);
        if (index < 0 ) {
            index = -1;
        }
        return index;
    }
    public int getIndex(Version v) {
        return getIndex(v.getDate().getTime());
    }
    public Record getItemAt(int index) {
        return items[index]; 
    }
    public Record getItem(long time) {
        int index = getIndex(time);
        return   (index > -1 && index < items.length) ? items[index] : null;
    }
    public Record getItem(Date date) {
        return getItem(date.getTime());
    }
    public Record getItem(Version v) {
        return  getItem(v.getDate().getTime());
    }

    public Record getItemForKey(Object key) {
        if (objToRecord == null) {
            throw new IllegalStateException("objToRecord has not been initialized");
        }
        return objToRecord.get(key);
    }
    public Date getFromDate() {
        return fromDate;
    }
    public Date getToDate() {
        return toDate;
    }
    
    public JFreeChart getChart(ChartConf chartConf) {
        return getChart(chartConf,false);
    }
    public JFreeChart getChart(ChartConf chartConf,boolean additive) {
        List<RecordType> types = chartConf.getRecordTypes();
        TimeSeriesCollection data = new TimeSeriesCollection();
        data.setDomainIsPointsInTime(false);
        long minDate = fromDate.getTime();
        long maxDate = toDate.getTime();
        long interval = maxDate - minDate;
        
        if (items.length > 0) {
            statHandler.initGraphPaint(chartConf);
            for (RecordType type : types) {
                TimeSeries series = new TimeSeries(type.getDisplayName());
                boolean additiveSer = additive && type.isSupportAdditive();
                float addValue = 0;
                for (int i = 0 ; i < items.length ; i++ ) {
                    Record rec = items[i];
                    float value = statHandler.getValue(rec,type);
                    if (additiveSer) {
                        addValue += value; 
                    } else {
                        addValue = value;
                    }
                    long timeLong = (long)(step*i + minDate);
                    Date time = new Date(timeLong);
                    series.add(new Day(time),addValue);
                }
                data.addSeries(series);
            }
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                chartConf.getTitle(),  // chart title
                chartConf.getXAxisTitle(),       // domain axis label
                chartConf.getYAxisTitle(),       // range axis label
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
        axis.setDateFormatOverride(new SimpleDateFormat("MM-yyyy"));
        return chart;
    }

    public void setJavaFileHandler(JavaFileHandler handler) {
        this.statHandler = handler;
    }

    public int getItemsCount() {
        return this.items.length;
    }
}
