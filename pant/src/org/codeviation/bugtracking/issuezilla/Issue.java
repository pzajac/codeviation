/*
 * Issue.java
 *
 * Created on November 14, 2002, 5:17 PM
 */

package org.codeviation.bugtracking.issuezilla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author  pz97949
 */
public class Issue {
    
    // constants 
    //
    
    // ISSUE columns
    public static final String ISSUE_ID_STRING = "ISSUE_ID";
    public static final String ISSUE_STATUS_STRING = "ISSUE_STATUS";    
    public static final String PRIORITY_STRING = "PRIORITY";    
    public static final String RESOLUTION_STRING = "RESOLUTION";
    public static final String COMPONENT_STRING = "COMPONENT";    
    public static final String VERSION_STRING = "VERSION";    
    public static final String REP_PLATFORM_STRING = "REP_PLATFORM";    
    public static final String ASSIGNED_TO_STRING = "ASSIGNED_TO";    
    public static final String DELTA_TS_STRING = "DELTA_TS";    
    public static final String SUBCOMPONENT_STRING = "SUBCOMPONENT";    
    public static final String REPORTER_STRING = "REPORTER";    
    public static final String TARGET_MILESTONE_STRING = "TARGET_MILESTONE";    
    public static final String ISSUE_TYPE_STRING = "ISSUE_TYPE";    
    public static final String CREATION_TS_STRING = "CREATION_TS";    
    public static final String QA_CONTACT_STRING = "QA_CONTACT";    
    public static final String STATUS_WHITEBOARD_STRING = "STATUS_WHITEBOARD";    
    public static final String VOTES_STRING = "VOTES";    
    public static final String OP_SYS_STRING = "OP_SYS";    
    public static final String SHORT_DESC_STRING = "SHORT_DESC";    
    public static final String KEYWORD_STRING = "KEYWORD";    
    
    /**  if is true the long desc wan't be loaded from database. the getLongDesc() returns 
     *  empty array
     **/
    private static  boolean  ignoreLongDesc = false;
///////////////
// persistent data 
    
    private int issueId;
    private Status status; 
    private Priority priority;
    private Resolution resolution;
    private String component;
    private String version;
    private String platform;
    private String assignedTo;
    
