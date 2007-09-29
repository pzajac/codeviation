/*
 * Diff.java
 *
 * Created on June 26, 2003, 8:32 AM
 */

package org.codeviation.model.vcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.Line;
import org.codeviation.model.Position;
import org.codeviation.model.Version;
import org.codeviation.model.Version.IssueType;

/**
 *
 * @author  pz97949
 */
public final class Diff  {
    /** type of items for getVector()
     */
    public static enum LnState {
           CHANGED_BUG(0),
           ADDED_BUG(1),
           REMOVED_BUG(2),
           OTHER_CHANGED(3),
           OTHER_REMOVED(4),
           OTHER_ADDED(5),
           CHANGED_NEW_BUG(6),
           ADDED_NEW_BUG(7),
           REMOVED_NEW_BUG(8),
           BUG_COUNT(9);
           
           int index;
           LnState(int index) {
               this.index = index;
           }
           public int getIndex() {
               return index;
           }
     }
    private static final long serialVersionUID = 1;

    /** initial version
     */
    Version version1;
    /** version after change
     */
    Version version2; 
    /** lines of diff
     */
    List<Line> lines = new ArrayList<Line>();
    
    /** default constructutor used for deserialzation
     */
    Diff() {
    }
    /** Creates a new instance of Diff 
     * @param diffResult result of 
     * cvs diff -c -r v1 -r2 file
     */
    public Diff(Version v1,Version v2,List<Line> lines) {
        version1 = v1;
        version2 = v2;
        this.lines.addAll(lines);
    }
    
