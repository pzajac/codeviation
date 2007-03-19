package org.codeviation.javac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Metric;
import org.codeviation.model.StaticMetric;
import org.codeviation.model.Version;

/**
 * Contains  revisions processed by pant.
 * @author Petr Zajac
 */
public class CVSVersionsByPant implements Metric,StaticMetric, Serializable {
    private static final long serialVersionUID = 1;
    
    transient JavaFile jf;
    transient List<String> revisions = new ArrayList<String>();
    transient List<String> tags = new ArrayList<String>();
    transient Version cachedVersion;
    transient String cachedRevision;
    transient String cachedTag;
    /** Creates a new instance of CVSVersionsByPant */
    public CVSVersionsByPant() {
    }
    
    public String getName() {
        return "CVSVersionsProcessedByPant";
    }

    public String getDescription() {
        return "Contains all cvs revisions processed by pant";
    }

    public boolean isPersistent() {
        return true;
    }

    public boolean isValid() {
        return jf != null;
    }
    public boolean containsRevision(String revision) {
        if (revision != null) {
            for (int i = 0 ; i < revisions.size() ; i++ )  {
                if (revision.equals(revisions.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean containsTag(String tag) {
        if (tag != null) {
            for (int i = 0 ; i < tags.size() ; i++ )  {
                if (tag.equals(tags.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public void addRevision(String revision,String tag) {
        revisions.add(revision);
        tags.add(tag);
    }
    
    public void setJavaFile(JavaFile javaFile) {
        this.jf = javaFile;
    }

    public Version getVersion(String tag) {
        if (tag == null) {
            throw new NullPointerException("Tag cannot be null");
        }
        int revIndex = -1;
        for (int i = 0 ; i < tags.size() ; i++) {
            String tmpTag = tags.get(i);
            if (tag.equals(tmpTag)) {
                revIndex = i;
                break;
            }
        }
        if (revIndex != -1) {
            if (!tag.equals(cachedTag)) {
                cachedTag = tag;
                String revision =  revisions.get(revIndex);
                cachedVersion = jf.getCVSResultMetric().getRootVersion().getVersion(revision);
            }
            return cachedVersion;
        } else {
            return null;
        }
        
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < revisions.size() ; i++) {
            
            sb.append(revisions.get(i));
            String tag = tags.get(i);
            if (tag != null) {
                sb.append(",");
                sb.append(tag);
            }
            sb.append('\n');
        }
        oos.writeObject(sb.toString());
    }
    
    public Set<String> getTags() {
      Set<String> retTags = new HashSet<String>();
      for (String tag : tags) {
          if (tag != null) {
              retTags.add(tag);
          }
      }
      return retTags;
    }
    
    public Set<String> getRevisions() {
        return new HashSet<String>(revisions);
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader reader = new BufferedReader(new StringReader((String)ois.readObject()));
        String line = null;
        revisions = new ArrayList<String>();
        tags = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            int colon = line.indexOf(',');
            if (colon == -1 ) {
                revisions.add(line);
                tags.add(null);
            } else {
                String rev = line.substring(0,colon);
                String tag = line.substring(colon + 1,line.length());
                revisions.add(rev);
                tags.add(tag);
            }
        }
    }
}
