/*
 * Versions.java
 *
 * Created on November 5, 2006, 10:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.codeviation.model.Line;
import org.codeviation.model.Version;

/**
 *
 * @author pzajac
 */
public final class VersionInterval  {
     Version from;
     Version to;
    /** Creates a new instance of Versions */
    public VersionInterval(Version v1,Version v2) {
        if (v1 == null) {
            v1 = v2;
        }
        if (v2 == null) {
            v2 = v1;
        }
        if (v1 == null) {
            throw new IllegalArgumentException("At least one version must be not null");
        }
        if (v1.compareTo(v2) < 0) {
            this.from = v1;
            this.to = v2;
        } else {
            this.to = v2;
            this.from = v1;            
        }
    }
    public Version getFrom() {
        return from;
    }
    public Version getTo() {
        return to;
    }
    public boolean contains (Version version) {
        return from.compareTo(version) <= 0 && to.compareTo(version) >= 0;
    }
    
    public boolean contains (Date date) {
        return from.getDate().compareTo(date) <= 0 && to.getDate().compareTo(date) >= 0;
    }
    public List<Version> getAllVersions() {
        List<Version> ret = new ArrayList<Version>();
        Version v = from;
        if (from == to) {
            ret.add(from);
            return ret;
        }
        do {
          ret.add(v); 
          v = v.getNext();
        } while (v != null && v != to);
        if (v == null && to != from) {
            throw new IllegalStateException("Invalid versions sequence: " + from +  "->" + to);
        }
        return ret;
        
    }
    public void addVersion(Version v) {
       if (from.compareTo(v) > 0) {
           v = from;
       } else if (to.compareTo(v) < 0 ) {
            to = v;
       }
    }
    
    public static VersionInterval get(List<Line> lines) {
            if (lines.size() < 2) {
                throw new IndexOutOfBoundsException("lines.size() < 2");
            }  
            VersionInterval vers = new VersionInterval(lines.get(0).getPosition().getVersion(),lines.get(1).getPosition().getVersion());
            for (int i = 2 ; i < lines.size() ; i++) {
                vers.addVersion(lines.get(i).getPosition().getVersion());
            } 
            return vers;
    }    
    public  void write(ObjectOutputStream oos) throws IOException {
        from.writeRef(oos);
        to.writeRef(oos);
    } 
    public static VersionInterval read(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Version from = Version.readRef(ois);
        if (from.getJavaFile() == null) {
            throw new IllegalStateException("from " + from.getRevision());
        }
        Version to = Version.readRef(ois);
        if (to.getJavaFile() == null) {
            throw new IllegalStateException("to " + to.getRevision());
        }
        return new VersionInterval(from,to);
    }   
}
