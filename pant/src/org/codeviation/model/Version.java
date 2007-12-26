/*
 * Version.java
 *
 * Created on June 23, 2003, 10:12 AM
 */

package org.codeviation.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.bugtracking.issuezilla.Activity;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.Diff;
import org.codeviation.bugtracking.issuezilla.Issue;

/**
 * It describes version of cvs revision of file. 
 * @author  pz97949
 */
public class Version implements Comparable<Version> , Serializable {
    private static final long serialVersionUID = 2;
    
    static Logger logger = Logger.getLogger(Version.class.getName());
    /** next version in this branch 
     */
    private Version nextVersion;
    /** all branches of this version 
     */
    private Version  branch ;
    /** revision number 
     */
    private String revision;
    /** comment  from log
     */
    private String comment;
    
    /** date of revision
     */
    private Date date;
    /** defects parsed from log 
     */
    private int defectNumbers[];
    
    private State state;
    transient private JavaFile javaFile;
    /** cvs user name
     */
    private String user;
    /** permitted difference between time of revision and time of bug
     */
    private static float bugCommitDiff = 1000*3600*24;
    
    public static enum IssueType {
        // not analyzed 
        UNKNOW,
        // just commit without issue
        NO_ISSUE,
        // defect
        DEFECT,
        ENHANCEMENT,
        FEATURE        
    }
    
    /** State from cvs log
     */
    public static enum State {
        DEAD,
        EXP;
        public static State parse(String value) {
            if (value.equals("dead")) {
                return State.DEAD;
            } else if (value.equals("Exp")) {
                return State.EXP;
            }
            throw new IllegalArgumentException("Illegal value : " + value);
        }
    }
    private IssueType issueType = IssueType.UNKNOW;
    
    /** Creates a new instance of Version 
     */
    public Version(String revString,String comment, Date date,String user,State state) {
         this.revision = revString ;
         this.comment = comment;
         this.date = date;
         this.user = user;
         this.state = state;
         if (date == null) {
             throw new NullPointerException("date");
         }
         if (user == null) {
             throw new NullPointerException("user");
         }
         if (state == null) {
             throw new NullPointerException("stat");
         }
         if (date == null) {
             throw new NullPointerException("date");
         }
         if (revString == null) {
             throw new NullPointerException("revString");
         }
    }
    
    public JavaFile getJavaFile() {
        return javaFile;
    }
    public void setJavaFile(JavaFile javaFile) {
        this.javaFile = javaFile;
        if (nextVersion != null) {
            nextVersion.setJavaFile(javaFile);
        }
        if (branch != null) {
            branch.setJavaFile(javaFile);
        }
    }
    public Date getDate () {
        // XXX fake
         if (date == null) {
             date = new Date();
         }
        return date;
    }
    public String getRevision() {
        return revision;
    }
    
    public String getComment() {
        return comment;
    }
    
    public String getUser() {
        return user;
    }
    /** creates tree hiearchy of versions
     * @return root of versions;  
     */
    public static Version sortVersions (List<Version> versions) throws IllegalArgumentException {

        Object verArray[] = versions.toArray();
        if (verArray.length == 0) {
            throw new IllegalArgumentException ("input collection is empty");
        }
        Arrays.sort(verArray);

        Version root = (Version)verArray[0];
        Version v1 = root;
        Stack<Version> stack = new Stack<Version>();
        for (int i = 1 ; i < verArray.length ; i++ ) {
             Version v2 = (Version) verArray[i];
//             int len1 = v1.getRevision().length();
//             int len2 = v2.getRevision().length(); 
               boolean theSameBranch = v1.getBranch().equals(v2.getBranch());
               
             if ( theSameBranch  ) {
                v1 = v1.nextVersion = v2;
             } else if (v1.getLevel() < v2.getLevel()){
                 stack.push(v1);
                 v1  = v1.branch = v2;
             } else {
                 v1 = stack.pop();
                 i--;
             }
        }
        return root;
    }
    
        
       
    private static String getBranch(String version) {
        return version.substring(0,version.lastIndexOf('.'));
        
    }
    /** get branch String 
     * example for 1.2.3 revisiuon return 1.2
     */
    public String getBranch() {
        return getBranch(revision);
    }
    
    public int getBranchVersion() {
        String r = getRevision();
        return Integer.parseInt(r.substring(r.lastIndexOf('.') + 1));
    }
    
