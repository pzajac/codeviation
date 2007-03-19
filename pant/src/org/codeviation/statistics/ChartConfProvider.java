
package org.codeviation.statistics;

/**
 * It allows to register ChartConf to webrowser. Register implementations to
 * META-INF/server/org.codeviation.statistics.ChartConfProvider
 * 
 * @author pzajac
 */
public interface ChartConfProvider {
    /** get ChartConfs
     */
    ChartConf[] getChartConfs();
}
