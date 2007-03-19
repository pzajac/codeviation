
package org.codeviation.javac;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Set;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author Petr Zajac
 */
public class CountsMetricTest extends TestCase {
    private File testPrjF;
    
    /** Creates a new instance of CountsMetricTest */
    public CountsMetricTest(String name) {
        super(name);
    }
    protected void setUp() {
        TestUtil.clearCache();
        TestUtil.enableMetricLogger();
        testPrjF = ExamplesSetup.getUsageMetricsProjectDir();
        MetricsRunner.clearMetrics();
        MetricsRunner.addMetric(new CountsBuilder());
    }
    
    public void testSimple() throws IOException, InterruptedException {
       String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
       System.out.println(System.getProperty(PersistenceManager.PANT_CACHE_FOLDER));
       ExamplesSetup.updateFile(filePath, "1.3");
       System.out.println("updated: 1.3");
        RunJavacTest.runJavac(testPrjF);          
        File javaMainFile = new File(testPrjF,"src/usagemetrics/Main.java");
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        CountsMetric metric = jf.getMetric(CountsMetric.class);
        assertNotNull(metric);
        
         assertEquals("versions.size() = 1" , 1, metric.getVersions().size());
        CountsItem cm = null;
        Version v = jf.getCVSResultMetric().getRootVersion();

        // 1.3
        cm =  metric.get(v.getVersion("1.3"));
        assertEquals("annotations",0,cm.getAnnotations());
        assertEquals("classes",1,cm.getClasses());
        assertEquals("constructors",1,cm.getConstructors());
        assertEquals("enums",0,cm.getEnums());
        assertEquals("fields",0,cm.getFields());
        assertEquals("interfances",0,cm.getInterfaces());
        assertEquals("methods",1,cm.getMethods());
        assertEquals("parameters",1,cm.getParameters());
        assertEquals("variables",0,cm.getVariables());
        assertEquals("staticInits",0,cm.getStaticInits());
        assertEquals("exceptionParameters",0,cm.getExceptionParameters());
        assertEquals("enumConstants",0,cm.getEnumConstants());

        
//        PrintWriter pw = new PrintWriter(new File("/tmp/counts.txt"));
//        generateAssert(metric,pw);
//        pw.close();
    }
    
