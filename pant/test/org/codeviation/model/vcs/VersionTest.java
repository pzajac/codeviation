/*
 * VersionTest.java
 *
 * Created on June 24, 2003, 10:22 AM
 */

package org.codeviation.model.vcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import junit.framework.TestCase;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Line;
import org.codeviation.model.TestUtil;
import org.codeviation.model.Version;
import org.codeviation.model.Version.State;

/**
 *
 * @author  pz97949
 */
public class VersionTest extends TestCase {
    private static String CVSROOT = "/home/pzajac/cvss/cvsrel/";
    private static final String COMMENT = ("fasdfsad 456 dsfsadf 435345 dfasdfas 534534");
    private Version version = new Version ("3.4.5.4", COMMENT,new Date(234234234),USER,Version.State.EXP);
    private static String USER = "zajo";
    
    /** Creates a new instance of VersionTest 
     * @param name 
     */
    public VersionTest(String name) {
        super(name);
    }
    protected void setUp() {
        TestUtil.clearCache();
    }
    
//    public static Test suite() {
//        TestSuite ts =  new TestSuite();
//        ts.addTest(new VersionTest("testParseLog"));
//        return ts;
//    }
    public void testGetDefectNumbers() {
        List<Integer> defects = version.parseIssues(); 
        assertTrue("2  != " +  defects.size(), defects.size() == 2 );
        assertTrue("defects[0]: 435345  != " + defects.get(0), defects.get(0) == 435345 ); 
        assertTrue("defects[1]: 534534  != " + defects.get(1), defects.get(1) == 534534); 
    }
    
    public void testGetBranch () {
      String branch = version.getBranch();  
      assertTrue("branch is not " + branch + " but  3.4.5",branch.equals("3.4.5"));
        
    }
    public void testGetLevel() {
        int level = version.getLevel();
        assertTrue(level + " != 3", level == 3);
        
    }
    public void testGetRevision () {
        String rev = version.getRevision();
        assertTrue(rev + " != 3.4.5.4", rev.equals("3.4.5.4"));
    }
    
    public void testGetComment () {
        String comment = version.getComment();
        assertTrue(comment + " != 'ahoj' ",comment.equals(COMMENT)); 
    }
    public void testCompareTo() {
        Version v1,v2,v3;
        v1 = new Version("1.1","a",new Date(1213123),USER,State.EXP);
        v2 = new Version("1.3", "b", new Date(43242),USER,State.EXP);
        v3 = new Version("1.1.2","c",new Date(42234234),USER,State.EXP);
        
        int result = v1.compareTo(v2);
        assertTrue("v1<-> v2 -1 != " + result, result == -1 );    
        result = v2.compareTo(v1);
        assertTrue(" v2 <->v1 1 != " + result, result == 1) ;
        result = v1.compareTo(v1);
        assertTrue(" v1 <-> v1 0 != " + result, result == 0);
        result = v3.compareTo(v1);
        assertTrue(" v3 <-> v1 1 != " + result, result == 1);
        result = v1.compareTo(v3);
        assertTrue(" v1 <-> v3 -1 != " + result, result == -1); 
    }
    
    public void testSortVersions() {
        ArrayList<Version> versions = new ArrayList<Version> ();
        versions.add(new Version ("1.3", "adas", new Date(3333333),USER,State.EXP));
        versions.add(new Version ("1.2", "adas", new Date(2222222),USER,State.EXP));
        versions.add(new Version ("1.4", "adas", new Date(4444444),USER,State.EXP));
        versions.add(new Version ("1.1", "adas", new Date(1111111),USER,State.EXP));
        Version version = Version.sortVersions(versions);
        assertTrue("1.1 != " + version.getRevision(), version.getRevision().equals("1.1"));
        version =  version.getNext();
        assertTrue("1.2 != " + version.getRevision(), version.getRevision().equals("1.2"));
        version = version.getNext();
        assertTrue("1.3 != " + version.getRevision(), version.getRevision().equals("1.3"));
        version = version.getNext();
        assertTrue("1.4 != " + version.getRevision(), version.getRevision().equals("1.4"));
    }
    public void testSortVersions2() {
        ArrayList<Version> versions = new ArrayList<Version> ();
        versions.add(new Version ("1.3", "adas",new Date (3333333),USER,State.EXP));
        versions.add(new Version ("1.2.2","adas",new Date(22222222),USER,State.EXP));
        versions.add(new Version ("1.2.1","adas",new Date(22222221),USER,State.EXP));
        versions.add(new Version ("1.2", "adas",new Date(2222222),USER,State.EXP));
        versions.add(new Version ("1.1", "adas", new Date(1111111),USER,State.EXP));
        Version version = Version.sortVersions(versions);
        
        assertTrue("1.1 != " + version.getRevision(), version.getRevision().equals("1.1"));
        version =  version.getNext();
        assertTrue("1.2 != " + version.getRevision(), version.getRevision().equals("1.2"));
         Version branch = version.getMyBranch();
        version = version.getNext();
        assertTrue("1.3 != " + version.getRevision(), version.getRevision().equals("1.3"));
        assertTrue("1.2.1 != " + branch.getRevision(), branch.getRevision().equals("1.2.1"));
        version =  branch.getNext();
        assertTrue("1.2.2 != " + version.getRevision(), version.getRevision().equals("1.2.2"));
    }
    
