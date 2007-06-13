
package org.codeviation.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.JavaFile;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.model.Version.State;
import org.codeviation.model.VersionedVector;

/**
 *
 * @author pzajac
 */
public final class StatisticsUtil {

    
    /** add versioned vector of Metric to the statistics
     * @param jf file with metric
     * @param stats Statistics
     * @param metricClass Metric
     */
    public static void addVersionedVector( JavaFile jf,Statistics stats,Class<? extends VersionedVector> metricClass) {
        VersionedVector cm = (VersionedVector) jf.getMetric(metricClass);
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
                    if (prevVer.getState() == State.DEAD) {
                        continue;
                    }
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
                    } else if (nextRec >= stats.getItemsCount() - 1 || i + 1 ==  versions.size()) {
                        nextRec = stats.getItemsCount() ;
                    }
                    // fill all Records between <prevRec,nextRec>
                    //
                    Vector vec = cm.getVector(prevVer);
                    for (int rec = prevRec ; rec < nextRec ; rec++) {
                        stats.getItemAt(rec).add(vec);
                    }
                }
            }
        }
    }
}