    private Timestamp deltaTimeStamp;
    private String SubComponent;
    private String reporter;
    private String targetMilestone;
    private String issueType;
    private Timestamp  creationTimeStamp;
    private String qaContact;
    private String statusWhiteBoart;
    private int votes;
    private String opSys;
    private String shortDesc;
    private String keyword;    
    /** array of dependend issue_ids 
     */ 
    private int dependsOn[];
    private LongDesc longDescs[] ;
    private String blocks[];
    private String CCs[];
    private Attachment attachments[];
    private Activity activities[];
    private IsDuplicate isDuplicate;
    
// temporary data
//    
    /** is dependsOn[] read?
     */
    private boolean bReadDependsOn = false;
    /** is longDescs[] read 
     */
    private boolean bReadLongDesc = false;
    private boolean bReadCCs = false;
    private boolean bReadBlocks = false ;
    
    
    /** set property from SQL request 
     */
    public void setPropertyAsString(String propName, String value) throws IOException {
        if (propName.compareToIgnoreCase(ISSUE_ID_STRING) == 0) {
            int issId = Integer.valueOf(value).intValue(); 
            setIssueId(issId);
        } else if (propName.compareToIgnoreCase(ISSUE_STATUS_STRING) == 0) {
            setStatus(Status.findStatus(value));
        } else if (propName.compareToIgnoreCase(PRIORITY_STRING) == 0) {
            setPriority(Priority.valueOf(value));
        } else if (propName.compareToIgnoreCase(RESOLUTION_STRING) == 0) {
            setResolution(Resolution.findResolution(value));
        } else if (propName.compareToIgnoreCase(COMPONENT_STRING) == 0) {
            setComponent(value);
        } else if (propName.compareToIgnoreCase(VERSION_STRING) == 0) {
            setVersion(value);
        } else if (propName.compareToIgnoreCase(REP_PLATFORM_STRING) == 0) {
            setPlatform(value);
        } else if (propName.compareToIgnoreCase(ASSIGNED_TO_STRING) == 0) {
            setAssignedTo(value);
        } else if (propName.compareToIgnoreCase(DELTA_TS_STRING) == 0) {
            setDeltaTimeStamp(Timestamp.valueOf(value));
        } else if (propName.compareToIgnoreCase(SUBCOMPONENT_STRING) == 0) {
            setSubComponent(value);
        } else if (propName.compareToIgnoreCase(REPORTER_STRING) == 0) {
            setReporter(value);
        } else if (propName.compareToIgnoreCase(TARGET_MILESTONE_STRING) == 0) {
            setTargetMilestone(value);
        } else if (propName.compareToIgnoreCase(ISSUE_TYPE_STRING) == 0) {
            setIssueType(value);
        } else if (propName.compareToIgnoreCase(CREATION_TS_STRING) == 0) {
            setCreationTimeStamp(Timestamp.valueOf(value));
        } else if (propName.compareToIgnoreCase(QA_CONTACT_STRING) == 0) {
            setQAContact(value);
        } else if (propName.compareToIgnoreCase(KEYWORD_STRING) == 0) {
            setKeyword(value);
        } else if (propName.compareToIgnoreCase(OP_SYS_STRING) == 0) {
            setOpSys(value);
        } else if (propName.compareToIgnoreCase(SHORT_DESC_STRING) == 0) {
            setShortDesc(value);
        } else {
            throw new IOException ("property name " + propName + " doesn't exist");
        }
    }
    
    
    public String getPropertyAsString(String propName) throws IOException {
        if (propName.compareToIgnoreCase(ISSUE_ID_STRING) == 0) {
            return (new Integer(getIssueId()).toString());
        } else if (propName.compareToIgnoreCase(ISSUE_STATUS_STRING) == 0) {
            return getStatus().toString();
        } else if (propName.compareToIgnoreCase(PRIORITY_STRING) == 0) {
            return getPriority().toString(); 
        } else if (propName.compareToIgnoreCase(COMPONENT_STRING) == 0) {
            return getComponent();
        } else if (propName.compareToIgnoreCase(VERSION_STRING) == 0) {
            return getVersion();
        } else if (propName.compareToIgnoreCase(REP_PLATFORM_STRING) == 0) {
            return getPlatform();
        } else if (propName.compareToIgnoreCase(ASSIGNED_TO_STRING) == 0) {
            return getAssignedTo();
        } else if (propName.compareToIgnoreCase(DELTA_TS_STRING) == 0) {
            return getDeltaTimeStamp().toString();
        } else if (propName.compareToIgnoreCase(SUBCOMPONENT_STRING) == 0) {
            return getSubComponent().toString();
        } else if (propName.compareToIgnoreCase(REPORTER_STRING) == 0) {
            return getReporter().toString();
        } else if (propName.compareToIgnoreCase(TARGET_MILESTONE_STRING) == 0) {
            return getTargetMilestone().toString();
        } else if (propName.compareToIgnoreCase(ISSUE_TYPE_STRING) == 0) {
            return getIssueType();
        } else if (propName.compareToIgnoreCase(CREATION_TS_STRING) == 0) {
            return getCreationTimeStamp().toString();
        } else if (propName.compareToIgnoreCase(QA_CONTACT_STRING) == 0) {
            return getQAContact();
        } else if (propName.compareToIgnoreCase(KEYWORD_STRING) == 0) {
            return getKeyword();
        } else if (propName.compareToIgnoreCase(OP_SYS_STRING) == 0) {
            return getOpSys();
        } else if (propName.compareToIgnoreCase(SHORT_DESC_STRING) == 0) {
            return getShortDesc();
        } else {
            throw new IOException ("Invalid property name");
        }
    }
    
