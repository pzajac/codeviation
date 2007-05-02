
package org.codeviation.statistics;

/**
 * It allows to register ChartConf to webrowser. Register implementations to
 * META-INF/server/org.codeviation.statistics.ChartConfProvider
 * 
 * @param GRAPH 
 * @author pzajac
 */
public interface ChartConfProvider<GRAPH extends Graph> {
    /** get ChartConfs
     * @return array of all supported ChartConfs
     */
    ChartConf<GRAPH>[] getChartConfs();
}
