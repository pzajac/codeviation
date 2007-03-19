/*
 * CVSResultMetric.java
 *
 * Created on November 4, 2006, 11:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model.vcs;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.JavaFile;
import org.codeviation.model.JavaFileUtil;
import org.codeviation.model.Line;
import org.codeviation.model.StaticMetric;
import org.codeviation.model.Position;
import org.codeviation.model.Version;
import org.codeviation.statistics.RecordType;

/**
 * Contains all cvs information about a Java file
 * @author pzajac
 */
public final class CVSMetric implements StaticMetric,java.io.Serializable {
    private static final long serialVersionUID = 1;
    
    Diff diffs[] ;
    Version rootVersion;
    /** don't permit to access to cvs repository on false. Better for performance of visualization
     * ant testing 
     */
    static boolean updateCVS = true;
    transient JavaFile javaFile;


    public static final RecordType BUGS_COUNT_TYPE = new RecordType("Bug Count",0,true);
    public static final RecordType BUGS_LINE_DENSITY = new RecordType("Bug Lines Density",1,false);
    public static final RecordType BUG_LINES =  new RecordType("Buggy Lines",2,true);
    public static final RecordType NON_BUG_LINES =  new RecordType("Non Buggy Lines",2,true);
    
    /** useful for testing
     */
    public void setAllDiffs(Diff diffs[]) {
        this.diffs = diffs;
    }
    public void setJavaFile(JavaFile javaFile) {
        this.javaFile = javaFile;
        if (rootVersion != null) {
            rootVersion.setJavaFile(javaFile);
        }
    }
    
    public JavaFile getJavaFile() {
        return javaFile;
    }
    public void setRootVersion(Version version) {
        rootVersion = version;
        if (javaFile != null) {
            rootVersion.setJavaFile(javaFile);
        }
    }
    
    public Diff [] getAllDiffs() {
        if (updateCVS && (diffs == null || diffs.length == 0 || !rootVersion.contains(javaFile.getCVSVersionName()))) {
            try {
                Logger.getLogger(getClass().getName()).log(Level.FINE, "getAllDiffs() Start:" + javaFile.getCVSPath());
                CvsUtil util = javaFile.getPackage().getCvsUtil();
                rootVersion = null;
                rootVersion = getRootVersion();
                diffs =  util.getAllDiffs(javaFile.getCVSPath(),rootVersion,diffs);
                fixPositions();
                javaFile.setMetric(this);
                Logger.getLogger(getClass().getName()).log(Level.FINE, "getAllDiffs() End:" + javaFile.getCVSPath());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(CVSMetric.class.getName()).log(java.util.logging.Level.SEVERE,
                                                                 ex.getMessage(),
                                                                 ex);
                diffs = new Diff[0];
            } 
        } 
        return (diffs != null) ? diffs : new Diff[0];
    }
    private void fixPositions() {                 
       // reconstruct all verfsions and assing correct position
        List<Line> lines = new ArrayList<Line>(500); 
        for (Diff d : diffs) {
            lines = d.patch(lines);
            d.fixPositions(lines);
        }
    } 
                        