    /** Getter for property opSys.
     * @return Value of property opSys.
     *
     */
    public java.lang.String getOpSys() {
        return opSys;
    }
    
    /** Setter for property opSys.
     * @param opSys New value of property opSys.
     *
     */
    public void setOpSys(java.lang.String opSys) {
        this.opSys = opSys;
    }
    
    /** returns names of all columns of ISSUE table
     */
    public static String [] getSimplePropertyNames() {
        return new String [] {
          ISSUE_ID_STRING,
          ISSUE_STATUS_STRING,
          PRIORITY_STRING,
          RESOLUTION_STRING,
          COMPONENT_STRING,
          VERSION_STRING,
          REP_PLATFORM_STRING,
          ASSIGNED_TO_STRING,
          DELTA_TS_STRING,
          SUBCOMPONENT_STRING,
          REPORTER_STRING,
          TARGET_MILESTONE_STRING,
          ISSUE_TYPE_STRING,
          CREATION_TS_STRING,
          QA_CONTACT_STRING,
          KEYWORD_STRING,
          OP_SYS_STRING,
          SHORT_DESC_STRING
        } ;

    }
        
    
    /** Creates a new instance of Issue 
     *  id doesn't reload issue from local database 
     */
    public Issue(int issueId) {
        this.issueId = issueId;
    }
    
