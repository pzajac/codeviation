/*
 * IssueParser.java
 *
 * Created on May 12, 2003, 3:09 PM
 */

package org.codeviation.bugtracking.issuezilla;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * It Parser ISSUES xml 
 * @author  pz97949
 */
public class IssueParser {
    
    /** Creates a new instance of IssueParser */
    public IssueParser() {
        
    }
    
    private static String getValueOfElement(Element element ,String subElementName) throws DOMException {
        NodeList list = element.getElementsByTagName(subElementName);
        if (list.getLength() == 0 ) {
            return null;
        }
        Element el = (Element ) list.item(0);
        Node node = el.getFirstChild();
        return  (node == null ) ? null : node.getNodeValue();
    }
    
    /** parse list of subnotes 
     */ 
    private static int [] getIntArray(Element issueElement,String subElementName) { 
       NodeList list = issueElement.getElementsByTagName(subElementName);
       int ints [] = new int[list.getLength()]; 
       for (int it = 0 ; it < list.getLength() ; it++) {
           ints[it] = Integer.parseInt(list.item(it).getFirstChild().getNodeValue());
       }
       return ints;
    }
    
    /** join list of elements by "'"
     */ 
    private static String getStringList(Element issueElement,String subElementName) {
        NodeList list = issueElement.getElementsByTagName(subElementName);
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (int i = 0 ; i < list.getLength(); i++ ) {
            Node node = list.item(i).getFirstChild();
            if (node != null) {
                if (first != true) {
                    buffer.append(", ");
                } else {
                    first = true;
                }
                buffer.append(node.getNodeValue());
            }
        }
        return buffer.toString();
    }
    
    private static String [] getStringArray(Element element, String subElementName) {
        NodeList list = element.getElementsByTagName(subElementName);
        String  values [] = new String [list.getLength()];
        for (int i = 0 ; i < list.getLength() ; i++ ) {
            values[i] = list.item(i).getFirstChild().getNodeValue();
        }
        return values;
    }
           
