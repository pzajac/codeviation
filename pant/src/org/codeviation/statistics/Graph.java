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
import org.codeviation.model.Package;
import org.codeviation.model.SourceRoot;

/**
 *
 * @author pzajac
 */
public interface Graph {
    public  void addJavaFile(JavaFile jf) ;
    public void addPackage(Package pkg) ;
    public void addSourceRoot(SourceRoot srcRoot);
    public JFreeChart getChart(ChartConf chart,boolean aditive);
    public void setJavaFileHandler(JavaFileHandler handler);
    public void setItemsCount(int count);
    public int getItemsCount();
}
