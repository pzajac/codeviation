/*
 * UsagesResult.java
 *
 * Created on October 30, 2006, 11:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.javac;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.VersionInterval;
import org.codeviation.model.VersionedMetric;

/**
 *
 * @author pzajac
 */
public class UsagesMetric extends VersionedMetric<UsageItem> implements  java.io.Serializable {
    private static final long SERIAL_VERSION_UID = 1;
    
    /** Creates a new instance of UsagesResult */
    public UsagesMetric() {
    }
    
    public String getName() {
        return "Usages Result"; 
    }
    
    public String getDescription() {
        return "Usages for method calls";
    }
    
    public boolean isPersistent() {
        return true;
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int size = ois.readInt();
        for (int i = 0 ; i < size ; i++) {
            PositionInterval pi = PositionInterval.read(ois);
            UsageItem ui = UsageItem.read(ois);
            VersionInterval vi = VersionInterval.read(ois);
            PositionIntervalResult<UsageItem> pir = new PositionIntervalResult<UsageItem>(pi,ui);
           
            getStorage().add(pir,vi);
        }
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {        
        Set<PositionIntervalResult<UsageItem>> usages = getStorage().getAllObjects();
        oos.writeInt(usages.size());
        for (PositionIntervalResult<UsageItem> usage : usages) {
            usage.getInterval().write(oos);
            usage.getObject().write(oos);
            VersionInterval vi = getStorage().get(usage);
            vi.write(oos);
        }        
    }
    
    public boolean isValid() {
        return true;
    }
}
