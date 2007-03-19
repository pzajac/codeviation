
package org.codeviation.javac;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.codeviation.model.Metric;
import org.codeviation.model.Version;

/**
 * Counts for all compiled versions (Version -> CountsItem)
 * @author pzajac
 */
public class CountsMetric implements Metric,java.io.Serializable {
    private static final long serialVersionUID = 1;
    transient Map<Version,CountsItem> v2Counts = new HashMap<Version,CountsItem>();
    
    public String getName() {
        return "Counts Metric";
    }

    public String getDescription() {
        return "Conts Metric";
    }

    public boolean isPersistent() {
        return true;
    }
    public CountsItem get(Version version) {
        return v2Counts.get(version);
    } 
    
    public void put(Version version,CountsItem item) {
        v2Counts.put(version, item);
    }
    Set<Map.Entry<Version,CountsItem>> getEntries() {
        return v2Counts.entrySet();
    }
    public Set<Version> getVersions() {
        return v2Counts.keySet();
    }
    public boolean isValid() {
        return true;
    }
    
    private void writeObject(ObjectOutputStream oos ) throws IOException {
        oos.writeInt(v2Counts.size());
        for (Map.Entry<Version,CountsItem> entry : v2Counts.entrySet()) {
            entry.getKey().writeRef(oos);
            entry.getValue().writeRef(oos);
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        v2Counts = new HashMap<Version,CountsItem>();        
        int size = ois.readInt();
        for (int i = 0 ; i < size ; i++) {
            Version v = Version.readRef(ois);
            CountsItem ci = CountsItem.readRef(ois);
            v2Counts.put(v,ci);
        } 
    }
    
}
