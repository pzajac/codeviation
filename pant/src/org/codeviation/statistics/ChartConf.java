
package org.codeviation.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for one chart
 */
public class ChartConf {
    private String title;
    private String xAxisTitle;
    private String yAxisTitle;
    private String name;
    private List <RecordType> recordTypes;
    private JavaFileHandler statHandler;
    private Class<Graph> graphClass;
    
    public ChartConf(String title,
            String xAxisTitle,
            String yAxisTitle,
            String name,
            List<RecordType> recordTypes,
            JavaFileHandler statHandler) {
        this.title = title;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
        this.name = name;
        this.recordTypes = recordTypes;
        this.statHandler = statHandler;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getXAxisTitle() {
        return xAxisTitle;
    }
    
    public void setYAxisTitle(String yAxisTitle) {
        this.yAxisTitle = yAxisTitle;
    }

    public void setXAxisTitle(String xAxisTitle) {
        this.xAxisTitle = xAxisTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYAxisTitle() {
        return yAxisTitle;
    }
    public String getName() {
        return name;
    }
    
    public List<RecordType> getRecordTypes() {
        return new ArrayList<RecordType>(recordTypes);
    }
    
    public JavaFileHandler getStatHandler() {
        return statHandler;
    }
    
    public void setGraphClass(Class graphClass) {
        this.graphClass = graphClass;
    }

    public String toString() {
        return name;
    }
    public Graph createGraph() {
        if (graphClass != null) {
            try {
                return graphClass.newInstance();
            }
            catch (InstantiationException ex) {
                logException(ex);
            }
            catch (IllegalAccessException ex) {
                logException(ex);
            }
        }
        return null;
    }
    private static void logException(Exception e) {
        Logger.getLogger(ChartConf.class.getName()).log(Level.SEVERE,e.getMessage(),e);
    }
}
