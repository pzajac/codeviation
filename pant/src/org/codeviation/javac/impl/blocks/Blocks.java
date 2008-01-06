
package org.codeviation.javac.impl.blocks;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import org.codeviation.model.PositionInterval;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Position;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;


/**
 *
 * @author pzajac
 */
public class Blocks {
    final JavaFile javaFile;
    private PrintWriter writer;
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
        File logFile = new File(javaFile.getPackage().getCVSFile(),javaFile.getName() + ".blocks.log");
        boolean error = true;
        try {
            writer = new PrintWriter(new FileWriter(logFile));
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
            error = false;
        } catch (IOException ioe) {
           writer.close();
           try {
              logBlocks(logFile);
           } catch (IOException ioes) {
              logger.log(Level.SEVERE,ioe.getMessage(),ioes); 
           }
           logger.log(Level.SEVERE, logFile.getAbsolutePath(), ioe);
        } finally {
            writer.close();
            if (error) {
                logFile.delete();
            }
        }
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
        writer.println("BlockItem:" +interval.start + ":" + interval.end + ":" + value);
        if (debug) {
            logger.fine("BlockItem:" +interval.start + ":" + interval.end + ":" + value);
        }
        return  new PositionInterval(startPos,endPos);
    }

    private void logBlocks(File posFile) throws IOException {
       File blocksList = new File(javaFile.getPackage().getCVSFile(),javaFile.getName() + ".blocks.lst");
       PrintWriter log = null;
       String line = null;
       BufferedReader reader = null;
       try {
           reader = new BufferedReader(new FileReader(posFile));
           log = new PrintWriter(new FileWriter(blocksList));
            while ((line = reader.readLine()) != null) {
                String[] symbs = line.split(":");
                if (symbs.length > 3) {
                    int p1 = Integer.parseInt(symbs[1]);
                    int p2 = Integer.parseInt(symbs[2]);
                    Position pos1 = javaFile.getPosition(p1);
                    Position pos2 = javaFile.getPosition(p2);
                    CVSMetric cvsm = javaFile.getCVSResultMetric();
                    log.println(line);
                    log.println("--------------");
                    log.println(cvsm.getContent(pos1, pos2));
                    log.println();
                }
            }
       } finally {
           if (log != null) {
                log.close();
           }
           if (reader != null) {
                reader.close();
           }
       }
    }
}

