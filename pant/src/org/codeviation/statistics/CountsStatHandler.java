
package org.codeviation.statistics;

import java.util.Arrays;
import java.util.logging.Logger;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.JavaFile;
import org.codeviation.javac.CountsItem;
import org.codeviation.javac.CountsMetric;

/**
 * Statistics Handler for CountsMetric
 * @author pzajac
 */
public final class CountsStatHandler implements JavaFileHandler<Statistics<String>>,ChartConfProvider{
    Statistics stats;
    static Logger logger = Logger.getLogger(Statistics.class.getName());

    public void addJavaFile( JavaFile jf) {
        StatisticsUtil.addVersionedVector(jf, stats, CountsMetric.class);
    }

    public float getValue(Record rec, RecordType type) {
        RecordType[] rts = CountsItem.RECORD_TYPES;
        if (type.getId() >= rts.length || rts[type.getId()] != type) {
            throw new IllegalArgumentException("Invalid type " + type.toString());
        }
        return rec.getValue(type.getId());
    }
    
    public ChartConf[] getChartConfs() {
        return new ChartConf[] {new ChartConf<Statistics<String>>("Class Elements Counts", 
                "Date", "Count of elements", "Class Elements Counts",
                Arrays.asList(CountsItem.RT_CLASSES,CountsItem.RT_FIELDS,CountsItem.RT_METHODS), new CountsStatHandler()),
                new ChartConf<Statistics<String>>("Class Elements Counts", 
                "Date", "Count of elements", "All Elements Counts",
                Arrays.asList(CountsItem.RECORD_TYPES), new CountsStatHandler())
        };
    }

    public void init(Statistics stats) {
        this.stats = stats;
    }

    public void initGraphPaint(ChartConf conf) {
        int index = stats.getItemsCount()  - 1;
        while (index > -1 && stats.getItemAt(index).getValues() == null) {
            index--;
        }
        if (index > 0 ) {
            Vector values = stats.getItemAt(index).getValues();
            for (; index < stats.getItemsCount() ; index++) {
                stats.getItemAt(index).setValues(values);
            }
        }
    }
}
