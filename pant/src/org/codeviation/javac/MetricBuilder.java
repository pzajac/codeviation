
package org.codeviation.javac;


/**
 * SPI for getting metrics from PANT run.
 * 
 * Register service in META-INF/services/org.codeviation.javac.MetricBuilder
 * @author pzajac
 */
public interface MetricBuilder {

    /** Name of metrics builder
     */
    public String getName();
    
    /** description is usefull for intereacting selection of MetricBuilders' set
     */ 
    public String getDescription();
    
    /** Visit java element, inforamtion about JavaFile, Tag, CVS Version,   Trees,... are accessible via MetricsRunner
     */ 
    public void visit( javax.lang.model.element.Element e ); 
    /** Improves performance of mining metrics. It doesn't make sence to process the 
     * same file with the same revision more times for different CVS tags.   
     */ 
    public boolean canProcessTheSameRevision();    
    
}
