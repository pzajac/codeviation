/*
 * LongDesc.java
 *
 * Created on November 15, 2002, 1:42 PM
 */

package org.codeviation.bugtracking.issuezilla;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 *
 * @author  pz97949
 */
public class LongDesc {
    String who;
    String text;
    Timestamp when;
    
    public static final String WHO_STRING = "WHO";
    public static final String THETEXT_STRING = "THETEXT";
    public static final String WHEN_STRING = "ISSUE_WHEN";
    /** Creates a new instance of LongDesc */
    public static class EntResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId,String systemId) {
            try {
                if (systemId != null ) {
                    InputSource is = new InputSource(LongDesc.class.getResourceAsStream("issuezilla.dtd"));
                    return is;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    
    public LongDesc(String who, String theText, Timestamp when) {
        this.who = who;
        //        this.text = theText;
        this.when = when;
        
        // format text
        StringBuffer sb = new StringBuffer(theText.length()) ;
        for (int i = 0, column = 0 ; i < theText.length() ; i++ ) {
            char ch = theText.charAt(i);
            column++;
            if (ch == '\n' || column == 67) {
                int spaceChar = searchSpaceChar(theText,i);
                sb.append(theText.substring(i,spaceChar));
                sb.append('\n');
                i = spaceChar;
                column = 0;
            } else {
                sb.append(ch);
            }
        }
        text = sb.toString();
    }
    
    private static int searchSpaceChar(String theText,int i) {
        char breakChr[] = new char [] {' ','\t','\n',',',';',':','\"','\''};
        boolean br = false;
        for (; i < theText.length() ; i++ ) {
            char ch = theText.charAt(i);
            for (int k = 0 ; k < breakChr.length ; k++ ) {
                if (ch == breakChr[k]) {
                    br = true;
                    break;
                }
            }
            if (br) break;
        }
        return i;
    }
    
    public void printToXML(PrintStream ps) {
        //<longdesc>
        ps.println("<longdesc>");
        //</longdesc>
        ps.println("</longdesc>");
    }
    
    /** Getter for property text.
     * @return Value of property text.
     *
     */
    public java.lang.String getText() {
        if (text == null) {
            return "";
        }
        return text;
    }
    
    /** Setter for property text.
     * @param text New value of property text.
     *
     */
    public void setText(java.lang.String text) {
        this.text = text;
    }
    
    /** Getter for property when.
     * @return Value of property when.
     *
     */
    public Timestamp getWhen() {
        return when;
    }
    
    /** Setter for property when.
     * @param when New value of property when.
     *
     */
    public void setWhen(Timestamp when) {
        this.when = when;
    }
    
    /** Getter for property who.
     * @return Value of property who.
     *
     */
    public java.lang.String getWho() {
        return who;
    }
    
    /** Setter for property who.
     * @param who New value of property who.
     *
     */
    public void setWho(java.lang.String who) {
        this.who = who;
    }
    public static LongDesc [] getFromXML(int issueId) throws IOException, SQLException {
        Statement stmt = IssuezillaUtil.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("select xml from xmlissue where id=" + issueId);
            if (!rs.next()) {
                throw new IOException("Cannot get xml of #" + issueId + " from XMLISSUE table");
            }
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(new EntResolver());
            Document doc = builder.parse(rs.getAsciiStream(1));
            Element el = (Element)doc.getElementsByTagName("issuezilla").item(0);
            el = (Element) el.getElementsByTagName("issue").item(0);
            NodeList elements = el.getElementsByTagName("long_desc");
            Vector<LongDesc> longDescs = new Vector<LongDesc>();
            for (int i = 0 ; i < elements.getLength() ; i++ ) {
                el = (Element)elements.item(i);
                String who = el.getElementsByTagName("who").item(0).getFirstChild().getNodeValue();
                String when = el.getElementsByTagName("issue_when").item(0).getFirstChild().getNodeValue();
                Text t = (Text) el.getElementsByTagName("thetext").item(0).getFirstChild();
                String theText = t.toString();
                longDescs.add(new LongDesc(who,theText,parseTimeStamp(when)));
            }
            LongDesc array [] = new LongDesc[longDescs.size()];
            longDescs.copyInto(array);
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            stmt.close();
        }
    }
    private static Timestamp parseTimeStamp(String str) {
        try {
            // is parsed yyyy:mm:dd hh:mm?
            return Timestamp.valueOf(str );
        } catch (Exception e) {
            //yyyymmddhhmm
            String year = str.substring(0,4);
            String month = str.substring(4,6);
            String day = str.substring(6,8);
            String hour = str.substring(8,10);
            String min = str.substring(10,12);
            String sec = str.substring(12,14);
            return Timestamp.valueOf(year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec);
        }
    }
    
    public String toString() {
        return "who: " + who + '\n' +
                "when:" + when + '\n' +
                "theText:" + text;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            LongDesc longDesc[] = getFromXML(10000);
            for (int i = 0 ; i < longDesc.length ; i++ ) {
                System.out.println(longDesc[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
