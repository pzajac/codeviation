    
package org.codeviation.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Metric;
import org.codeviation.model.StaticMetric;

/**
 * Consists of PageRank history of usages for a JavaFile
 * @author pzajac
 */
public final class PageRankMetric implements Metric,StaticMetric,java.io.Serializable {
    public static final class Value {
        int position;
        double ratio;
        double rank;
        
        public Value(int position, double ratio, double rank) {
            this.position = position;
            this.ratio = ratio;
            this.rank = rank;
        }

        public double getRank() {
            return rank;
        }

        public double getRatio() {
            return ratio;
        }

        public int getPosition() {
            return position;
        }
        public String toString() {
            return position + ";" + ratio + ";" + rank;
        }
        private static Value parse(String line ) {
            StringTokenizer tokenizer = new StringTokenizer(line,";");
            int pos = Integer.parseInt(tokenizer.nextToken());
            double ratio = Double.parseDouble(tokenizer.nextToken());
            double rank = Double.parseDouble(tokenizer.nextToken());
            return new Value(pos,ratio,rank);
        }

    }
    private static final long serialVersionUID = 2;
    
    transient Map<String,Value> v2PageRank  ;
    transient JavaFile jf;
    
    /** Creates a new instance of PageRankMetric */
    public PageRankMetric() {
    }
    
    public String getName() {
        return "PageRankMetrics";
    }

    public String getDescription() {
        return "Google's PageRank metrics for versions";
    }

    public boolean isPersistent() {
        return true;
    }

    public void put(String tag,double rank,int position,double ratio) {
        if (v2PageRank == null) {
           v2PageRank =  new HashMap<String,Value>();
        }
        v2PageRank.put(tag,new Value(position,ratio,rank));
    }
    
    /** Cet page rank for cvs version
     * @param tag - cvs tag
     * @return -1 if if no rank was computed for the version 
     */
    public double get(String tag) {
        double rank = -1.0;
        Value value = getValue(tag);
        if (value != null) {
            rank = value.getRank();
        }
        return rank;
    }
    public Value getValue(String tag) {
        if (v2PageRank != null) {
            return  v2PageRank.get(tag);
        }
        return null;
    } 
    public Set<String> getTags() {
        if (v2PageRank != null) {
            return v2PageRank.keySet();
        } else {
            return java.util.Collections.emptySet();
        }
    }
    public boolean isValid() {
        return true;
    }
    private void writeObject(ObjectOutputStream oos ) throws IOException {
        if (v2PageRank != null) {
            StringBuilder hist = new StringBuilder();
            for (Map.Entry<String,Value> entry : v2PageRank.entrySet()) {
                hist.append(entry.getKey());
                hist.append('\n');
                hist.append(entry.getValue());
                hist.append('\n');
            }
            oos.writeObject(hist.toString());
        } else {
            oos.writeObject("");
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        v2PageRank = new HashMap<String,Value>();        
        String hist = (String)ois.readObject();
        BufferedReader reader = new BufferedReader(new StringReader(hist));
        while (true) {
            String tag = reader.readLine();
            if (tag == null) {
                break;
            }
            String value = reader.readLine();
            if (value == null) {
                break;
            }
            v2PageRank.put(tag, Value.parse(value));
        }
    }

    public void setJavaFile(JavaFile jf) {
       this.jf = jf;
    }

}
