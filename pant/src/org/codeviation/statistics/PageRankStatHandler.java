
package org.codeviation.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.tasks.PageRankMetric;


/**
 * Statistics Handler for PageRankMetric
 * @author pzajac
 */
public final class PageRankStatHandler implements JavaFileHandler<Statistics<String>>,ChartConfProvider<Statistics<String>>{
    private Statistics<String> stats;
    private boolean initialized;
    private ChartConf conf;
    
    private static int RANK = 0;
    private static int MIN_POS = 1;
    private static int MAX_POS = 2;
    private static int SUM_POS = 3;
    private static int CT_POS = 4;
    private static int VALUES_SIZE = 5;    
    static RecordType  PAGE_RANK_RECORD_TYPE = new RecordType("Page Rank - value",0,false);
    static RecordType  PAGE_RANK_MIN_POSITION = new RecordType("Page Rank - min position",1,false);
    static RecordType  PAGE_RANK_MAX_POSITION = new RecordType("Page Rank - max position",2,false);
    static RecordType  PAGE_RANK_AVG_POSITION = new RecordType("Page Rank - avg position",3,false);

    public void init(Statistics<String> stats) {
        this.stats = stats;
        initialized = false;
    }
    public void initGraphPaint(ChartConf conf) {
        this.conf = conf;
    }

    public void addJavaFile( JavaFile jf) {
        if (!initialized) {
            SourceRoot srcRoot = jf.getPackage().getSourceRoot();
            Repository rep = srcRoot.getRepository();
            List<String> tags = new ArrayList<String> (srcRoot.getCvsTags());
            Collections.sort(tags);
            Date min = null;
            Date max = null;
            if (tags.size() > 1) {
                min = rep.getTagDate(tags.get(0));
                max = rep.getTagDate(tags.get(tags.size() - 1));
            }
            stats.initKeys(tags, min, max);
            initialized = true;
        }
        PageRankMetric prm = jf.getMetric(PageRankMetric.class);
        if (prm != null ){
            
            Set<String> tags = prm.getTags();
            for (String tag : tags) {
                PageRankMetric.Value value = prm.getValue(tag);
                if (value != null) {
                   Record rec = stats.getItemForKey(tag);
                   Vector values = rec.getValues();
                   if (values == null) {
                       values = new DenseVector(VALUES_SIZE);
                   }
                   double minPos = rec.getValue(MIN_POS);
                   double maxPos = rec.getValue(MAX_POS);
                   double pos = value.getPosition();
                   if (pos < values.get(MIN_POS) || values.get(MIN_POS) == 0) {
                      values.set(MIN_POS, pos);
                   } 
                   if (pos > values.get(MAX_POS) ) {
                       values.set(MAX_POS,pos);
                   }
                   values.set(RANK,values.get(RANK) + value.getRank());
                   values.set(CT_POS,values.get(CT_POS) + 1);
                   values.set(SUM_POS,values.get(SUM_POS) + pos);
                   rec.setValues(values);
                }
            }
        }        
    }

    public float getValue(Record rec, RecordType type) {
        float ret = 0;
        if (type == PageRankStatHandler.PAGE_RANK_RECORD_TYPE) {
            ret = rec.getValue(RANK);
        } else if (type == PageRankStatHandler.PAGE_RANK_MAX_POSITION) {
            ret = rec.getValue(MAX_POS);
        } else if (type == PageRankStatHandler.PAGE_RANK_MIN_POSITION) {
            ret = rec.getValue(MIN_POS);
        } else if (type == PageRankStatHandler.PAGE_RANK_AVG_POSITION) {
            float ct = rec.getValue(CT_POS);
            if (ct > 0) {
                ret = rec.getValue(SUM_POS)/ct;
            }
        }
        return ret;
    }
    
    public ChartConf[] getChartConfs() {
        return new ChartConf[] {new ChartConf<Statistics<String>>("PageRank - value", 
                "Date", "Rank", "PageRank history",
                Arrays.asList(PAGE_RANK_RECORD_TYPE), new PageRankStatHandler()),
            new ChartConf<Statistics<String>>("PageRank - position ", 
                "Date", "Value", "PageRank history",
                Arrays.asList(new RecordType[]{PAGE_RANK_MAX_POSITION,PAGE_RANK_MIN_POSITION,PAGE_RANK_AVG_POSITION}), new PageRankStatHandler())                
        };
    }
}
