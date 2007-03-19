
package org.codeviation.javac.impl.blocks;
import java.util.HashMap;
import org.codeviation.model.PositionInterval;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Position;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.Version;


/**
 *
 * @author pzajac
 */
public class Blocks {
    JavaFile javaFile;
    String className;

    Map<Interval,BlocksItem> intervals = new HashMap<Interval,BlocksItem>();
        
    private static final class Interval {
        int start;
        int end;
        
        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Interval other = (Interval) obj;

            if (this.start != other.start)
                return false;
            if (this.end != other.end)
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 3;

            hash = 97 * hash + this.start;
            hash = 97 * hash + this.end;
            return hash;
        }

    }
    public Blocks(JavaFile javaFile,String className) {
        this.javaFile = javaFile;
        this.className = className;
    }
    public void persists() {
        Version v = javaFile.getCVSVersion();
        if (v == null) {
            return;
        }
        BlocksMetric metric = javaFile.getMetric(BlocksMetric.class);
        if (metric == null) {
            metric = new BlocksMetric();
        }
       
        for (Entry<Interval,BlocksItem> entry : intervals.entrySet()) {
            Interval interval = entry.getKey();
            Position startPos = javaFile.getPosition(interval.start);
            Position endPos = javaFile.getPosition(interval.end ) ;
            PositionInterval pi = new PositionInterval(startPos,endPos);
            PositionIntervalResult<BlocksItem> pir = new PositionIntervalResult<BlocksItem>(pi,entry.getValue());
            metric.addSrcVerObject(pir, v);
        }
        javaFile.setMetric(metric);
    }
  
    public  void addBlockItem(int startPos,int endPos, BlocksItem item) {
        Interval i = new Interval(startPos,endPos);
        if (startPos != -1 && endPos != -1 && !intervals.containsKey(i)) {
            intervals.put(i,item);
            System.out.println("add" + item + ":" + startPos + "," + endPos );
        }
    }    
}

