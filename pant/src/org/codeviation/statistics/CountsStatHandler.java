
package org.codeviation.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.JavaFile;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.javac.CountsItem;
import org.codeviation.javac.CountsMetric;

/**
 * Statistics Handler for CountsMetric
 * @author pzajac
 */
public final class CountsStatHandler implements JavaFileHandler<Statistics>,ChartConfProvider{
    Statistics stats;
    

    public void addJavaFile( JavaFile jf) {
        CountsMetric cm = jf.getMetric(CountsMetric.class);
        SourceRoot srcRoot = jf.getPackage().getSourceRoot();
        Date minDate = srcRoot.getMinTagDate();
        Date maxDate = srcRoot.getMaxTagDate();
        
        if (cm != null && minDate != null && maxDate != null) {
            List<Version> versions = new ArrayList<Version>(cm.getVersions());
            Collections.sort(versions, new VersionDateComparator());
            versions = Version.filterVersions(versions,minDate,maxDate);
            // XXX add first and last tag. We don't know if the file has been deleted
            if (!versions.isEmpty()) {
                Version nextVer = versions.get(0);
                Version prevVer = null;
                int nextRec = stats.getIndex(nextVer);
                int prevRec =  -1 ;
                for (int i = 1 ; i < versions.size() ; i++) {
                    prevVer = nextVer;
                    prevRec = nextRec;
                    if (prevRec == -1) {
                        if (prevVer.getDate().compareTo(stats.getFromDate()) < 0)  {
                            prevRec = 0;
                        } else {
                            break;
                        }
                    }
                    nextVer = versions.get(i);
                    nextRec = stats.getIndex(nextVer);
                    if (nextRec == -1) {
                        nextRec = stats.getItemsCount() - 1;
                        if (nextRec == prevRec) {
                            break;
                        }
                    } else if (nextRec > stats.getItemsCount() - 1) {
                        nextRec = stats.getItemsCount() - 1;
                    }
                    // fill all Records between <prevRec,nextRec>
                    //
                    Vector vec = cm.get(prevVer).getVector();
                    for (int rec = prevRec ; rec < nextRec ; rec++) {
                        stats.getItemAt(rec).add(vec);
                    }
                }
            }
        }
    }

    public float getValue(Record rec, RecordType type) {
        RecordType[] rts = CountsItem.RECORD_TYPES;
        if (type.getId() >= rts.length || rts[type.getId()] != type) {
            throw new IllegalArgumentException("Invalid type " + type.toString());
        }
        return rec.getValue(type.getId());
    }
    
    public ChartConf[] getChartConfs() {
        return new ChartConf[] {new ChartConf("Class Elements Counts", 
                "Date", "Count", "Class Elements Counts",
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
