package org.codeviation.model;

/**
 * It describes a metric type.
 * @author pzajac
 */
public interface Metric {
     public String getName();
     public String getDescription();
     /** Can be stored to pantcache folder?
      */ 
     public boolean isPersistent();
     /** For example CVSMetric is valid only for javafiles in folder with "CVS" folder
      */
     public boolean isValid();
}
