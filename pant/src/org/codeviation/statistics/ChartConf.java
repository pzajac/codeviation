
package org.codeviation.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for one chart
* @param GRAPH extends Graph
 */
public class ChartConf <GRAPH extends Graph>{
    private String title;
    private String xAxisTitle;
    private String yAxisTitle;
    private String name;
    private List <RecordType> recordTypes;
    private JavaFileHandler<GRAPH> statHandler;
    private Class<GRAPH> graphClass;
    
    public ChartConf(String title,
            String xAxisTitle,
            String yAxisTitle,
            String name,
            List<RecordType> recordTypes,
            JavaFileHandler<GRAPH> statHandler) {
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
    
    public JavaFileHandler<GRAPH> getStatHandler() {
        return statHandler;
    }
    
    public void setGraphClass(Class<GRAPH> graphClass) {
        this.graphClass = graphClass;
    }

    public String toString() {
        return name;
    }
    public GRAPH createGraph() {
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

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChartConf other = (ChartConf) obj;
        if (this.title != other.title && (this.title == null || !this.title.equals(other.title))) {
            return false;
        }
        if (this.xAxisTitle != other.xAxisTitle &&
            (this.xAxisTitle == null || !this.xAxisTitle.equals(other.xAxisTitle))) {
            return false;
        }
        if (this.yAxisTitle != other.yAxisTitle &&
            (this.yAxisTitle == null || !this.yAxisTitle.equals(other.yAxisTitle))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.recordTypes != other.recordTypes &&
            (this.recordTypes == null || !this.recordTypes.equals(other.recordTypes))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.title != null
                            ? this.title.hashCode(): 0;
        hash = 29 * hash + this.xAxisTitle != null
                            ? this.xAxisTitle.hashCode(): 0;
        hash = 29 * hash + this.yAxisTitle != null
                            ? this.yAxisTitle.hashCode(): 0;
        hash = 29 * hash + this.name != null
                            ? this.name.hashCode(): 0;
        hash = 29 * hash + (this.recordTypes != null
                            ? this.recordTypes.hashCode(): 0);
        return hash;
    }

    private static void logException(Exception e) {
        Logger.getLogger(ChartConf.class.getName()).log(Level.SEVERE,e.getMessage(),e);
    }
}