    /** 
     * level of version 
     * 1.2.3 has level 2 
     */
    public int getLevel() {
        int counter = 0;
        int index = 0 ;
        while((index = revision.indexOf('.',index) + 1 )!= 0) {
            counter++ ;
        }
        return counter;
    }
    
    
    public Version getNext() {
        return nextVersion;
    } 
    
    public void setNext(Version v) {
        nextVersion = v;
    }
    public Version getMyBranch() {
        return branch;
    }
    
    public int compareTo(Version o) {
        StringTokenizer t1 = new StringTokenizer(getRevision(),".");
        StringTokenizer t2 = new StringTokenizer( o.getRevision(),"."); 
        while (t1.hasMoreElements() && t2.hasMoreElements()) {
            int value1 = Integer.parseInt(t1.nextToken().trim());
            int value2 = Integer.parseInt(t2.nextToken().trim());
            if (value1 < value2) {
               return -1;
            } else if (value1 > value2 ) {
               return 1;
            }
        }
        if (t1.hasMoreElements()) {
            return 1;
        } else if (t2.hasMoreElements()) {
            return -1;
        } else {
            return 0;
        }
    }
    
    /** tests if this commit has number of defect in comment
     * @throws IllegalStateException on SQLException from issuezilly     
     */ 
    public Version.IssueType getIssueType () throws IllegalStateException {
        Set<Integer> defectsList = new HashSet<Integer>(10);
        if (issueType == IssueType.UNKNOW) {
            List<Integer> issueIds = parseIssues();
            try {
                Collection<Issue> issues = Issue.readIssues(issueIds);
                issues = filterIssues(issues);
                for (Issue issue : issues) {
                       if (issue != null ) {
                           String typeString = issue.getIssueType();
                           if ("DEFECT".equals(typeString)) {
                               issueType = IssueType.DEFECT;
                               defectsList.add(issue.getIssueId());
                           } else if ("ENHANCEMENT".equals(typeString) && issueType != IssueType.DEFECT) {
                               issueType = IssueType.ENHANCEMENT;
                           } else if ("FEATURE".equals(typeString) && issueType != IssueType.DEFECT) {
                               issueType = IssueType.FEATURE;
                           }
                       }
                }
            } catch (Exception e) {
            // no bugtracking database available
                logger.log(Level.WARNING, e.getMessage(),e);
            }
            defectNumbers = new int[defectsList.size()];
            int i = 0;
            for (Integer issueId : defectsList) {
                defectNumbers[i++] = issueId;
            }
            if (issueType == IssueType.UNKNOW) {
                issueType = IssueType.NO_ISSUE;
            }
        }
        return issueType;
    }
    /** Sets IssueType to UNKNOWN value.
     */
    public void clearIssueType() {
       issueType = IssueType.UNKNOW;
    }
    /** Get all defects from log. 
     *  Return only number > 1000;
     *  @return array of issue_id  
     */
    public int [] getDefectNumbers() {
        getIssueType();
        return (defectNumbers == null) ? new int[0] : defectNumbers;
    }
    
    public List<Integer> parseIssues() {
        List<Integer> issues = new ArrayList<Integer>();
        // parse defects
        //
        String comment = getComment();
        StringBuffer number = new StringBuffer();
        boolean parseNumberState = false;
        for (int i = 0 ; i < comment.length() ; i++ ) {
            char ch = comment.charAt(i);
            if (parseNumberState == true) {
                // sequence of digits
                if (Character.isDigit(ch)) {
                    number.append(ch);
                }  else {
                    try {
                        Integer bugNumber = Integer.valueOf(number.toString());
                        if (bugNumber.intValue() > 1000) {
                            issues.add(bugNumber);
                        }
                    } catch (NumberFormatException e) {
//                        logger.log(Level.SEVERE, " should be number" , e);
                    }
                    number.setLength(0);
                    parseNumberState = false;
                }
            } else {
                // sequence of non digits characters
                if (Character.isDigit(ch)) {
                    number.append(ch);
                    parseNumberState = true;
                }
            }
        }// for
        if (number.length() > 0 ) {
            issues.add(Integer.valueOf(number.toString()));
        }
        return issues;
    }
   
