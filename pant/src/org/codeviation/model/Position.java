/*
 * Position.java
 *
 * Created on November 5, 2006, 8:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import org.codeviation.model.Version;

/**
 *
 * @author pzajac
 */
public final class Position implements  Serializable {
    private static final long serialVersionUID = 1;
    /** offset in line
     */
    private int offset;
    private Line line;
    /** version of usage
     */
    private Version version;
    /** Creates a new instance of Position 
     * @param offset offset
     * @param version
     */
     public  Position(int offset,Version version,Line line) {
         this.offset = offset;
         this.version = version;
         this.line = line;
    }

     public Version getVersion() {
         return version;
     }
     public Line getLine() {
         return line;
     }
     
     /** equals and compareTo breaks contract !!!
      * Equals os 
      */
     public boolean equals(Object object) {
         if (object instanceof Position) {
            Position pos = (Position) object;
            return getOffset() == pos.getOffset() && getLine().equals(pos.getLine()); 
         }
         return false;
     }
     public int hashCode() {
         return getOffset() + line.hashCode();          
     }
     /** Get offset on the line 
      * 
      * @return offset on the line
      */
     public int getOffset() {
         return offset;
     }

     /** Get absolute position in file for revision v
      * 
      * @param v revision of abs pos
      * @return absolute position
      */
     public int getAbsolutePosition(Version v) {
        List<Line> lines = v.getJavaFile().getLines(v);
        boolean found = false;
        int absPos = 0;
         for (int i = 0; i < lines.size(); i++) {
             Line line = lines.get(i);
             if (line == this.line) {
                 found = true;
                 break;
             }
             absPos += line.getNewContent().length();
         }
        if (!found) {
            throw new IllegalStateException("No  line in Version " +  v + " for position " + this);
        }
        return absPos + getOffset();
     }
     public String toString() {
         String linePos = null;
         if (line != null && line.getPosition() != null) {
             Position p = line.getPosition();
             linePos = String.valueOf(p.getVersion() + ":" + p.getOffset());
         }
         return version + "(" + offset + ")" +"[" + linePos + "]";
     }
     private void writeObject(ObjectOutputStream  oos)  throws IOException {
         write(oos);
     }
     private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        offset = ois.readInt();
        version = Version.readRef(ois);
        line = Line.readRef(ois);        
     }

     public static Position read(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int offset = ois.readInt();
        Version version = Version.readRef(ois);
        Line line = Line.readRef(ois);        
        return new Position(offset,version,line);
     }
     
     public void write(ObjectOutputStream oos) throws IOException {
         oos.writeInt(offset);
         version.writeRef(oos);
         line.writeRef(oos);
     }
     /** It will replace version with a parameter if is of the same revision.
      */
     public void updateVersion(Version version1,Version version2) {
         if (version != null) {
             if (version.getRevision().equals(version1.getRevision())) {
                 version = version1;
             } else if (version.getRevision().equals(version1.getRevision())) {
                 version = version2;
             }
         }
     }
}
