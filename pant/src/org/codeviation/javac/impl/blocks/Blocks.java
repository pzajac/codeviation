
package org.codeviation.javac.impl.blocks;
import java.util.HashMap;
import org.codeviation.model.PositionInterval;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
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

    // for unit tests
    static Logger logger = Logger.getLogger(Blocks.class.getName());
    static boolean debug = false;
    
    Map<Interval,BlocksItem> intervals = new HashMap<Interval,BlocksItem>();
    
    Map<Interval,String> classes = new HashMap<Blocks.Interval, String>();
    Map<Interval,String> methods = new HashMap<Blocks.Interval, String>();
    
        
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
       
        // store blocks
        //
        for (Entry<Interval,BlocksItem> entry : intervals.entrySet()) {
            PositionInterval pi = getPositionInterval(entry.getKey(),entry.getValue());
            PositionIntervalResult<BlocksItem> pir = new PositionIntervalResult<BlocksItem>(pi,entry.getValue());
            metric.addSrcVerObject(pir, v);
        }
        // store classes
        //
        for (Entry<Interval,String> entry : classes.entrySet()) {
            PositionInterval pi = getPositionInterval(entry.getKey(),entry.getValue());
            PositionIntervalResult<String> pir = new PositionIntervalResult<String>(pi,entry.getValue());
            metric.addClass(pir, v);
        }
        // store methods
        for (Entry<Interval,String> entry : methods.entrySet()) {
            PositionInterval pi = getPositionInterval(entry.getKey(),entry.getValue());
            PositionIntervalResult<String> pir = new PositionIntervalResult<String>(pi,entry.getValue());
            metric.addMethod(pir, v);
        }
        javaFile.setMetric(metric);
    }
  
    public  void addBlockItem(int startPos,int endPos, BlocksItem item) {
        Interval i = new Interval(startPos,endPos);
        if (startPos != -1 && endPos != -1 && !intervals.containsKey(i)) {
            intervals.put(i,item);
        }
    }    

    public void addClassName(int startPos,int endPos,String className) {
        Interval i = new Interval(startPos,endPos);
        if (startPos != -1 && endPos != -1 ) {
            classes.put(i,className);
        }
    }
    public void addMethodSignature(int startPos,int endPos,String className) {
        Interval i = new Interval(startPos,endPos);
        if (startPos != -1 && endPos != -1) {
            methods.put(i,className);
        }
    }
    /** just only for testing
     */
    public static void setDebug(boolean debug) {
        Blocks.debug = debug;
    }
  
    private <T> PositionInterval getPositionInterval(Interval interval,T value) {
        Position startPos = javaFile.getPosition(interval.start);
        Position endPos = javaFile.getPosition(interval.end);
        if (debug) {
            logger.fine("BlockItem:" +interval.start + ":" + interval.end + ":" + value);
        }
        return  new PositionInterval(startPos,endPos);
    }
}

