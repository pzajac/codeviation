/*
 * Interval.java
 *
 * Created on November 5, 2006, 9:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * From startPosition to endPosition in java file. 
 * @author pzajac
 */
public final class PositionInterval implements Serializable,Comparable<PositionInterval> {
    Position startPosition;
    Position endPosition;
    
    /** Creates a new instance of Interval */
    public PositionInterval(Position startPosition,Position endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj instanceof PositionInterval) {
            PositionInterval i2 = (PositionInterval)obj;
            
            if (startPosition.equals(i2.startPosition) && endPosition.equals(i2.endPosition)) {
                //compare all lines
                List<Line> lines1 = endPosition.getVersion().getLines(startPosition,endPosition);
                List<Line> lines2 = i2.endPosition.getVersion().getLines(i2.startPosition,i2.endPosition);
                retVal =  lines1.equals(lines2);
            }
        } 
        return retVal;
    }

    public Position getStartPosition() {
        return startPosition;
    }
    
    public Position getEndPosition() {
        return endPosition;
    }
    public int hashCode () {
        return startPosition.hashCode() + (endPosition.hashCode() >> 3);
    }    
    
    public String toString() {
        return getStartPosition() + " -> " + getEndPosition();
    }

    private static final class PosOffComparator implements Comparator<Position> {
        public int compare(Position p1, Position p2) {
            int res = p1.getVersion().compareTo(p2.getVersion());
            if (res == 0) {
                res = p2.getLine().getNewLineNumber() - p2.getLine().getNewLineNumber() ;
                if (res == 0) {
                    res = p2.getOffset() - p1.getOffset();
                    if (res == 0) {
                        res = p2.hashCode() - p1.hashCode();
                    }
                }
            }
            return res;
        }
        
    }
    
    /** ordering for testing purpose
     */
    public int compareTo(PositionInterval p1) {
       PosOffComparator comparator = new PosOffComparator();
       int res = comparator.compare(p1.getStartPosition(), getStartPosition());
       if (res == 0) {
           res = comparator.compare(p1.getEndPosition(),getEndPosition());
           if (res == 0) {
               return p1.hashCode() - hashCode();
           }
       }
       return res;
    }
    
    public static PositionInterval read (ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Position startPos = Position.read(ois);
        Position endPosition = Position.read(ois);
        return new PositionInterval(startPos,endPosition);
        
    }
    
    public void write (ObjectOutputStream oos) throws IOException {
        startPosition.write(oos);
        endPosition.write(oos);
    }
}