    /** @return version 1.0 and its siblings and branches 
     * 
     */
    public  Version getRootVersion() {
        if (rootVersion == null) {
            CvsUtil cvsUtil = javaFile.getPackage().getCvsUtil();          
            try {
                Logger.getLogger(getClass().getName()).log(Level.FINE, "getRootVersion() Start:" + javaFile.getCVSPath());
                setRootVersion(cvsUtil.getRootVersinTree(javaFile.getName()));
                Logger.getLogger(getClass().getName()).log(Level.FINE, "getRootVersion() End:" + javaFile.getCVSPath());
                // store it permamently
                javaFile.setMetric(this);
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        }
        return rootVersion;
    }
    
    public String getContent(Position startPos,Position endPos) {
       Version version = startPos.getVersion();
       if (startPos.getVersion().compareTo(endPos.getVersion()) > 0) {
           version = endPos.getVersion();
       }     
       List<Line> lines = javaFile.getLines(version);
       int index = lines.indexOf(startPos.getLine());
       if (index != -1 ) {
           StringBuilder out = new StringBuilder();
           Line startLine = startPos.getLine();
           int startOffset = startPos.getOffset() - startLine.getPosition().getOffset();
           out.append(startLine.getNewContent().substring(startOffset));
           for (; index < lines.size() ; index++) {
               Line line = lines.get(index);
               if (line == endPos.getLine()) {
                   int endOffset = endPos.getOffset() - line.getPosition().getOffset();
                   String content = line.getNewContent();
                   if (line == startLine) {
                       return content.substring(startOffset,endOffset);
                   } else {
                       if (out.length() > 0) {
                            out.append("\n");
                       }    
                       out.append(content.substring(0,endOffset));
                   }
                   return out.toString();
               } else if (line != startLine) {
                   if (out.length() > 0) {
                        out.append("\n");
                   }    
                   out.append(line.getNewContent());
               }
           }
       }
       throw new IllegalStateException(javaFile  + " getContent(" + startPos + "," + endPos + "failed ") ;
    }    
    public String getName() {
        return "Versioned Result";
    }
    
    public String getDescription() {
        return "Versioned Result";
    }
    
    public boolean isPersistent() {
        // don't persist empty CVSMetric
        //
        return rootVersion != null;
    }

    // XXX hashcode
    public boolean equals(Object o2) {
        boolean ret = false;
        if (o2 instanceof CVSMetric) {
            CVSMetric cvs = (CVSMetric)o2;
            ret = (javaFile == null || javaFile.equals(cvs.javaFile)) &&
                  (rootVersion == null || rootVersion.equals(cvs.rootVersion));
            if (ret && diffs != null) {
                ret = cvs.diffs != null &&  diffs.length == cvs.diffs.length;
                if (ret ) {
                    for (int i = 0 ; ret && i < diffs.length ; i++) {
                        ret = diffs[i].equals(cvs.diffs[i]);
                    }
                }
            }
        }
        return ret;
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(rootVersion);
        if (diffs != null) {
            oos.writeInt(diffs.length);
            for (Diff diff : diffs) {
                diff.write(oos);
                oos.flush();
            }
        } else {
            oos.writeInt(0);
        }
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        javaFile = JavaFileUtil.getCurrentJavaFile();
        javaFile.setCVSMetric(this);
        Version rootVersion = (Version) ois.readObject();
        if (rootVersion != null) {
            // XXX check new version
            setRootVersion(rootVersion);
        }
        int diffCt = ois.readInt();
        diffs = new Diff[diffCt];
        Version v1 = rootVersion;
        Version v2 = null;
        for (int i  = 0; i < diffCt ; i++) {
            if (v2 != null) {
                v1 = v2;
            }
            v2 = v1.getNext();
            diffs[i] = new Diff();
            diffs[i].read(v1, v2, ois);
        }
        if (diffs != null) {
            fixPositions();
        }
    }
    
    public static void setUpdateCVS(boolean updateCVS) {
        CVSMetric.updateCVS = updateCVS;
    }
    
     public static boolean getUpdateCVS() {
         return updateCVS;
     }

    public boolean isValid() {
        boolean bValid = false;
        File file = javaFile.getCVSFile();
        // XXX check if file is binary
        if (file != null) {
            bValid = new File(file.getParent(),"CVS").isDirectory();
        }
        return bValid;
    }
    
    /** Get revision for specific date in trunk
     *  @return null if no revision was found
     */
    public Version getVersion(Date date) {
        Version v = getRootVersion();
        Version candidateVersion = null;
        while (v != null) {
            if (date.compareTo(v.getDate()) >= 0 && (candidateVersion == null 
                    || v.getDate().compareTo(candidateVersion.getDate()) > 0)) {
                candidateVersion = v;
            }
            v = v.getNext();
        }
        return candidateVersion;
    }
   
    
    /** Update Line.replaceVersion items
     */
    public void updateReplaceLineVersions() {
        Diff diffs[] = getAllDiffs();
        if (diffs.length > 0) {
            javaFile.getLines(diffs[diffs.length - 1].getVersion2());
        }
    }
}