    private static Timestamp parseTimeStamp(String string) {
        if (string.indexOf('-') != -1 ) {
            int count = 0;
            for (int i = 0 ; i < string.length() ; i++) {
                if (string.charAt(i) == ':') {
                    count++;
                }
            }
            if (count == 1 ) {
                string = string + ":00";
            }
            return Timestamp.valueOf(string);
        } else {
            //yyyy-mm-dd hh:mm:ss.fffffffff
            String year = string.substring(0,4);
            String mm = string.substring(4,6);
            String dd = string.substring(6,8);
            String hh = string.substring(8,10);
            String ss = string.substring(10,12);
            String ffff = string.substring(12);
            Timestamp ts = Timestamp.valueOf(year + "-" + mm + "-" + dd + " " + hh + ":" + mm + ":" + ss + "." + ffff);
            return ts;
        }
        
    }
    /** 
     *@throws IllegalStateException when exists attribute  issue error (NotFound|NotPermitted) 
     */
    public static Collection parseXml(InputStream xmlStream) throws SAXException,IOException,IllegalStateException,ParserConfigurationException  {
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
       DocumentBuilder builder = dbf.newDocumentBuilder();
       Document doc = builder.parse(xmlStream);
       
//    <!ELEMENT issuezilla (issue+)>
       Element element = doc.getDocumentElement();
       List<Issue> issues = new ArrayList<Issue>();
       if (element.getNodeName().equals("issuezilla")) {
//  <!ELEMENT issue ( issue_id, component, version, rep_platform, assigned_to,
//    delta_ts, subcomponent, reporter, target_milestone?, issue_type, creation_ts,
           NodeList list = element.getElementsByTagName("issue");
           for (int issueCount = 0 ; issueCount <  list.getLength() ; issueCount++) {
               Element issueElement = (Element) list.item(issueCount);
               int issue_id = Integer.parseInt(getValueOfElement(issueElement,"issue_id"));                 
               Issue issue = new Issue(issue_id);
               issue.setComponent(getValueOfElement(issueElement,"component"));
               issue.setVersion(getValueOfElement(issueElement,"version"));
               issue.setPlatform(getValueOfElement(issueElement,"rep_platform"));
               issue.setAssignedTo(getValueOfElement(issueElement,"assigned_to"));
               issue.setDeltaTimeStamp(parseTimeStamp(getValueOfElement(issueElement,"delta_ts")));
               issue.setSubComponent(getValueOfElement(issueElement,"subcomponent"));
               issue.setReporter(getValueOfElement(issueElement,"reporter"));
               issue.setTargetMilestone(getValueOfElement(issueElement,"target_milestone"));
               issue.setIssueType(getValueOfElement(issueElement,"issue_type"));
               issue.setCreationTimeStamp(parseTimeStamp(getValueOfElement(issueElement, "creation_ts")));


    //    qa_contact?, status_whiteboard?, votes?, op_sys, short_desc, keywords*, dependson*,
               issue.setQAContact(getValueOfElement(issueElement, "qa_contact"));
               issue.setStatusWhiteBoart(getValueOfElement(issueElement, "status_whiteboard"));
               String votes = getValueOfElement(issueElement, "votes");
               issue.setVotes(votes == null ? 0 : Integer.parseInt(votes));
               issue.setOpSys(getValueOfElement(issueElement, "op_sys"));
               issue.setShortDesc(getValueOfElement(issueElement, "short_desc"));
               // keywords*
               issue.setKeyword(getStringList(issueElement,"keywords"));

               // dependson*
               issue.setDependsOn(getIntArray(issueElement,"dependson"));

    //    blocks*, cc*, long_desc+, attachment*)>
              issue.setBlocks(getStringArray(issueElement,"blocks"));

              // cc*
              issue.setCCs(getStringArray(issueElement,"cc"));
              //long_desc+
              //    <!ELEMENT long_desc (who, issue_when, thetext)>
              NodeList long_descNodes = issueElement.getElementsByTagName("long_desc");
              LongDesc longDescs [] = new LongDesc[long_descNodes.getLength()];
              for (int itLS = 0 ; itLS < long_descNodes.getLength() ; itLS++) {
                  Element lsElement = (Element) long_descNodes.item(itLS);
                  String who =  getValueOfElement(lsElement, "who");
                  String thetext = getValueOfElement(lsElement, "thetext");
                  Timestamp issue_when = Timestamp.valueOf(getValueOfElement(lsElement,"issue_when"));
                  LongDesc ls = new LongDesc(who,thetext,issue_when);
                  longDescs[itLS] = ls;
              }
              issue.setLongDescs(longDescs);


              //attachment* [ignored]

              //attributes of issue
    //    <!ATTLIST issue error (NotFound|NotPermitted) #IMPLIED>
              String attr = issueElement.getAttribute("error");
              if (attr != null && attr.length() > 1) {
                  throw new IllegalStateException("Issue error " + attr);
              }

    //    <!ATTLIST issue issue_status
              issue.setStatus(Status.findStatus(issueElement.getAttribute("issue_status")));
    //    <!ATTLIST issue priority (P1|P2|P3|P4|P5) #REQUIRED>
              issue.setPriority(Priority.valueOf(issueElement.getAttribute("priority")));
  
    //    <!ATTLIST issue resolution
    //        (FIXED|INVALID|WONTFIX|LATER|REMIND|DUPLICATE|WORKSFORME|MOVED) #IMPLIED>
              issue.setResolution(Resolution.findResolution(issueElement.getAttribute("resolution")));          
    //
    //
    //    <!-- Data pertaining to attachments.  NOTE - some of these fields    -->
    //    <!-- are currently unimplemented (ispatch, filename, etc.).          -->
    //
    //    <!ELEMENT attachment (mimetype, attachid, date, desc, ispatch*, filename,
    //        submitter_id, data, attachment_iz_url)>
    //
    //      <!-- encoding   : How the inline attachment is encoded.            -->
    //
    //      <!ATTLIST attachment encoding CDATA #FIXED "Base64" >
    //
    //      <!-- mimetype     : Mime type for the attachment.                  -->
    //      <!-- attachid     : A unique id for this attachment.               -->
    //      <!-- date         : Timestamp of when added 'yyyy-mm-dd hh:mm'     -->
    //      <!-- desc         : Short description for attachment.              -->
    //      <!-- ispatch      : Whether attachment is a patch file.            -->
    //      <!-- filename     : Filename of attachment.                        -->
    //      <!-- submitter_id : Issuezilla ID of attachement submitter.        -->
    //      <!-- data         : Encoded attachment.                            -->
    //      <!-- attachment_iz_url : URL to attachment in iz.                  -->
    //
    //      <!ELEMENT mimetype (#PCDATA)>
    //      <!ELEMENT attachid (#PCDATA)>
    //      <!ELEMENT date (#PCDATA)>  
    //      <!ELEMENT desc (#PCDATA)> 
    //      <!ELEMENT ispatch (#PCDATA)>
    //      <!ELEMENT filename (#PCDATA)>
    //      <!ELEMENT submitter_id (#PCDATA)>
    //      <!ELEMENT data (#PCDATA)>   
    //      <!ELEMENT attachment_iz_url (#PCDATA)>   
              issues.add(issue);
           } // FOR 
       } // IF ISSUES 
       return issues;
    } //Enumeration parseXml(InputStream xmlStream) throws SAXException,IOException,IllegalStateException  {     
}
