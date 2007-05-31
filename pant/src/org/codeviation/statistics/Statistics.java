
package org.codeviation.statistics;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import no.uib.cipr.matrix.Vector;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Version;
import org.codeviation.model.Version.State;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.RectangleInsets;

/**
 * Statistics chart 
 *  
 * @param T keys
 * @author pzajac
 */
public final class Statistics<T> implements Graph<Statistics<T>> {
    Date fromDate;
    Date toDate;
    // time step
    long step;
    Record items[];
    List<T> keys;

    static Logger logger = Logger.getLogger(Statistics.class.getName());
//    Map<String,Object> params = new HashMap<String,Object>();
        Map<T,Record> objToRecord ;
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
    public void initKeys(List<T> keys,Date from,Date to) {
        this.fromDate = from;
        this.toDate = to;
        this.keys = new ArrayList<T>(keys);
        this.items = new Record[keys.size()];
        init();
        
        objToRecord = new HashMap<T,Record>();
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
    public void addJavaFiles(Iterable<JavaFile> jfs) {
        for (JavaFile jf : jfs) {
            addJavaFile(jf);
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
   public void addValues(Map<Version,Vector> values) {
        Iterator<Map.Entry<Version,Vector>> entryIt = values.entrySet().iterator();
        if (!entryIt.hasNext()) {
            return ;
        }
        Map.Entry<Version,Vector> prev = entryIt.next();
        while(entryIt.hasNext()) {
                Map.Entry<Version,Vector> next = entryIt.next();
                int nextRec = getIndex(next.getKey());
                int prevRec =  -1 ;
                    prevRec = nextRec;
                    if (prev.getKey().getState() == State.DEAD) {
                        continue;
                    }
                    if (prevRec == -1) {
                        if (prev.getKey().getDate().compareTo(getFromDate()) < 0)  {
                            prevRec = 0;
                        } else {
                            break;
                        }
                    }
                    //nextVer = versions.get(i);
                    nextRec = getIndex(next.getKey());
                    if (nextRec == -1) {
                        nextRec = getItemsCount() - 1;
                        if (nextRec == prevRec) {
                            break;
                        }
                    } else if (nextRec >= getItemsCount() - 1 || !entryIt.hasNext()) {
                        nextRec = getItemsCount() ;
                    }
                    // fill all Records between <prevRec,nextRec>
                    //
                    Vector vec = prev.getValue();
                    for (int rec = prevRec ; rec < nextRec ; rec++) {
                        getItemAt(rec).add(vec);
                    }
            }        
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

    public Record getItemForKey(T key) {
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
    
    public JFreeChart getChart(ChartConf<Statistics<T>> chartConf) {
        return getChart(chartConf,false);
    }
    public JFreeChart getChart(ChartConf<Statistics<T>> chartConf,boolean additive) {
        List<RecordType> types = chartConf.getRecordTypes();
        TimeSeriesCollection data = new TimeSeriesCollection();
      //  data.setDomainIsPointsInTime(false);
        long minDate = fromDate.getTime();
        long maxDate = toDate.getTime();
        long interval = maxDate - minDate;
        Set<Day> days = new HashSet<Day>();
        
        if (items.length > 0) {
            statHandler.initGraphPaint(chartConf);
            for (RecordType type : types) {
                days.clear();
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
                    long timeLong = (step*i + minDate);
                    Date time = new Date(timeLong);
                    Day day = new Day(time);
                    if (!days.contains(day)) {
                        days.add(day);
                        series.add(new Day(time),addValue);
                    }
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
//       StandardLegend sl = (StandardLegend) chart.getLegend();
//        sl.setDisplaySeriesShapes(true);
        
        XYPlot plot = chart.getXYPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-yyyy"));
        
        
        chart.setBackgroundPaint(Color.WHITE);
        chart.setTitle((String)null);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        
        plot.setWeight(10);
        
        XYItemRenderer r = plot.getRenderer();
        System.out.println(r.getClass());
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
      //      renderer.setBaseShapesVisible(true);
//            renderer.setBaseShapesFilled(true);
            renderer.setUseFillPaint(true);
            System.out.println(renderer.getLegendItemLabelGenerator());
            renderer.setBaseItemLabelsVisible(true);
            //renderer.setSeriesItemLabelsVisible(1, true);
            renderer.setUseOutlinePaint(false);
//            XYTextAnnotation annotation = new XYPointerAnnotation("Ahoj",new Month(2, 2006).getLastMillisecond(),2000,3.14);
//            renderer.addAnnotation(annotation);
            
//            renderer.setStroke(new BasicStroke(2f));
//            renderer.setSeriesStroke(1, new BasicStroke(4.0f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,6,0,6}, 0.0f));
//            renderer.getPlot().setRenderer(1, r)getRenderer(1).setStroke();
//            renderer.setStroke();
            
  //          renderer.setS
        }
        
        
    //    chart.getLegend().setPosition(RectangleEdge.RIGHT);

        return chart;
    }

    public void setJavaFileHandler(JavaFileHandler handler) {
        this.statHandler = handler;
    }

    public int getItemsCount() {
        return this.items.length;
    }
}