    public void addAllVersions(Set <Version> vers) {
        vers.add(this);
        if (nextVersion != null) {
            nextVersion.addAllVersions(vers);
        }
        if (branch != null) {
            branch.addAllVersions(vers);
        }
    }
    
    public boolean contains(String v) {
        Set<Version> allVers = new HashSet<Version>();
        addAllVersions(allVers); 
        return allVers.contains(new Version(v,"",new Date(),"",State.EXP));
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Version ) && ((Version)obj).revision.equals(revision);
    }
    
    @Override
    public int hashCode() {
        return revision.hashCode();
    }
    
    public List<Line> getLines() {
        return javaFile.getLines(this);
    }
    public List<Line> getLines(Position startPos,Position endPos) {
        assert startPos.getVersion() == this;
        assert endPos.getVersion() == this;
        List<Line> lines = javaFile.getLines(this);
        List<Line> result = new ArrayList<Line>();
        
        Line line1 = null, line2 = null;
        int i1 = lines.indexOf(startPos.getLine());
        int i2 = lines.indexOf(endPos.getLine());
        if (i1 == - 1 || i2 == -1) {
            throw new IllegalStateException("Missing line, i1 = " + i1 + " i2 = " + i2);
        }
        if (i1 > i2) {
            throw new IllegalStateException(" i1 > i2 : " + i1 + " < " + i2);
        }                       
        for (; i1 <= i2  ; i1++) {
            result.add(lines.get(i1));
        }
        return result;
    }
    
    /** @return Version for revision vers
     */
    public Version getVersion(String vers) {
        if (getRevision().equals(vers)) {
            return this;
        }
        Version v = null;
        if (getNext() != null) {
            v = getNext().getVersion(vers);
        }
        if (v == null) {
            if (branch != null) {
                v = branch.getVersion(vers);
            }
        }
        return v;
    }
 
    /** @return filtered List of versions from interval minDate and maxDate
     * @param minDate - starting date
     * @param maxDate - ending date
     * @param versions - input list of versions
     */ 
    public static List<Version> filterVersions(List<Version> versions,Date minDate,Date maxDate) {
        List<Version> filteredVers = new ArrayList<Version>();
        for (Version v: versions) {
            if (minDate.compareTo(v.getDate()) <= 0 && maxDate.compareTo(v.getDate()) >= 0) {
                filteredVers.add(v);
            }
        }
        return filteredVers;
    }
    @Override
    public String toString() {
        return getRevision();
    }
    
    public Diff getDiff() {
        try {
            Diff[] diffs = getJavaFile().getDiffs();
            for (Diff diff : diffs) {
                if (equals(diff.getVersion2()) ) {
                    return diff;
                }
            }
        }  catch (IOException ex) {
             JavaFile.logger.log(java.util.logging.Level.SEVERE,
                                                             ex.getMessage(), ex);
        }
        return null;        
    }
    
    public void writeRef(ObjectOutputStream oos) throws IOException {
        oos.writeObject(getRevision());
    }
    
    public static Version readRef(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        JavaFile javaFile1 = JavaFile.getJavaFile(ois);
        CVSMetric cvsm = javaFile1.getCVSResultMetric();
        return cvsm.getRootVersion().getVersion((String)ois.readObject());
    }
    
    public State getState() {
        return state;
    }

    /** NetBeans cvs and issuezill is probably in different timezome 
     * add issues  with +- 12 hours to revision date
     */
    Collection<Issue> filterIssues(Collection<Issue> iss) {
        List<Issue> filterIssues = new ArrayList<Issue>();
        long time = getDate().getTime();
 //       System.out.println(getJavaFile().getPackage().getSourceRoot() + "/" + getJavaFile().getPackage()  + "." + getJavaFile().getClassName() );
        for (Issue issue : iss) {
            Activity acts[] = issue.getActivities() ;
            boolean ok = false;
            for (Activity act: acts) {
                if ("resolution".equals(act.fieldName) && "FIXED".equals(act.newValue)) {
                   long diffTime = (  time - act.when.getTime());
   //                System.out.println("Date: " + act.when + " " + getDate() +  " : " + diffTime); 
                   if (Math.abs(diffTime) < bugCommitDiff ) {
                       filterIssues.add(issue);
                       ok = true;
                   }
                }
            }
  //          System.out.println("Issue: " + issue.getIssueId() + ", "  + ok);
            
        }
        return filterIssues;
    }
}
 