
package org.codeviation.statistics;

import org.jfree.chart.JFreeChart;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.SourceRoot;

/**
 *
 * @author pzajac
 */
public final class GenericGraph implements Graph<GenericGraph> {
    private JavaFileHandler handler;
    private int itemsCount;
    private JFreeChart chart;
    /** Creates a new instance of GenericGraph */
    public GenericGraph() {
    }
    
    public void addJavaFile(JavaFile jf) {
        handler.addJavaFile(jf);
    }

    public void addJavaFiles(Iterable<JavaFile> jfs) {
        for (JavaFile jf : jfs) {
            addJavaFile(jf);
        }
    }

    public void addSourceRoot(SourceRoot srcRoot) {
        for (Package pack : srcRoot.getPackages()) {
            addJavaFiles(pack);            
        }
    }

    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }
    public JFreeChart getChart(ChartConf chartConf,boolean aditive) {
        return this.chart;
    }

    public void setJavaFileHandler(JavaFileHandler handler) {
        this.handler = handler;
    }

    public void setItemsCount(int count) {
        this.itemsCount = count;
    }

    public int getItemsCount() {
        return itemsCount;
    }

}
