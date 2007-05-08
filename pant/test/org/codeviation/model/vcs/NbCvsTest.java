
package org.codeviation.model.vcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.JavaFileUtil;
import org.codeviation.model.Line;
import org.codeviation.model.Package;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.PositionVersionIntervalResultContainer;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.Version;
import org.codeviation.javac.UsageItem;
import org.codeviation.javac.UsagesMetric;

/**
 *
 * @author pzajac
 */
public class NbCvsTest extends TestCase {
    
    /** Creates a new instance of NbCvsTest */
    public NbCvsTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        ExamplesSetup.initNbCvsTestingCache();
    }
    
    public void testApisupportJnlplauncherRuntimePolicy() throws IOException {
        JavaFile jf = getRuntimePolictJF();
        Version v = jf.getCVSVersion();
        assertNotNull("Test configuration, cvs cache doesn't contains file",v);
        List<Line> lines = jf.getLines(v);
        
        TestUtil.assertFile(lines, jf.getCVSFile());
    }
 
    public void testCoreProgress() throws IOException {
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        assertNotNull(rep);
        SourceRoot srcRoot = rep.getSourceRoot("core/progress/src");
        for (Package pack : srcRoot.getPackages() ) {
            for (JavaFile jf : pack.getJavaFiles()) {
                System.out.println(jf.getName());
                Version v = jf.getCVSVersion();
                if (v != null) {
                    if (jf.getDiffs().length > 0) {
                        List<Line> lines = jf.getLines(v);
                        TestUtil.assertFile(lines, jf.getCVSFile());
                    }
                }
            }
        }
    }
    private JavaFile getRuntimePolictJF() {
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        assertNotNull(rep);
        SourceRoot srcRoot = rep.getSourceRoot("apisupport/harness/jnlp-src");
        Package pack = srcRoot.getPackage("org.netbeans.modules.apisupport.jnlplauncher",true);
        return  pack.getJavaFile("RuntimePolicy.java");
    }
    
    public void testUsages() {
        JavaFile jf = getRuntimePolictJF();
        UsagesMetric um = jf.getMetric(UsagesMetric.class);
        CVSMetric cvs = jf.getCVSResultMetric();
        
        PositionVersionIntervalResultContainer<UsageItem> usages = um.getStorage();
        for ( PositionIntervalResult<UsageItem> result :  usages.getAllObjects()) {
           PositionInterval pi = result.getInterval();
           System.out.println(result.getObject().getClazz() + " . " + result.getObject().getMethod());
           System.out.println("l: " + pi.getStartPosition().getLine().getNewContent());
           System.out.println("c: " + cvs.getContent(pi.getStartPosition(), pi.getEndPosition()));
        }
    }
    
    public void testAntDebuggerSrc() throws IOException, ClassNotFoundException {
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        assertNotNull(rep);
        SourceRoot srcRoot = rep.getSourceRoot("ant/debugger/src");
        Package pack = srcRoot.getPackage("org.netbeans.modules.ant.debugger",true);
        JavaFile jf = pack.getJavaFile("ToolTipAnnotation.java");
        CVSMetric cvsm = jf.getCVSResultMetric();
        Version v = cvsm.getRootVersion();
        assertNotNull(v);
        JavaFileUtil.setCurrentJavaFile(jf);
        while (v != null) {
            List<Line> lines = jf.getLines(v);
            for (int i = 0 ; i < lines.size() ; i++) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos); 
                Line line = lines.get(i);
                line.writeRef(oos);
                oos.close();
                ByteArrayInputStream bis  = new ByteArrayInputStream(bos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bis);
                assertNotNull(Line.readRef(ois));
            }
            System.out.println(v.getRevision());
            v = v.getNext();
        }
    }
}