    public Diff(String diffResult,Version v1,Version v2) throws IllegalStateException {
        try {
            version1 = v1;
            version2 = v2;
            if (diffResult.length() != 0) {
                BufferedReader reader = new BufferedReader(new StringReader(diffResult));
                String line = null;
                // diff -N 
                while ((line = reader.readLine()) != null && line.startsWith("diff ") == false) ;
                if (line == null || !line.startsWith("diff ")) {
                    throw new IllegalStateException(diffResult);
                }
                // --- file
                // *************
                List<String> addedLines = new ArrayList<String>(100);
                List<String> removedLines = new ArrayList<String>(100);
                while ((line = reader.readLine()) != null) {
                    // ignore string \\ No newline at end of file
                    if (line.length() == 0) {
                        continue;
                    }
                    char firstChar = line.charAt(0);
                    if (!Character.isDigit(firstChar)) {
                        continue;
                    }
                    // readline info
                    Info info = new Info(line);
                    // read removed lines
                    info.read(reader,removedLines,info.removedCount);
                    // read added lines
                    if (info.addedCount > 0 && info.removedCount > 0) {
                        // probably bug in diff
                        // see CVSMetricTest.testBaseOption()
                        while ((line = reader.readLine()) != null && !line.equals("---")); 
                        if (line != null && line.startsWith("\\")) {
                            reader.readLine();
                        }
                    }
                    info.read(reader,addedLines,info.addedCount);
                    // diffBlocks
                    diffBlocks(info,removedLines,addedLines);
               }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalStateException("IOException :" + ioe.getMessage());
        }
    }

    private void diffBlocks (Info info,List<String> removedLines,List<String> addedLines) throws IllegalStateException {
        int changedCount = Math.min(info.removedCount,info.addedCount);
        int i = 0;
        for (; i < changedCount ; i++) {
           lines.add(new Line(info.removedOffset + i ,info.addedOffset + i,removedLines.get(i),addedLines.get(i),Line.State.CHANGED));
        }
        for (int r = i; r < info.removedCount ; r++) {
           lines.add(new Line(info.removedOffset + r ,-1,removedLines.get(r),null,Line.State.REMOVED));
        }
        for (int a = i; a < info.addedCount ; a++) {
           lines.add(new Line(-1,info.addedOffset + a,null,addedLines.get(a),Line.State.CREATED));
        }
    } //diffBlocks
    
    public void log (PrintStream ps) {
        for (Iterator it = lines.iterator(); it.hasNext() ; ) {
            Line line = (Line) it.next();
            ps.print(line.getInitialLineNumber() + ", " + line.getNewLineNumber() + ", " );
            switch (line.getState()) {
                case CHANGED:
                    ps.println("changed");
                    break;
                case CREATED:
                    ps.println("created");
                    break;
                case REMOVED:
                    ps.println("removed");
                    break;
            }
            ps.println(line.getInitialContent());
            ps.println(line.getNewContent());
        }
    }
    /** @return first revision of diff
     */
    public Version getVersion1() {
        return version1;
    }
    
    /** @return second revision of diff
     */
    public Version getVersion2() {
        return version2;
    }
    /** get number of changed lines 
     */
    public int getChangedLines() {
        int number = 0;
        for (int i = 0 ; i < lines.size() ; i++ ) {
             if (((Line)lines.get(i)).getState() == Line.State.CHANGED) {
                 number++;
             }
        }
        return number;
    }
    
    /** get number of added lines
     */
    public int getAddedLines() {
        int number = 0;
        for (int i = 0 ; i < lines.size() ; i++ ) {
             if (lines.get(i).getState() == Line.State.CREATED) {
                 number++;
             }
        }
        return number;
    }
    /** get number of removed lines 
     */
    public int getRemovedLines() {
        int number = 0;
        for (int i = 0 ; i < lines.size() ; i++ ) {
             if (lines.get(i).getState() == Line.State.REMOVED) {
                 number++;
             }
        }
        return number;
    }
    
    
    public List<Line> getLines() {
        return lines;
    }
    
    /** patche of two versions
     */
    public List<Line> patch(List<Line> previousLines) {
        int i1 = 0,i2 = 0,iRes = 0;
        List<Line> lines1 = previousLines;
        List<Line> lines2 = lines;
        if (lines.isEmpty()) {
            return new ArrayList<Line>(previousLines);
        }
        List<Line> results = new ArrayList<Line>(lines1.size() + lines2.size());
        while (i1 < lines1.size() || i2 < lines2.size()) {
            Line l2 = lines.get(i2);
            // new line 
            if (l2.getState() == Line.State.CREATED) {
                while ( i1 < lines1.size() && results.size() + 1 < l2.getNewLineNumber()) {
                    results.add(lines1.get(i1++));
                }
                results.add(l2);
                i2++;
            // change line 
            } else if (l2.getState() == Line.State.CHANGED) {
                while ( i1 < lines1.size() && results.size() + 1 < l2.getNewLineNumber()) {
                    results.add(lines1.get(i1++));
                }
                results.add(l2);
                if (i1 < lines1.size()) {
                    lines1.get(i1++).setReplaceVersion(version2);
                }
                i2++;
            } else if (l2.getState() == Line.State.REMOVED) {
                while ( i1 < lines1.size() && i1 + 1 < l2.getInitialLineNumber()) {
                    results.add(lines1.get(i1++));
                }
                if (i1 < lines1.size()) {
                    lines1.get(i1++).setReplaceVersion(version2);
                }
                i2++;
            }
            if (i2 == lines2.size()) {
                while ( i1 < lines1.size()) {
                    results.add(lines1.get(i1++));
                }
                break;
            }
        }        
        return results;
    }
    
    /** set positions to all lines with null positions
     */
    void fixPositions(List<Line> lines) {
       int offset = 0;
       for (Line line : lines) {
           if (line.getState() != Line.State.REMOVED) {
               if (line.getPosition() == null) {
                   line.setPosition(new Position(offset,getVersion2(),line));
               }
               offset += line.getNewContent().length() - 1; 
           }
       } 
    }   
    
    public Line getLine(int initialLine,int newLine) {
       for (Line l : lines) {
           if (l.getInitialLineNumber() == initialLine && l.getNewLineNumber() == newLine) {
               return l;
           } 
       }
       throw new IllegalStateException("No line found " + getVersion2().getRevision() + "," + initialLine + "," + newLine);
    }
    void updateVersions (Version version1,Version version2) {
        this.version1 = version1;
        this.version2 = version2;
        for (Line l : lines) {
            if (l.getPosition() != null) {
                l.getPosition().updateVersion(version1,version2);
            }
        }
    }
    private static final byte CHANGED          = 1;
    private static final byte CHANGED_NO_INDEX = 2;
    private static final byte REMOVED          = 3;
    private static final byte REMOVED_NO_INDEX = 4;
    private static final byte ADDED            = 5;
    private static final byte ADDED_NO_INDEX   = 6;
    
    /** read from output Stream
     */
    void read(Version version1,Version version2,ObjectInputStream ois) throws IOException,ClassNotFoundException {
        this.version1 = version1;
        this.version2 = version2;
        if (version1 == null) {
            throw new NullPointerException();
        }
        if (version2 == null) {
            throw new NullPointerException();
        }
        int linesCount = ois.readInt();
        int indexes[] = new int[linesCount*2];
        byte types[] = new byte[linesCount];
        int iln = 0;
        int nln = 0;
        for (int i  = 0,indexesCt = 0 ; i <  linesCount ; i++) {
            byte type = ois.readByte();
            types[i] = type;
            
            switch (type) {
                case CHANGED:
                    iln = ois.readInt();
                    nln = ois.readInt();     
                case CHANGED_NO_INDEX:
                    indexes[indexesCt++] = iln++;
                    indexes[indexesCt++] = nln++;
                    break;
                case REMOVED:
                    iln = ois.readInt();
                case REMOVED_NO_INDEX:
                    indexes[indexesCt++] = iln++;
                    indexes[indexesCt++] = -1;
                    break;
                case ADDED:
                    nln = ois.readInt();
                case ADDED_NO_INDEX:
                    indexes[indexesCt++] = -1;
                    indexes[indexesCt++] = nln++;
                    break;
                    
            } // switch;
        }
            // readLines
            //
            lines = new ArrayList<Line>(linesCount);
            String allLines = (String) ois.readObject();
            tmpOffset = 0;
            int indexesCt = 0;
            for (int t : types) {
                  switch(t) {
                case CHANGED:
                case CHANGED_NO_INDEX:
                    lines.add(Line.createTrimedLine(indexes[indexesCt++],
                                       indexes[indexesCt++],
                                       nextToken(allLines),
                                       nextToken(allLines),
                                       Line.State.CHANGED));
                    break;
                case REMOVED:
                case REMOVED_NO_INDEX:
                    lines.add(Line.createTrimedLine(indexes[indexesCt++],
                                       indexes[indexesCt++],
                                       nextToken(allLines),
                                       null,
                                       Line.State.REMOVED));
                    break;
                case ADDED:
                case ADDED_NO_INDEX:
                    String lineStr = nextToken(allLines);
                    lines.add(Line.createTrimedLine(indexes[indexesCt++],
                                       indexes[indexesCt++],
                                       null,
                                       lineStr,
                                       Line.State.CREATED));
                    break;
            } // switch
        }
    }

    /** @return next token or empty string
     */ 
    private static int tmpOffset;
    private static String nextToken(String diffContent) {
        if (tmpOffset >= diffContent.length()) {
            return "";
        }
        if (diffContent.charAt(tmpOffset) == '\n') {
            tmpOffset ++;
            return "";
        }
        int nextOffset = diffContent.indexOf('\n', tmpOffset);
        if (nextOffset == -1) {
            nextOffset = diffContent.length();
        }
        String line = diffContent.substring(tmpOffset,nextOffset);
        tmpOffset = nextOffset + 1;
        return  line; 
    }
    /** standard write takes a lot of space on disk
     */
    void write(ObjectOutputStream oos) throws IOException{
        oos.writeInt(lines.size());
        StringBuilder sb = new StringBuilder(10000);
        int initLineNumber = -1;
        int newLineNumber = -1; 
        int iln = 0,nln = 0;
        for (Line line : lines) {
            iln = line.getInitialLineNumber();
            nln = line.getNewLineNumber();
            switch(line.getState()) {
                case CHANGED:
                    sb.append(line.getInitialContent());
                    sb.append('\n');
                    sb.append(line.getNewContent());
                    sb.append('\n');
                    if (iln == (++initLineNumber) && nln == (++newLineNumber)) {
                        oos.writeByte(CHANGED_NO_INDEX);
                    } else {
                        initLineNumber = iln;
                        newLineNumber = nln;
                        oos.writeByte(CHANGED);
                        oos.writeInt(iln);
                        oos.writeInt(nln);
                    }
                    break;
                case CREATED:
                    sb.append(line.getNewContent());
                    sb.append('\n');
                    if ( nln == (++newLineNumber)) {
                        oos.writeByte(ADDED_NO_INDEX);
                    } else {
                        newLineNumber = nln;
                        oos.writeByte(ADDED);
                        oos.writeInt(nln);
                    }
                    break;
                case REMOVED:
                    sb.append(line.getInitialContent());
                    sb.append('\n');
                    if ( iln == (++initLineNumber)) {
                        oos.writeByte(REMOVED_NO_INDEX);
                    } else {
                        initLineNumber = iln;
                        oos.writeByte(REMOVED);
                        oos.writeInt(iln);
                    }
                    break;
            }
        }
            oos.writeObject(sb.toString());             
    }

    // XXX hashcode
    public boolean equals (Object o2) {
        boolean ret = false;
        if (o2 instanceof Diff) {
            Diff diff = (Diff)o2;
            ret = getVersion1().equals(diff.getVersion1()) &&
                  getVersion2().equals(diff.getVersion2()) &&
                  getLines().equals(diff.getLines());
        }   
        return ret;
    }
    
    
    /**
     * Get statistics vector
     * <ol> 
     *    <li> changed lines (bug)
     *    <li> added lines (bug)
     *    <li> removedLines (bug)
     *    <li> changed lines
     *    <li> removed lines
     *    <li> added lines
     *    <li> reserved for bug counts
     * </ol>
     * @return vector for statists
     * 
     */ 
    public Vector getVector() {
        // bugy lines, replaced lines, other lines
        double data[] = new double[LnState.values().length + 3];
        if (getVersion2().getIssueType() == IssueType.DEFECT) {
            data[LnState.CHANGED_BUG.getIndex()] = getChangedLines();
            data[LnState.ADDED_BUG.getIndex()] = getAddedLines();
            data[LnState.REMOVED_BUG.getIndex()] = getRemovedLines();
        } else {
           data[LnState.OTHER_CHANGED.getIndex()] = getChangedLines();
           data[LnState.OTHER_REMOVED.getIndex()] = getRemovedLines();
           data[LnState.OTHER_ADDED.getIndex()] = getAddedLines();
        }
        for (Line line : getLines()) {
            Version replaceVer = line.getReplaceVersion();
            if (replaceVer != null &&replaceVer.getDefectNumbers().length > 0) {
               switch (line.getState()) {
                  case CHANGED:
                data[LnState.CHANGED_NEW_BUG.getIndex()]++;
                    break;
                  case REMOVED:
                data[LnState.REMOVED_NEW_BUG.getIndex()]++;
                    break;
                  case CREATED:
                data[LnState.ADDED_NEW_BUG.getIndex()]++;
                    break;
                }
            }
        }
 
        return new DenseVector(data);

    }
    private static class Info {
        int addedOffset;
        int addedCount;
        int removedOffset;
        int removedCount;
        
        Info(String line) {
            String intervals [] = line.split("[acd]");
            boolean  aa = line.indexOf("a") != -1;
            boolean  dd = line.indexOf("d") != -1;
            boolean  cc = line.indexOf("c") != -1;
            if (intervals.length != 2) {
                throw new IllegalArgumentException("line: " + line );
            }
            // removed lines 
            
            String vals[] = intervals[0].split(",");
            removedOffset = Integer.parseInt(vals[0]);
            if (vals.length == 2) {
                removedCount = Integer.parseInt(vals[1]) - removedOffset ;
            }
            
            // added lines
            
            vals = intervals[1].split(",");
            addedOffset = Integer.parseInt(vals[0]);
            if (vals.length == 2) {
                addedCount = Integer.parseInt(vals[1]) - addedOffset;
            }
            if (aa || cc) {
                addedCount++;
            }
            if (dd || cc) {
                removedCount++;
            }
        }

        static void read (BufferedReader reader,List<String> lines,int count) throws IOException {
            String line = null;
            lines.clear();
            while (count-- > 0 && (line = reader.readLine()) != null ) {
                lines.add(line);
            }
        }
    } // class Info

}
