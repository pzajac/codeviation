
package org.codeviation.statistics;

import org.codeviation.model.JavaFile;

/** Add some statistics to Statistics for javaFile
 * @param GRAPH graph with JFreeChart
 */
public interface JavaFileHandler <GRAPH extends Graph> {
    
    /** invoked before first addJavaFile(...)
     * @param stats GRAPH
     */
    public void init(GRAPH stats) ;
    
    /** invoked before first getValue()
     * @param conf 
     */
    public void initGraphPaint(ChartConf conf);
    
    /** init values , possible vay how to add values: <br>
     *  stats.getItem(...).add(...)
     *  @param jf java file for getting values
     */
    public void addJavaFile(JavaFile jf);
    /** @return value from Record for Record type
     *  @param rec record with values (initialized by addJavaFile)
     *  @param type of graph 
     */ 
    public float getValue(Record rec,RecordType type);
}
