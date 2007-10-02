/*
 * Line.java
 *
 * Created on June 26, 2003, 8:34 AM
 */

package org.codeviation.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.Diff;

/**
 *
 * @author  pz97949
 */
public final class Line {
    private int initialLineNumber;
    private int newLineNumber;
    private State state;
    private String initialContent;
    private String newContent;
    // position is computed after merging (patching) all changes
    
    private Position position;
    public enum State {
       REMOVED,
       CHANGED,       
       CREATED
    }
    public Version replaceVersion;
    
    /** @return version which replaces this line
     * Use before first usage 
     * CVSMetric.updateReplaceLineVersions()
     */
    public Version getReplaceVersion() {
       return replaceVersion; 
    }
    /** @param version which replaces this line
     */ 
    public void setReplaceVersion(Version version) {
       this.replaceVersion = version; 
    }
    /** Creates a new instance of Line */
    public Line(int initialLineNumber,int newLineNumber,String initialContent,String newContent,State state) {
        this.initialLineNumber = initialLineNumber;
        this.newLineNumber = newLineNumber;
        this.state = state;
        this.initialContent = (initialContent != null) ? initialContent.substring(2) :null;
        this.newContent = (newContent != null) ? newContent.substring(2) :null;
        if (state == State.CREATED &&  newContent == null ) {
            throw new IllegalArgumentException("STATE_CREATED and new content");
        }
        if (state == State.CREATED &&  newLineNumber == -1 ) {
            throw new IllegalArgumentException("STATE_CREATED and newLineNumber");
        }
    }
    private Line () {
        
    }
    public static Line createTrimedLine(int initialLineNumber, int newLineNumber,String initialContent,String newContent,State state) {
        Line line = new Line();
        line.initialLineNumber = initialLineNumber;
        line.newLineNumber = newLineNumber;
        line.state = state;
        line.newContent = newContent;
        line.initialContent = initialContent;
        if (line.state == State.CREATED &&  line.newContent == null ) {
            throw new IllegalArgumentException("STATE_CREATED and new content");
        }
        if (line.state == State.CREATED &&  line.newLineNumber == -1 ) {
            throw new IllegalArgumentException("STATE_CREATED and newLineNumber");
        }
        return line;
    }
    
    /** @return content of line from previous revision
     */
    public String getInitialContent() {
        return initialContent;
    }
    /** @return line content from new revision
     */
    public String getNewContent() {
        return newContent;
    }
    /** @return line number of from previous revision
     */
    public int getInitialLineNumber() {
        return initialLineNumber;
    }
    /** @return line number from new revision 
     */
    public int getNewLineNumber() {
        return newLineNumber;
    }
    /** @return STATE_CHANGED, STATE_REMOVED, or STATE_ADDED
     */
    public State getState() {
        return state;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = position;
    }
    
    public static Position getPosition(List<Line> lines,Version v,int offset) {
        int pointer = 0;
        for (Line line : lines) {
            if (offset < pointer + line.getNewContent().length()) {
                return new Position(offset - pointer + line.getPosition().getOffset(),v,line);
            }  
            pointer += line.getNewContent().length() + 1;
        }
        throw new IllegalStateException("offset = " + offset + " > " + pointer + "," + v + "," + 
       v.getJavaFile().getPackage().getSourceRoot() + "/" + v.getJavaFile().getPackage() + "." +  v.getJavaFile());
    }
    @Override
    public String toString() {
          return "(" + initialLineNumber + "," + newLineNumber + ")" + position + "\n" 
                  + initialContent + "\n" + newContent + "\n";
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        boolean ret = false;
        if (object instanceof Line ) {
            Line line = (Line ) object;
            ret = (state == line.state) &&
                     (newContent == null || newContent.equals(line.newContent)) &&
                     (initialContent == null || initialContent.equals(line.initialContent)) &&
                     (initialLineNumber == line.initialLineNumber) &&
                     (newLineNumber == line.newLineNumber);
        }
        return ret;
    }
    @Override
    public int hashCode() {
        int hashCode = newLineNumber + initialLineNumber<<15 ;
        if (newContent != null) {
            hashCode += newContent.hashCode();  
        } 
        if (initialContent != null) {
            hashCode += newContent.hashCode() << 3;              
        }
        hashCode += state.hashCode() << 13;
        return hashCode;
    } 
    /** write only reference
     */
    public void writeRef(ObjectOutputStream oos) throws IOException {
       oos.writeObject(position.getVersion().getRevision());
       oos.writeInt(getInitialLineNumber());
       oos.writeInt(getNewLineNumber());
    }
    
    public static Line readRef(ObjectInputStream ois) throws IOException, ClassNotFoundException {
       String revision = (String)ois.readObject();
       int initLn = ois.readInt();
       int newLn = ois.readInt();
       CVSMetric cvsRes = JavaFileUtil.getCurrentJavaFile().getMetric(CVSMetric.class);
       Version version = cvsRes.getRootVersion().getVersion(revision);
       if (version == null) {
           throw new IOException("Missing version : " + initLn + "," + newLn + "," + revision + "," + cvsRes.getJavaFile());
       }
       Diff diff = version.getDiff(); 
       if (diff == null) {
           throw new IOException("NUll diff : " + initLn + "," + newLn + "," + revision + "," + cvsRes.getJavaFile());           
       }
       return diff.getLine(initLn,newLn);
    }
}