    public void testManyFilesAndSrcCvsTags() throws IOException, InterruptedException {
        File javaMainFile = new File(testPrjF,"src/usagemetrics/Main.java");
        String filePath = "pantexamples/usagemetrics/src/usagemetrics/Main.java";
        ExamplesSetup.updateFile(filePath, "1.1");
        System.setProperty(MeasuringJavac.CVS_TAG_PROP_NAME, "1");
        RunJavacTest.runJavac(testPrjF);
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        Set<String> tags = jf.getPackage().getSourceRoot().getCvsTags();
        assertEquals("Tags size:",1,tags.size());
        ExamplesSetup.updateFile(filePath, "1.2");
        System.setProperty(MeasuringJavac.CVS_TAG_PROP_NAME, "2");
        RunJavacTest.runJavac(testPrjF);
        ExamplesSetup.updateFile(filePath, "1.3");
        System.setProperty(MeasuringJavac.CVS_TAG_PROP_NAME, "3");
        RunJavacTest.runJavac(testPrjF);
        ExamplesSetup.updateFile(filePath, "1.5");
        System.getProperties().remove(MeasuringJavac.CVS_TAG_PROP_NAME);
        RunJavacTest.runJavac(testPrjF);
        CVSMetric cvsm = jf.getCVSResultMetric();
        
        jf = JavaFile.getJavaFile(javaMainFile, "usagemetrics");
        
        Version rootV = cvsm.getRootVersion(); 
        Version v14 = rootV.getVersion("1.4");
        Version v15 = rootV.getVersion("1.5");
        assertSame("must be v14",v14,cvsm.getVersion(v14.getDate()));
        Date date = new Date((v14.getDate().getTime() + v14.getDate().getTime())/2);
        assertSame("must be v14",v14,cvsm.getVersion(date));
        assertSame("must be v15",v15,cvsm.getVersion(v15.getDate()));

        CVSVersionsByPant cvsVByPant = jf.getMetric(CVSVersionsByPant.class);
        Version vTag3 = cvsVByPant.getVersion("3");
        assertNotNull(vTag3);
        assertEquals("1.3",vTag3.getRevision());
      
        // test src cvs tags
        SourceRoot srcRoot = jf.getPackage().getSourceRoot(); 
        tags = srcRoot.getCvsTags();
        assertEquals("Tags size:",3,tags.size());
        assertTrue("contains '1' tag",tags.contains("1"));
        assertTrue("contains '2' tag",tags.contains("2"));
        assertTrue("contains '3' tag",tags.contains("3"));
        
        tags = srcRoot.getRepository().getAllTags();
        assertEquals("Tags size:",3,tags.size());
        assertTrue("contains '1' tag",tags.contains("1"));
        assertTrue("contains '2' tag",tags.contains("2"));
        assertTrue("contains '3' tag",tags.contains("3"));
        
        CountsMetric metric = jf.getMetric(CountsMetric.class);
        assertNotNull(metric);
        
        assertEquals("versions.size() = 4" , 4, metric.getVersions().size());
        CountsItem cm = null;
        Version v = jf.getCVSResultMetric().getRootVersion();

        // 1.5
        cm =  metric.get(v.getVersion("1.5"));
        assertEquals("annotations",1,cm.getAnnotations());
        assertEquals("classes",1,cm.getClasses());
        assertEquals("constructors",2,cm.getConstructors());
        assertEquals("enums",1,cm.getEnums());
        assertEquals("fields",3,cm.getFields());
        assertEquals("interfances",1,cm.getInterfaces());
        assertEquals("methods",4,cm.getMethods());
        assertEquals("parameters",2,cm.getParameters());
        assertEquals("variables",0,cm.getVariables());
        assertEquals("staticInits",0,cm.getStaticInits());
        assertEquals("exceptionParameters",0,cm.getExceptionParameters());
        assertEquals("enumConstants",3,cm.getEnumConstants());

        // 1.1
        cm =  metric.get(v.getVersion("1.1"));
        assertEquals("annotations",0,cm.getAnnotations());
        assertEquals("classes",1,cm.getClasses());
        assertEquals("constructors",1,cm.getConstructors());
        assertEquals("enums",0,cm.getEnums());
        assertEquals("fields",0,cm.getFields());
        assertEquals("interfances",0,cm.getInterfaces());
        assertEquals("methods",1,cm.getMethods());
        assertEquals("parameters",1,cm.getParameters());
        assertEquals("variables",0,cm.getVariables());
        assertEquals("staticInits",0,cm.getStaticInits());
        assertEquals("exceptionParameters",0,cm.getExceptionParameters());
        assertEquals("enumConstants",0,cm.getEnumConstants());

        // 1.3
        cm =  metric.get(v.getVersion("1.3"));
        assertEquals("annotations",0,cm.getAnnotations());
        assertEquals("classes",1,cm.getClasses());
        assertEquals("constructors",1,cm.getConstructors());
        assertEquals("enums",0,cm.getEnums());
        assertEquals("fields",0,cm.getFields());
        assertEquals("interfances",0,cm.getInterfaces());
        assertEquals("methods",1,cm.getMethods());
        assertEquals("parameters",1,cm.getParameters());
        assertEquals("variables",0,cm.getVariables());
        assertEquals("staticInits",0,cm.getStaticInits());
        assertEquals("exceptionParameters",0,cm.getExceptionParameters());
        assertEquals("enumConstants",0,cm.getEnumConstants());

        // 1.2
        cm =  metric.get(v.getVersion("1.2"));
        assertEquals("annotations",0,cm.getAnnotations());
        assertEquals("classes",1,cm.getClasses());
        assertEquals("constructors",1,cm.getConstructors());
        assertEquals("enums",0,cm.getEnums());
        assertEquals("fields",0,cm.getFields());
        assertEquals("interfances",0,cm.getInterfaces());
        assertEquals("methods",1,cm.getMethods());
        assertEquals("parameters",1,cm.getParameters());
        assertEquals("variables",0,cm.getVariables());
        assertEquals("staticInits",0,cm.getStaticInits());
        assertEquals("exceptionParameters",0,cm.getExceptionParameters());
        assertEquals("enumConstants",0,cm.getEnumConstants());

        
//        PrintWriter pw = new PrintWriter(new File("/tmp/counts2.txt"));
//        generateAssert(metric,pw);
//        pw.close();
    }
    
    private void generateAssert(CountsMetric metric,PrintWriter pw) {
        Set<Version> versions = metric.getVersions();
        int vSize = versions.size();
        pw.println("assertEquals(\"versions.size() = " + vSize + "\" , "  + vSize + ", metric.getVersions().size());"); 
        pw.println("CountsItem cm = null;");
        pw.println("Version v = jf.getCVSResultMetric().getRootVersion();");
        for (Version v : versions) {
            generateMetricsItemAssert(metric,v,pw);
        }
    }
    
    private void generateMetricsItemAssert(CountsMetric metric,Version v,PrintWriter pw) {
       CountsItem cm =  metric.get(v);
       pw.println("\n// " + v.getRevision() );
       pw.println("cm =  metric.get(v.getVersion(\"" + v.getRevision() +  "\"));");
       pw.println("assertEquals(\"annotations\"," + cm.getAnnotations() + ",cm.getAnnotations());");
       pw.println("assertEquals(\"classes\"," + cm.getClasses()+ ",cm.getClasses());");
       pw.println("assertEquals(\"constructors\"," + cm.getConstructors()+ ",cm.getConstructors());");
       pw.println("assertEquals(\"enums\"," + cm.getEnums()+ ",cm.getEnums());");
       pw.println("assertEquals(\"fields\"," + cm.getFields()+ ",cm.getFields());");
       pw.println("assertEquals(\"interfances\"," + cm.getInterfaces()+ ",cm.getInterfaces());");
       pw.println("assertEquals(\"methods\"," + cm.getMethods()+ ",cm.getMethods());");
       pw.println("assertEquals(\"parameters\"," + cm.getParameters()+ ",cm.getParameters());");
       pw.println("assertEquals(\"variables\"," + cm.getVariables()+ ",cm.getVariables());");
       pw.println("assertEquals(\"staticInits\"," + cm.getStaticInits()+ ",cm.getStaticInits());");
       pw.println("assertEquals(\"exceptionParameters\"," + cm.getExceptionParameters()+ ",cm.getExceptionParameters());");
       pw.println("assertEquals(\"enumConstants\"," + cm.getEnumConstants()+ ",cm.getEnumConstants());");
    }
}
