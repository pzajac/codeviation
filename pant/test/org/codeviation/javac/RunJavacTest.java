/*
 * RunJavacTest.java
 * JUnit based test
 *
 * Created on November 15, 2006, 10:48 PM
 */


package org.codeviation.javac;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.tools.ant.types.Path;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.VersionInterval;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Path;
import org.codeviation.model.JavaFile;
import org.codeviation.model.JavaFileUtil;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.ExamplesSetup;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.VersionInterval;
import org.codeviation.model.PositionVersionIntervalResultContainer;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.Version;

/**
 *
 * @author pzajac
 */
public class RunJavacTest extends TestCase {
    protected File testPrjF;
    
    public RunJavacTest(String testName) {
        super(testName);
    }
    protected void setUp() {
        ExamplesSetup.checkoutExamples = false;
        TestUtil.clearCache();
        TestUtil.enableMetricLogger();
        testPrjF = ExamplesSetup.getUsageMetricsProjectDir();
    }
    
    static File getBuildDir(File testPrjF) {
        return new File (testPrjF,"build");
        
    }
    static void runJavac(File testPrjF) {
        Javac javac = new Javac();
        Project prj = new Project();
        prj.setProperty("build.compiler","org.codeviation.javac.MeasuringJavac");
        File buildDir = getBuildDir(testPrjF);
        TestUtil.deleteFile(buildDir);
        buildDir.mkdirs();
        Path path = new Path(prj);
        path.setPath(new File(testPrjF,"src").getAbsolutePath());
        javac.setSrcdir(path);
        javac.setDestdir(buildDir);
        javac.setProject(prj);
//        javac.setTarget("1.5");
        javac.execute();
        
    }
    