    public void testParseLog() throws IOException {
          File prj = ExamplesSetup.getTestJ2seProjectDir(); 
          File javaMainFile = new File(prj,"src/testj2seexample/Main.java");
          JavaFile jf = JavaFile.getJavaFile(javaMainFile, "testj2seexample");
          assertNotNull(jf); 
          CVSMetric cvsm = jf.getMetric(CVSMetric.class);
          assertNotNull(cvsm);
          assertNotNull(cvsm.getRootVersion());
          
          // 1. versions
          
          Version vers = jf.getCVSVersion();
          assertNotNull(vers);
          assertEquals("cvs version is 1.3","1.3",vers.getRevision());
          
          // 2. diffs
          
          Diff diffs[]  = jf.getDiffs();
          System.out.println(diffs.length);
          Diff d = diffs[1];
          assertEquals("1.1",d.getVersion1().getRevision());
          assertEquals("1.2", d.getVersion2().getRevision());
          List<Line> lines = d.getLines();
          Line l = lines.get(0);
          assertNotNull(l.getPosition());
          assertNull(l.getInitialContent());
          assertEquals("import java.util.List;",l.getNewContent());
          assertEquals(-1,l.getInitialLineNumber());
          assertEquals(4,l.getNewLineNumber());
          
          l = lines.get(1);
          assertEquals("        ArrayList list = new ArrayList();",l.getInitialContent());
          assertEquals("        List list = new ArrayList();",l.getNewContent());
          assertEquals(7,l.getInitialLineNumber());
          assertEquals(8,l.getNewLineNumber());          
          
          
          assertEquals(0,diffs[0].getVersion1().getLines().size());
          

          System.out.println("--------------");
          List<Line> ll = diffs[1].getVersion2().getLines();
          for (Line line : ll) {
              System.out.println(line);
          }
          
          
          System.out.println("--------------------");
          l = ll.get(3);
          assertNotNull(l.getPosition());
          assertEquals("import java.util.List;",l.getNewContent());
          l = ll.get(7);
          assertEquals("        List list = new ArrayList();",l.getNewContent());
          
          //
          // test replaced versions
          Diff diff = diffs[0];
          lines = diff.getLines();
          Version v = lines.get(0).getReplaceVersion();
          assertNotNull(v);
          assertEquals("1.3",v.getRevision());
          assertNull(lines.get(1).getReplaceVersion());
          assertNull(lines.get(2).getReplaceVersion());
          v = lines.get(3).getReplaceVersion();
          assertNotNull(v);
          assertEquals("1.3",v.getRevision());          
          assertNull(lines.get(4).getReplaceVersion());
          assertNull(lines.get(5).getReplaceVersion());
          v = lines.get(6).getReplaceVersion();
          assertNotNull(v);
          assertEquals("1.2",v.getRevision());          
          assertNull(lines.get(7).getReplaceVersion());
          assertNull(lines.get(8).getReplaceVersion());
          assertNull(lines.get(9).getReplaceVersion());
  
          lines = diffs[1].getLines();
          assertEquals(2,lines.size());
          v = lines.get(0).getReplaceVersion();
          assertNotNull(v);
          assertEquals("1.3",v.getRevision());          
          v = lines.get(1).getReplaceVersion();
          assertNotNull(v);
          assertEquals("1.3",v.getRevision());          
          
          lines = diffs[2].getLines();
          assertNull(lines.get(3).getReplaceVersion());
          assertNull(lines.get(4).getReplaceVersion());
          
// Generator :
//
//          PrintWriter pw = new PrintWriter("/tmp/vers.txt");
//          for (Diff diff : diffs ) {
//              for (Line line : diff.getLines() ) {
//                  pw.println(line.getPosition() + ";" + line.getReplaceVersion() + "; " + line.getNewContent());
//              }
//          }
//          pw.close();
    }
    
    public static void compareLogs(InputStream is1,InputStream is2) throws IOException {
        assertNotNull(is1);
        assertNotNull(is2);
        BufferedReader r1 = new BufferedReader(new InputStreamReader(is1));
        BufferedReader r2 = new BufferedReader(new InputStreamReader(is2));
        int line = 0 ;
        String s1 = null;
        String s2 = null;
        
        while (true ) {
            s1 = r1.readLine();
            s2 = r2.readLine();
    //        System.out.println(s1);
    //        System.out.println(s2);
            if (s1 == null || s2 == null) {
                break;
            }
            assertTrue("the log files are different on line " + (line++), s1.equals(s2));
        }
        assertTrue ("The files has different size, line =  " + line, s1 == null && s2 == null);
        
    }
    private void showVersion(Version version) {
        if (version == null) {
            return;
        }
        System.out.println(version.getRevision());
        showVersion(version.getMyBranch());
        showVersion(version.getNext());
    }
        
    private static void logVersion (Version v,PrintStream ps) {
        ps.println(v.getRevision());
        if (v.getMyBranch() != null) {
             logVersion(v.getMyBranch(),ps);
        }
        if (v.getNext() != null) {
            logVersion(v.getNext(),ps);
        }
    }
       
    public static void main (String args[]) {
    junit.textui.TestRunner.run(new VersionTest("testAddDir"));
    }
}
