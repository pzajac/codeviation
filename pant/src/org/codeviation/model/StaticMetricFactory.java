
package org.codeviation.model;

import org.codeviation.model.StaticMetric;

/**
 *
 * @author pzajac
 */
public final class StaticMetricFactory {
    
    /** Creates a new instance of StaticMetricFactory */
    private StaticMetricFactory() {
    }
    
    static <T extends Metric>T getMetric(JavaFile file,Class<T> type) {
       try {
           T metric = type.newInstance();
           if (metric instanceof StaticMetric) {
               ((StaticMetric)metric).setJavaFile(file);
               if (metric.isValid()) {
                   return metric;
               }
           }
       }
       catch (InstantiationException ex) {
          throw new IllegalStateException(ex);
       }
       catch (IllegalAccessException ex) {
           throw new IllegalStateException(ex);
       }
       return null;
    }
    
//    public void registerType(Class<MetricResult> result) {
//        classes.add(result);
//    }
//
//    static boolean isStaticMetric(MetricResult mr) {
//        return classes.contains(mr.getClass());
//    }
}
