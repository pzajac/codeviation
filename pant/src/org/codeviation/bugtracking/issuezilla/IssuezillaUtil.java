/*
 * Util.java
 *
 * Created on August 22, 2002, 10:56 AM
 */

package org.codeviation.bugtracking.issuezilla;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author  pz97949
 */
public final class IssuezillaUtil {
    static Statement stmt;
    static Connection connection;
    private static StorageType storageType = StorageType.ISSUEZILLA_REPLICA_DB;
    private static boolean initializedProxy = false;
    /** enable. disable http proxy 
     **/
    private static String proxySet = "true";
    private static String proxyHost = "webcache.czech.sun.com";
    private static String proxyPort = "8080";

    static Timestamp parseDate(String date) {
        Timestamp ts = Timestamp.valueOf(date);
        long time = ts.getTime();
        // + 7 hours
        time += 7*1000*3600;
        ts.setTime(time);
        return ts;
    }

    /** Entity resolver for Issues.dtd
     */ 

    /** Creates a new instance of Util */
    private IssuezillaUtil() {
    }
    
    public static final Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
    public static void initProxy() {
        if (initializedProxy == false ) {
            initializedProxy = true;
            System.getProperties().put( "proxySet", proxySet );
            System.getProperties().put( "proxyHost", proxyHost );
            System.getProperties().put( "proxyPort", proxyPort );
        }
    }
    public static void disableProxy() {
        Properties props = System.getProperties();
        props.remove("proxySet");
        props.remove("proxyHost");
        props.remove("proxyPort");
        // ignore proxy initialization
        initializedProxy = true;
    }

    public static StorageType getStorageType() {
        return storageType;
    }

    public static void setStorageType(StorageType storageTyp) {
        IssuezillaUtil.storageType = storageTyp;
    }
    
   
    public static synchronized Connection getConnection() throws SQLException{
        if (connection == null ) {
            try {
               Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (Exception cnfe) {
                cnfe.printStackTrace();
                throw new SQLException(" Exception: " + cnfe );
           }
            connection = DriverManager.getConnection("jdbc:mysql://qa-amd64-linux.czech.sun.com:3306/issuezilla",  "guest", "guest");
        }
        return connection;
    }
    
    public static synchronized Statement getStatement() throws SQLException{
        /*if (stmt == null) {
            stmt = getConnection().createStatement();
        }*/
        return getConnection().createStatement();
        //return stmt;
    }
    
    /** convert value string to xml value 
     */
    public static String asXMLString (String xmlString) {
        StringBuffer buff = new StringBuffer () ;
        for (int i = 0 ; i < xmlString.length() ; i++ ) {
            char ch = xmlString.charAt(i);
            switch (ch) {
                case '&':
                    buff.append("&amp;");
                break;
                case '<':
                    buff.append("&lt;");
                break;
                case '>':
                    buff.append("&gt;");
                break;
                case '"':
                    buff.append("&quot;");
                break;
                default:
                    if ( ch > 127) {
                        buff.append('.');
                    } else {
                        buff.append(ch);
                    }
            }
        }
        return buff.toString();
    }
    
    /** XSLT transformation of collection of issues/
     * @param xsltStream input XSLT stream
     * @param issues Collection<Issue>
     * @param output result stream 
     */
    public static void xsltTransform(Collection issues, InputStream xsltStream,StreamResult output) 
            throws TransformerConfigurationException, TransformerException, SQLException,IOException {
     
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer(new StreamSource(xsltStream));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            ps.println("<issuezilla>");
            printIssues(issues, ps);
            ps.println("</issuezilla>");
            ps.close();

            // debug
            FileOutputStream fos = new FileOutputStream("/tmp/test.xml");
            fos.write(baos.toByteArray());
            // debug
            try { 
               ByteArrayInputStream bais  = new ByteArrayInputStream( baos.toByteArray());
               transformer.transform(new StreamSource(bais),output); 
            } catch (TransformerException  e) {
                System.out.println(baos.toString());
                throw e;
            }
                
        /** Creates a new instance of DomParser */


    }
    
    /**
     * print all issues from collection to XML. 
     * @param issues 
     * @param ps output stream
     */
    public static  void printIssues(Collection issues, PrintStream ps) throws SQLException,IOException  {
        for (Iterator it = issues.iterator() ; it.hasNext();) {
            Issue issue = (Issue) it.next();
            issue.writeToXml(ps);
        }
    }
    
    public static Timestamp toPacificTime(Timestamp ts) {
        /* + 9 hours */
        return new Timestamp(ts.getTime() - 1000*3600*9) ;
    }
    public static Timestamp fromPacificTime(Timestamp ts) {
        return new Timestamp(ts.getTime() + 1000*3600*9);
    }
    
    static void logSevere(Exception ex, String msg) {
        Logger.getLogger(IssuezillaUtil.class.getName()).log(Level.SEVERE, msg, ex);
    }
}