   public boolean readFromLocalDb()  {
        try {
            Statement stmt = IssuezillaUtil.getStatement();
            ResultSet rs = stmt.executeQuery("select * from issue where issue_id='" + getIssueId() + "'");
            if (rs.next() == false) {
                return false;
            }
            String propertyNames[] = getSimplePropertyNames();
            for (int i = 0 ; i < propertyNames.length ; i++ ) {
                String value = rs.getString(propertyNames[i]);
                
                setPropertyAsString(propertyNames[i], value);
            }
            bReadLongDesc  = false;
            bReadDependsOn = false;
            bReadCCs = false;
            bReadBlocks = false;
            stmt.close();
            rs.close();
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static Collection<Issue> readIssues(Collection<Integer> issueIds) throws SQLException,IOException {
        ArrayList<Issue> list = new ArrayList<Issue>();
            Statement stmt = IssuezillaUtil.getStatement();
            // read 20 issues in one time 
            Iterator idIt = issueIds.iterator();
            
            while(idIt.hasNext()) {
                StringBuffer idBuf = new StringBuffer();
                for (int counter = 0 ; counter < 40 && idIt.hasNext(); counter++ ) {
                    if (idBuf.length() > 0 ) {
                        idBuf.append(" or ");
                    }
                    idBuf.append("issue_id='");
                    idBuf.append(idIt.next());
                    idBuf.append("' ");
                }
                String query = "select * from issue where " + idBuf.toString();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    Issue issue = new Issue(rs.getInt("issue_id"));
                    try {
                        String propertyNames[] = getSimplePropertyNames();
                        for (int i = 0 ; i < propertyNames.length ; i++ ) {
                            String value = rs.getString(propertyNames[i]);
                            issue.setPropertyAsString(propertyNames[i], value);
                        }
                        issue.bReadLongDesc  = false;
                        issue.bReadDependsOn = false;
                        issue.bReadCCs = false;
                        issue.bReadBlocks = false;
                        list.add(issue);
                    } catch(Exception e) {
                        // XXX dirty hack!
                        Logger.getLogger(Issue.class.getName()).log(Level.SEVERE,"Issue " + issue.getIssueId(),e);
                    }
                }
               rs.close();
            }
            stmt.close();

        return list;
        
    }
    
   /** read issue for specific date 
    */
   public boolean readFromLocalDb (Timestamp  ts)  {
       if (readFromLocalDb() == false ) {
           return false;
       }
       try {
           Connection con = IssuezillaUtil.getConnection();
           PreparedStatement ps = con.prepareStatement(
              "select * from activity where issue_id=?  order by whn");
           ps.setInt(1,getIssueId());  
//           ps.setTimestamp(2,ts);
           ResultSet rs = ps.executeQuery();
           while (rs.next()) {
               String propName = rs.getString("what");
               
               String value = (ts.getTime() > rs.getTimestamp("whn").getTime()) 
                                    ? rs.getString("newvalue") :
                                      rs.getString("oldvalue") ;
               setPropertyAsString(propName,value);
           }
               
           ps.close();
           rs.close();
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
       return true;
   }
    /** reads long desc from LONG_DESC table
     */
    private LongDesc[] readLongDesc() throws SQLException,IOException  {
        Statement stmt = IssuezillaUtil.getStatement();
        ResultSet rs = stmt.executeQuery("select * from long_desc where ISSUE_ID='" + getIssueId() + "'");
        Vector<LongDesc> vec = new Vector<LongDesc>();
        while (rs.next()) {
            
            InputStream is = rs.getAsciiStream(LongDesc.THETEXT_STRING);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer sbuff = new StringBuffer();
                String row = null;
                while ( (row = reader.readLine()) != null ) {
                    sbuff.append(row);
                    sbuff.append("\n");
                }
                LongDesc ld = new LongDesc (rs.getString(LongDesc.WHO_STRING), 
                                            sbuff.toString(),
                                            rs.getTimestamp(LongDesc.WHEN_STRING));
                vec.add(ld);
            }
        }
        LongDesc lds[] = new LongDesc[vec.size()];
        vec.copyInto(lds);
        bReadLongDesc = true;
        stmt.close();
        rs.close();
        return lds;
    }
    
    /** reads dependsOn from DEPENDSON table 
     */
    private int[] readDependsOn() throws SQLException {
        Statement stmt = IssuezillaUtil.getStatement();
        ResultSet rs = stmt.executeQuery("select * from dependson where ISSUE_ID='" + getIssueId() + "'");
        Vector<Integer> vec = new Vector<Integer>();
        while (rs.next()) {
            vec.add(new Integer(rs.getInt("DEPENDS")));
        }
        int dependsOn[] = new int[vec.size()];
        for (int i = 0 ; i < vec.size() ; i++ ) {
            dependsOn[i] =((Integer) vec.elementAt(i)).intValue();
        }
        bReadDependsOn = true;
        stmt.close();
        rs.close();
        return dependsOn;
    }
    
    /** reads blocks from BLOCK table 
     */
    private String[] readBlocks() throws SQLException {
        Statement stmt = IssuezillaUtil.getStatement();
        ResultSet rs = stmt.executeQuery("select * from block where ISSUE_ID='" + getIssueId() + "'");
        Vector<String> vec = new Vector<String>();
        while (rs.next()) {
            vec.add(rs.getString("BLOCK"));
        }
        String blocks[] = new String[vec.size()];
        vec.copyInto(blocks);
        bReadBlocks = true;
        stmt.close();
        rs.close();
        return blocks;
    }
    
    /** reads CCs from CC table 
     */
    private String[] readCCs() throws SQLException {
        Statement stmt = IssuezillaUtil.getStatement();
        ResultSet rs = stmt.executeQuery("select * from cc where ISSUE_ID='" + getIssueId() + "'");
        Vector<String> vec = new Vector<String>();
        while (rs.next()) {
            vec.add(rs.getString("CC"));
        }
        String CCs[] = new String[vec.size()];
        vec.copyInto(CCs);
        bReadCCs = true;
        stmt.close();
        rs.close();
        return CCs;
    }
    
    

    /** write to XML specified by 
     *  http://www.netbeans.org/issues/issuezilla.dtd
     * @param ps output stream
     */
    public void writeToXml(PrintStream ps) throws SQLException, IOException  {
        Enumeration en = null;
        int i = 0;
     
        // issue
        ps.print("<issue ");
           // attlist error
           // attlist issue_status 
        ps.println("issue_status=\"" + getStatus().toString() + "\" ");
           // attlist priority
        ps.println("priority=\"" + getPriority().toString() + "\" "); 
           // attlist resolution
        ps.println("resolution=\"" + getResolution().toString() + "\" >");
        // issue_id
        ps.println("<issue_id>" + getIssueId() + "</issue_id>");
        // component
        ps.println("<component>" + IssuezillaUtil.asXMLString(getComponent()) + "</component>");
        // version
        ps.println("<version>" + IssuezillaUtil.asXMLString(getVersion()) + "</version>");
        // rep_platform
        ps.println("<rep_platform>" + IssuezillaUtil.asXMLString(getPlatform()) + "</rep_platform>");
        // assigned_to
        ps.println("<assigned_to>" + IssuezillaUtil.asXMLString(getAssignedTo()) + "</assigned_to>");
        // delta_ts
        ps.println("<delta_ts>" + IssuezillaUtil.asXMLString(getDeltaTimeStamp().toString()) + "</delta_ts>");
        // subcomponent
        ps.println("<subcomponent>" + IssuezillaUtil.asXMLString(getSubComponent()) + "</subcomponent>");
        // reporter
        ps.println("<reporter>" + IssuezillaUtil.asXMLString(getReporter()) + "</reporter>");
        // target_milestone?
        if (getTargetMilestone() != null && getTargetMilestone().length() > 0) {
            ps.println("<target_milestone>" + IssuezillaUtil.asXMLString(getTargetMilestone()) + "</target_milestone>");
        }
        // issue_type
        ps.println("<issue_type>" + IssuezillaUtil.asXMLString(getIssueType()) + "</issue_type>");
        // creation_ts
        ps.println("<creation_ts>" + IssuezillaUtil.asXMLString(getCreationTimeStamp().toString()) + "</creation_ts>");
        // qa_contact ?
        if (getQAContact() !=null && getQAContact().length() > 0) {
            ps.println("<qa_contact>" + IssuezillaUtil.asXMLString(getQAContact()) + "</qa_contact>");
        }
        // status_whiteboard ?
        if (getStatusWhiteBoart() != null && getStatusWhiteBoart().length() > 0) {
            ps.println("<status_whiteboard>" + IssuezillaUtil.asXMLString(getStatusWhiteBoart()) + "</status_whiteboard>");
        }
        // votes ?
        if (getVotes() > 0 ) {
            ps.println("<votes>" + IssuezillaUtil.asXMLString(String.valueOf(getVotes())) + "</votes>");
        }
        // op_sys
         ps.println("<op_sys>" + IssuezillaUtil.asXMLString(getOpSys()) + "</op_sys>");
        // short_desc
         ps.println("<short_desc>" + IssuezillaUtil.asXMLString(getShortDesc()) + "</short_desc>");
        // keywords*
         en = enumerateKeywords(getKeyword());
         while (en.hasMoreElements()) {
             String key = (String) en.nextElement();
             if (key.length() > 0 ) {
                ps.println("<keywords>" + IssuezillaUtil.asXMLString(key) + "</keywords>");
             }
         }
        // dependson*
         int depOns[] = getDependsOn();
         for (i = 0 ; i < depOns.length ; i++ ) {
             ps.println("<dependson>" + depOns[i] + "</dependson>");
         }
        // block* 
         String blocks[] = getBlocks();
         for (i = 0 ; i < blocks.length ; i++ ) {
             ps.println("<blocks>" + IssuezillaUtil.asXMLString(blocks[i]) + "</blocks>");
         }
        // cc*
         String cc[] = getCCs();
         for ( i = 0 ; i < cc.length ; i++ ) {
             ps.println("<cc>" + IssuezillaUtil.asXMLString(cc[i]) + "</cc>");
         }
        // long_desc+
         LongDesc ldesc[]  = getLongDescs();
         for (i = 0 ; i < ldesc.length ; i++ ) {
             ldesc[i].printToXML(ps);
         }
        // attachment*
         //[PENDING]
         
        // </issue>
         ps.println("</issue>");
        
    }
    
      public LongDesc [] getLongDescs() throws SQLException,IOException {
          if (bReadLongDesc == false) {
              if (ignoreLongDesc == true) {
                  longDescs = new LongDesc[]{};
              } else {
                  longDescs = readLongDesc();
              }
              bReadLongDesc = true;
          }
          return longDescs;
     }
      
     public void setLongDescs(LongDesc longDescs []) {
         bReadLongDesc = true;
         this.longDescs = longDescs;
     }
     public int [] getDependsOn() throws SQLException {
         if (bReadDependsOn == false) {
             dependsOn = readDependsOn();
             bReadDependsOn = true;
         }
         return dependsOn;
     }
    
  
    /** readIssue from local db
     */
    
    public static Issue readIssue(int issue_id) throws SQLException,IOException {
        Issue issue = new Issue(issue_id);
        return issue.readFromLocalDb() ? issue : null;
    }
    /** read issue from xml defined by issuezila DTD 
     */
    public static Collection  readIssueFromXml(InputStream xmlStream) throws SAXException, IOException,ParserConfigurationException {
        return IssueParser.parseXml(xmlStream);
    }
    
    public static Issue readIssueFromIssuezilla(int issueId) throws IOException, 
                                                             SAXException,
                                                             ParserConfigurationException{
        IssuezillaUtil.initProxy();
        URL url = new URL("http://www.netbeans.org/issues/xml.cgi?id=" + issueId);
        Collection list = IssueParser.parseXml(url.openStream());          
        Iterator it = list.iterator();
        if (it.hasNext()) {
            return (Issue) it.next();
        } else {
            return null;
        }
    } // readIssueFromIssuezilla
    
    /** Getter for property creationTimeStamp.
     * @return Value of property creationTimeStamp.
     *
     */
    public Timestamp getCreationTimeStamp() {
        return creationTimeStamp;
    }    
   
    /** Setter for property creationTimeStamp.
     * @param creationTimeStamp New value of property creationTimeStamp.
     *
     */
    public void setCreationTimeStamp(Timestamp creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }
    
    /** Getter for property priority.
     * @return Value of property priority.
     *
     */
    public Priority getPriority() {
        return priority;
    }
    
    /** Setter for property priority.
     * @param priority New value of property priority.
     *
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    /** Getter for property qa_contact.
     * @return Value of property qa_contact.
     *
     */
    public java.lang.String getQAContact() {
        return qaContact == null ? "" : qaContact;
    }
    
    /** Setter for property qa_contact.
     * @param qa_contact New value of property qa_contact.
     *
     */
    public void setQAContact(java.lang.String qa_contact) {
        this.qaContact = qa_contact;
    }
    
    /** Getter for property platform.
     * @return Value of property platform.
     *
     */
    public String getPlatform() {
        return platform;
    }
    
    /** Setter for property platform.
     * @param platform New value of property platform.
     *
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    /** Getter for property assignedTo.
     * @return Value of property assignedTo.
     *
     */
    public java.lang.String getAssignedTo() {
        return assignedTo == null ? "" : assignedTo;
    }
    
    /** Setter for property assignedTo.
     * @param assignedTo New value of property assignedTo.
     *
     */
    public void setAssignedTo(java.lang.String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    /** Getter for property deltaTimeStamp.
     * @return Value of property deltaTimeStamp.
     *
     */
    public Timestamp getDeltaTimeStamp() {
        return deltaTimeStamp;
    }
    
    /** Setter for property deltaTimeStamp.
     * @param deltaTimeStamp New value of property deltaTimeStamp.
     *
     */
    public void setDeltaTimeStamp(Timestamp timeStamp) {
        this.deltaTimeStamp = timeStamp;
    }
    
    /** Getter for property component.
     * @return Value of property component.
     *
     */
    public java.lang.String getComponent() {
        return component;
    }
    
    /** Setter for property component.
     * @param component New value of property component.
     *
     */
    public void setComponent(java.lang.String component) {
        this.component = component;
    }
    
    /** Getter for property issue_type.
     * @return Value of property issue_type.
     *
     */
    public java.lang.String getIssueType() {
        return issueType;
    }
    
    /** Setter for property issue_type.
     * @param issue_type New value of property issue_type.
     *
     */
    public void setIssueType(java.lang.String issue_type) {
        this.issueType = issue_type;
    }
    
    /** Getter for property issueId.
     * @return Value of property issueId.
     *
     */
    public int getIssueId() {
        return issueId;
    }
    
    /** Setter for property issueId.
     * @param issueId New value of property issueId.
     *
     */
    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }
    
    /** Getter for property reporter.
     * @return Value of property reporter.
     *
     */
    public java.lang.String getReporter() {
        return reporter;
    }
    
    /** Setter for property reporter.
     * @param reporter New value of property reporter.
     *
     */
    public void setReporter(java.lang.String reporter) {
        this.reporter = reporter;
    }
    
    /** Getter for property resolution.
     * @return Value of property resolution.
     *
     */
    public Resolution getResolution() {
        return resolution;
    }
    
    /** Setter for property resolution.
     * @param resolution New value of property resolution.
     *
     */
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }
    
