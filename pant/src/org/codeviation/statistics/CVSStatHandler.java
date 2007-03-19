
package org.codeviation.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.Diff;
import org.codeviation.model.vcs.Diff.LnState;

/**
 *
 * @author pzajac
 */
public final class CVSStatHandler implements JavaFileHandler<Statistics>, ChartConfProvider{
    Statistics stats;
    
    /** bugs -> record mapping
     */
    private Map<Integer,Record> bugs = new HashMap<Integer,Record>(); 
    
    private double absBugsCount;
    private double absBugsLineCount;
    private double absOtherLineCount;
    
    
    public void init(Statistics stats) {
        this.stats = stats;
        bugs.clear();
    }
   
    public void initGraphPaint(ChartConf conf) {
        absBugsCount = 0;
        absOtherLineCount = 0;
        absBugsLineCount = 0;
    }
    public void addJavaFile(JavaFile jf) {
      CVSMetric metric = jf.getCVSResultMetric();
        if (metric != null) {
            Diff diffs[]  = metric.getAllDiffs();
            for (Diff diff : diffs) {
               Version ver = diff.getVersion2();
               int issues = 0;
               for (int issue : ver.getDefectNumbers()) {
                   if (bugs.get(issue) == null) {
                       bugs.put(issue,stats.getItem(ver.getDate()));
                       issues++;
                   };
               }
               Vector vec = diff.getVector();
               vec.add(Diff.LnState.BUG_COUNT.getIndex(), issues);
               vec.add(LnState.BUG_COUNT.getIndex(), (double)issues);
               Record rec = stats.getItem(ver);
               if (rec != null ) {
                  rec.add(vec);
               }
            }
        }       
    }

    public float getValue(Record rec, RecordType type) {
        float buggyLines = (float) rec.getValue(LnState.ADDED_BUG.getIndex()) + 
                rec.getValue(LnState.CHANGED_BUG.getIndex()) +
                rec.getValue(LnState.REMOVED_BUG.getIndex());
        float otherLines = (float) rec.getValue(LnState.OTHER_ADDED.getIndex()) + 
                rec.getValue(LnState.OTHER_CHANGED.getIndex()) +
                rec.getValue(LnState.OTHER_REMOVED.getIndex());
        
        float retVal = 0;
        absBugsLineCount += buggyLines;
        absOtherLineCount += otherLines;
        double bugsCount = rec.getValue(LnState.BUG_COUNT.getIndex()); 
        absBugsCount += bugsCount;
        if (CVSMetric.BUGS_COUNT_TYPE.equals(type)) {
            retVal = rec.getValue(LnState.BUG_COUNT.getIndex());
        } else if (CVSMetric.BUGS_LINE_DENSITY.equals(type)) {
            float sum = (float) (absBugsLineCount + absOtherLineCount);
            if (sum > 0 ) {
                retVal = (float) (absBugsLineCount / sum);
            }
        } else if (CVSMetric.BUG_LINES.equals(type)) {
            retVal = buggyLines;
        } else if (CVSMetric.NON_BUG_LINES.equals(type)) {
            retVal = otherLines;
        } else {
            throw new IllegalStateException ("Invalid type " + type);
        }
        return retVal;
    }
    public  ChartConf[] getChartConfs() {
        return new ChartConf[] {
            new ChartConf("Bugs Count", 
                "Date", "Count", "Bugs Count for history",
                Arrays.asList(CVSMetric.BUGS_COUNT_TYPE), 
                new CVSStatHandler()),
            new ChartConf("Bugs  density per line ", 
                "Date", "bug/non bug lines", "Ration of bug fix and normal commit",
                Arrays.asList(CVSMetric.BUGS_LINE_DENSITY), 
                new CVSStatHandler()),
            new ChartConf("Lines History", 
                "Date", "Value", "Lines history",
                Arrays.asList(CVSMetric.BUG_LINES,
                    CVSMetric.NON_BUG_LINES), 
                new CVSStatHandler()),
                
        };
    }
}