      public void runJavacBiggerApp() {
        Javac javac = new Javac();
        Project prj = new Project();
        prj.setProperty("build.compiler","org.codeviation.javac.MeasuringJavac");
        File buildDir = new File (testPrjF,"build");
        buildDir.mkdirs();
        Path path = new Path(prj);
        path.setPath(new File(testPrjF,"src").getAbsolutePath());
        javac.setSrcdir(path);
        javac.setDestdir(buildDir);
        Path cp = new Path(prj);
        FileList fileList = new FileList();
        fileList.setFiles("/home/pzajac/cvss/osobni/pant/extlib/ant.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/issuezillaquery.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/javac-api.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/javac-impl.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/org-netbeans-modules-java-source.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/jfreechart/gnujaxp.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/jfreechart/jcommon-0.8.0.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/jfreechart/jfreechart-0.9.8.jar:" +
                "/home/pzajac/cvss/osobni/pant/extlib/jfreechart/servlet.jar");
        cp.addFilelist(fileList);
        javac.setClasspath(cp);
        javac.setProject(prj);
        javac.execute();          
    }
//    public class Main {
//
//    /** Creates a new instance of Main */
//    public Main() {
//    }
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        // TODO code application logic here
//        List<String> list = new ArrayList<String>();
//        list.add("bbb");
//        System.out.println("ahoj");
//    }
//
//}
    // XXX mising new ArrayList<String>();  
    public void testCompileAndUsages() throws IOException, InterruptedException {
        String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
        ExamplesSetup.updateFile(filePath, "1.3");
        runJavac(testPrjF);          
        File javaMainFile = new File(testPrjF,"src/usagemetrics/Main.java");
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        UsagesMetric ur = jf.getMetric(UsagesMetric.class);
        PositionVersionIntervalResultContainer<UsageItem> storage =  ur.getStorage();
        Set<PositionIntervalResult<UsageItem>> items = storage.getAllObjects();
        CVSMetric cvs = JavaFileUtil.getCVSResultMetric(jf);
        assertTrue(items.size() > 0);
        List<PositionIntervalResult<UsageItem>> sortedItems = new ArrayList<PositionIntervalResult<UsageItem>>();
        sortedItems.addAll(items);
        Collections.sort(sortedItems, new PIRComparator());
        
        Iterator<PositionIntervalResult<UsageItem>> it = sortedItems.iterator();
          
        assertUsage(it,"1.3","1.3","java.lang.Object","Object()","",ur,cvs);
        assertUsage(it,"1.3","1.3","java.util.ArrayList","ArrayList()","",ur,cvs);
        assertUsage(it,"1.3","1.3","java.util.List","add(E)","list",ur,cvs);
        assertUsage(it,"1.3","1.3","java.io.PrintStream","println(java.lang.String)","System.out",ur,cvs);
        assertFalse(it.hasNext());
        
        Repository rep = PersistenceManager.getDefault().getRepository(ExamplesSetup.getCvsWork());
        assertEquals(1,rep.getSourceRoots().size());
        SourceRoot sr = rep.getSourceRoots().get(0);
        
        assertEquals("pantexamples/usagemetrics/src",sr.getRelPath());
        List<org.codeviation.model.Package> packages = sr.getPackages();
        assertEquals(1,packages.size());
        org.codeviation.model.Package pack = packages.get(0);
        assertEquals("usagemetrics",pack.getName());
        List<JavaFile> javaFiles = pack.getJavaFiles();
        assertEquals(2,javaFiles.size());
        JavaFile jf2 = javaFiles.get(0);
        assertEquals("Main.java",jf2.getName());

        jf = javaFiles.get(1);
        ur = jf.getMetric(UsagesMetric.class);
        cvs = jf.getCVSResultMetric();

        storage =  ur.getStorage();
        items = storage.getAllObjects();

        sortedItems.clear();
        sortedItems.addAll(items);
        Collections.sort(sortedItems, new PIRComparator());
         
        it = sortedItems.iterator();
        assertUsage(it,"1.1","1.1","java.security.Policy","Policy()","",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.Policy",null,"",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.Permissions","Permissions()","",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.PermissionCollection","add(java.security.Permission)","permissions",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.AllPermission","AllPermission()","",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.PermissionCollection","setReadOnly()","permissions",ur,cvs);
        assertUsage(it,"1.1","1.1","usagemetrics.RuntimePolicy","getAllPermissionCollection()","",ur,cvs);
        assertUsage(it,"1.1","1.1","usagemetrics.RuntimePolicy","getPermissions(java.security.CodeSource)","",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.PermissionCollection","implies(java.security.Permission)","getPermissions(domain.getCodeSource())",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.ProtectionDomain","getCodeSource()","domain",ur,cvs);
        assertUsage(it,"1.1","1.1","usagemetrics.RuntimePolicy","getPermissions(java.security.CodeSource)","",ur,cvs);
        assertUsage(it,"1.1","1.1","java.security.ProtectionDomain","getCodeSource()","domain",ur,cvs);
        assertFalse(it.hasNext());
    }    
    
    public void testCompileAndUsagesNoCVS() throws IOException, IOException, InterruptedException {
        String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
        ExamplesSetup.updateFile(filePath, "1.3");
        TestUtil.deleteAllCvsFolder(testPrjF);
        runJavac(testPrjF);          
        File javaMainFile = new File(testPrjF,"src/usagemetrics/Main.java");
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        assertNull(jf.getCVSResultMetric());    
        assertNull(jf.getCVSVersion());
    }
    
/* 
Index: Main.java
===================================================================
RCS file: /home/pzajac/vyvoj/cvs/pantexamples/usagemetrics/src/usagemetrics/Main.java,v
retrieving revision 1.2
retrieving revision 1.3
diff -r1.2 -r1.3
31c31
<         list.add("aaa");
---
>         list.add("bbb");

Index: Main.java
===================================================================
RCS file: /home/pzajac/vyvoj/cvs/pantexamples/usagemetrics/src/usagemetrics/Main.java,v
retrieving revision 1.1
retrieving revision 1.2
diff -r1.1 -r1.2
31a32
>         System.out.println("ahoj");

     
*/
    public void testJavacForManyVersions() throws IOException, InterruptedException {
        String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
        ExamplesSetup.updateFile(filePath, "1.1");
        runJavac(testPrjF);
        ExamplesSetup.updateFile(filePath, "1.2");
        runJavac(testPrjF);
        ExamplesSetup.updateFile(filePath, "1.3");
        runJavac(testPrjF);

        File javaMainFile = new File(testPrjF,"src/usagemetrics/Main.java");
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        UsagesMetric ur = jf.getMetric(UsagesMetric.class);
        PositionVersionIntervalResultContainer<UsageItem> storage =  ur.getStorage();
        Set<PositionIntervalResult<UsageItem>> items = storage.getAllObjects();
        CVSMetric cvs = JavaFileUtil.getCVSResultMetric(jf);
        List<PositionIntervalResult<UsageItem>> sortedItems = new ArrayList<PositionIntervalResult<UsageItem>>();
        sortedItems.addAll(items);
        Collections.sort(sortedItems, new PIRComparator());
        
        Iterator<PositionIntervalResult<UsageItem>> it = sortedItems.iterator();
    
        assertUsage(it,"1.3","1.3","java.util.List","add(E)","list",ur,cvs); 
        assertUsage(it,"1.2","1.3","java.io.PrintStream","println(java.lang.String)","System.out",ur,cvs); 
        assertUsage(it,"1.1","1.3","java.lang.Object","Object()","",ur,cvs); 
        assertUsage(it,"1.1","1.3","java.util.ArrayList","ArrayList()","",ur,cvs);
        assertUsage(it,"1.1","1.2","java.util.List","add(E)","list",ur,cvs); 
        assertFalse(it.hasNext());
        
        // test addded, removed and unchanged result
        Version v12 = cvs.getRootVersion().getVersion("1.2");
        Set<PositionIntervalResult<UsageItem>> addedPIR = storage.getAddedPIR(v12);
        assertEquals("only one was added in revision 1.2",1,addedPIR.size());
        it = addedPIR.iterator();
        PositionIntervalResult<UsageItem> pir = it.next();
        UsageItem ui = pir.getObject();
        assertEquals("java.io.PrintStream", ui.getClazz());

    
        // test unchanged
        Set<PositionIntervalResult<UsageItem>> unchangedPIR = storage.getUnchangedPIR(v12);
        assertEquals("unchanged usages in v1.2",3,unchangedPIR.size());

        // test removed
        Version v13 = cvs.getRootVersion().getVersion("1.3");
        Set<PositionIntervalResult<UsageItem>> removedPIR = storage.getRemovedPIR(v13);
        assertEquals("only one was removed in revision 1.3",1,removedPIR.size());
        it = removedPIR.iterator();
        pir = it.next();
        ui = pir.getObject();
        assertEquals("java.util.List", ui.getClazz());
        
        Version v11 = cvs.getRootVersion().getVersion("1.1");
        removedPIR = storage.getRemovedPIR(v11);
        assertTrue(removedPIR.isEmpty());
        
        unchangedPIR = storage.getUnchangedPIR(v11);
        assertTrue(unchangedPIR.isEmpty());
        
        
   }
//    public void testMoreFiles() throws IOException, InterruptedException {
//       testPrjF = ExamplesSetup.getPantProject();
//       System.out.println(testPrjF.getAbsolutePath());
//       runJavac();
//    }
    

    static class InvalidMetricBuilder implements MetricBuilder {
        public String getName() {
            return "InvalidMetricBuilder";
        }
    
        public String getDescription() {
            return "InvalidMetricBuilder";
        }

        public void visit( Element e ) {
            throw new RuntimeException("Correct exception");
        }

        public boolean canProcessTheSameRevision() {
            return true;
        }

    }
    public void testLogFile() throws IOException, InterruptedException {
        File folder =  TestUtil.getTmpFolder(getClass().getName());
        File logFile = new File(folder,"testLogFile.log");
        System.setProperty(MeasuringJavac.LOG_FILE_SYSTEM_PROPERTY_NAME, folder.getAbsolutePath() + File.separator + "testLogFile.log");
        logFile.delete();
        MetricsRunner.initialize();
        MetricsRunner.clearMetrics();
        MetricsRunner.addMetric(new InvalidMetricBuilder());
        String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
        ExamplesSetup.updateFile(filePath, "1.3");
        TestUtil.enableParentHandler(false);
        try {
            runJavac(testPrjF);
        } finally {
            TestUtil.enableParentHandler(true);
        }
        assertTrue("log file exists",logFile.exists());
        assertTrue("log file is not empty", logFile.length() > 0 );
    }
    private void assertUsage(Iterator<PositionIntervalResult<UsageItem>> it,String fromVersion,String toVersion,String className,String methodName,String content,UsagesMetric ur,CVSMetric cvs) {
        PositionVersionIntervalResultContainer<UsageItem> storage =  ur.getStorage();
        PositionIntervalResult<UsageItem> item = it.next();
        VersionInterval vers = storage.get(item);
        assertEquals(fromVersion,vers.getFrom().getRevision());
        assertEquals(toVersion,vers.getTo().getRevision());
        assertEquals(className,item.getObject().getClazz());
        assertEquals(methodName, item.getObject().getMethod());
        PositionInterval i = item.getInterval();
        assertEquals(content,cvs.getContent(i.getStartPosition(),i.getEndPosition()));
    } 
//        Writer fw = new FileWriter("/tmp/UsagesResult.golden");
//        try {
//            generateTest(fw,ur,jf);
//        } finally {
//            fw.close();
//        }
//    

    private void generateTest(Writer writer,UsagesMetric ur,JavaFile jf,Collection<PositionIntervalResult<UsageItem>> items) {

        PrintWriter pw = new PrintWriter(writer);
        
        PositionVersionIntervalResultContainer<UsageItem> storage =  ur.getStorage();
        
        CVSMetric cvs = JavaFileUtil.getCVSResultMetric(jf);
        for (PositionIntervalResult<UsageItem> item : items) {
            VersionInterval vers = storage.get(item);

            vers = storage.get(item);        
            PositionInterval i = item.getInterval();
            pw.print("assertUsage(it,\"" + vers.getFrom().getRevision() + "\",\""); 
            pw.print(vers.getTo().getRevision() + "\",\"");
            pw.print(item.getObject().getClazz() + "\",\"");
            pw.print(item.getObject().getMethod() + "\",\"");
            pw.println(cvs.getContent(i.getStartPosition(),i.getEndPosition()) + "\",ur,cvs); "); 
        }
        pw.println("assertFalse(it.hasNext());");
    }

    private static class PIRComparator implements Comparator<PositionIntervalResult<UsageItem>> {
        public int compare(PositionIntervalResult<UsageItem> pir1,
                           PositionIntervalResult<UsageItem> pir2) {
            int result = pir1.getInterval().compareTo(pir2.getInterval()); 
            if (result == 0) {
                result = pir2.hashCode() - pir1.hashCode();
            }
            return result;
        }
    }
}