    /** Getter for property status_whiteBoart.
     * @return Value of property status_whiteBoart.
     *
     */
    public java.lang.String getStatusWhiteBoart() {
        return statusWhiteBoart == null ? "" : statusWhiteBoart;
    }
    
    /** Setter for property status_whiteBoart.
     * @param status_whiteBoart New value of property status_whiteBoart.
     *
     */
    public void setStatusWhiteBoart(java.lang.String status_whiteBoart) {
        this.statusWhiteBoart = status_whiteBoart;
    }
    
    /** Getter for property SubComponent.
     * @return Value of property SubComponent.
     *
     */
    public java.lang.String getSubComponent() {
        return SubComponent;
    }
    
    /** Setter for property SubComponent.
     * @param SubComponent New value of property SubComponent.
     *
     */
    public void setSubComponent(java.lang.String SubComponent) {
        this.SubComponent = SubComponent;
    }
    
    /** Getter for property targetMilestone.
     * @return Value of property targetMilestone.
     *
     */
    public java.lang.String getTargetMilestone() {
        return targetMilestone;
    }
    
    /** Setter for property targetMilestone.
     * @param targetMilestone New value of property targetMilestone.
     *
     */
    public void setTargetMilestone(java.lang.String targetMilestone) {
        this.targetMilestone = targetMilestone;
    }
    
