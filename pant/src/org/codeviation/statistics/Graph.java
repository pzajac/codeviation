/*
 * Graph.java
 *
 * Created on February 21, 2007, 5:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.statistics;

import org.jfree.chart.JFreeChart;
import org.codeviation.model.JavaFile;

/**
 *
 * @author pzajac
 */
public interface Graph<GRAPH extends Graph> {
    public  void addJavaFiles(Iterable<JavaFile> jfs) ;
    public JFreeChart getChart(ChartConf<GRAPH> chart,boolean aditive);
    public void setJavaFileHandler(JavaFileHandler<GRAPH> handler);
    public void setItemsCount(int count);
    public int getItemsCount();
}