    /** Getter for property version.
     * @return Value of property version.
     *
     */
    public java.lang.String getVersion() {
        
        return version == null ? "" :version;
    }
    
    /** Setter for property version.
     * @param version New value of property version.
     *
     */
    public void setVersion(java.lang.String version) {
        this.version = version;
    }
    
    /** Getter for property votes.
     * @return Value of property votes.
     *
     */
    public int getVotes() {
        return votes;
    }
    
    /** Setter for property votes.
     * @param votes New value of property votes.
     *
     */
    public void setVotes(int votes) {
        this.votes = votes;
    }
    
    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }
    public String getShortDesc() {
        return (shortDesc == null )? "" : shortDesc;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public String getKeyword() {
        return keyword == null ? "" : keyword;
    }
    
    public String[] getCCs() throws SQLException {
        if (bReadCCs == false ) {
            CCs = readCCs() ;
        }
        bReadCCs = true;
        return CCs;
    }
    
    public void setCCs(String CCs[]) {
        this.CCs = CCs;
        bReadCCs = true;
    }
    
    public Status getStatus() {
        if (status == null) {
            return Status.notUsed;
        } else {
           return status;
        }
    }
    
    public void setStatus (Status status) {
        this.status = status;
    }
   
    public void setDependsOn(int depsOn[]) {
        this.dependsOn = depsOn;
        bReadDependsOn = true;
    }
    
    public Activity[] getActivities() {
        return activities;
    }

    public void setActivities(Activity[] activities) {
        this.activities = activities;
    }

    public IsDuplicate getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(IsDuplicate isDuplicate) {
        this.isDuplicate = isDuplicate;
    }
    
    /** parse keyword delimed by ','
     */
    private Enumeration enumerateKeywords(String keyword) {
        if (keyword == null) {
            keyword = "";
        }
        final StringTokenizer tokenizer = new StringTokenizer(keyword,",");
        Enumeration en;
        
        return new Enumeration () {
            public boolean hasMoreElements() {
                return tokenizer.hasMoreElements();
            }
            public Object nextElement() {
                return tokenizer.nextToken().trim();
            }
        };
    }
    
    /** Getter for property blocks.
     * @return Value of property blocks.
     *
     */
    public java.lang.String[] getBlocks() throws SQLException {
        if (bReadBlocks == false ) {
            this.blocks = readBlocks();
            bReadBlocks = true;
        }
        return blocks;
    }
    
    public void setBlocks(String []blocks) {
        bReadBlocks = true;
        this.blocks = blocks;
    }

    public boolean equals(Object object) {
        boolean ret = true; 
        try {
            if (object instanceof Issue) {
                Issue is = (Issue) object;
                ret = ret && getAssignedTo().equals(is.getAssignedTo()); 
                 //getAttachments ignored
//                ret = ret && Arrays.equals(getBlocks(),is.getBlocks());
                
                String cc1 [] = getCCs();
                String cc2 [] = is.getCCs();
                Arrays.sort(cc1);
                Arrays.sort(cc2);
                ret = ret && Arrays.equals(cc1,cc2);
                ret = ret && getComponent().equals(is.getComponent());
                ret = ret && getCreationTimeStamp().equals(is.getCreationTimeStamp());
           //[ignored]     ret = ret && getDeltaTimeStamp().equals(is.getDeltaTimeStamp());
                ret = ret && (getIssueId() == is.getIssueId());
                ret = ret && (getIssueType().equals(getIssueType()));
                ret = ret && (getKeyword().equals(getKeyword()));
                // getLongDescs() ignored
                ret = ret && (getOpSys().equals(is.getOpSys()));
                ret = ret && getPlatform().equals(is.getPlatform());
                ret = ret && getPriority().equals(is.getPriority());
                ret = ret && getQAContact().equals(is.getQAContact());
                ret = ret && getReporter().equals(is.getReporter());
                ret = ret && getResolution().equals(is.getResolution());
                ret = ret && getShortDesc().equals(is.getShortDesc());
                ret = ret && getStatus().equals(is.getStatus());
         //       ret = ret && getStatusWhiteBoart().equals(is.getStatusWhiteBoart());
                ret = ret && getSubComponent().equals(is.getSubComponent());
                ret = ret && getTargetMilestone().equals(is.getTargetMilestone());
                ret = ret && getVersion().equals(is.getVersion());
//                ret = ret && (getVotes() == is.getVotes());
            } else {
                ret = false ;
            }
        } catch (SQLException e) {
            ret = false;
            e.printStackTrace(System.err);
        }
        return ret;
    } //equals

    public Attachment[] getAttachments() {
        return attachments == null ? new Attachment[0] : attachments;
    }

    public void setAttachments(Attachment[] attachments) {
        this.attachments = attachments;
    }
    
    
    public static void setIgnoreLongDesc (boolean newIgnoreLongDesc) {
        ignoreLongDesc = newIgnoreLongDesc;
    }
    
    public static boolean getIgnoreLongDesc() {
        return ignoreLongDesc;
    }
}
