
package org.codeviation.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.apache.tools.ant.taskdefs.Javac;
import org.codeviation.model.JavaFile;
import org.codeviation.javac.MetricBuilder;
import org.codeviation.model.SourceRoot;
import org.openide.util.Lookup;

/**
 * Executes MetricBuildders for java files.
 * @author phrebejk,pzajac
 */
public class MetricsRunner implements TaskListener {
    // changed by System.getProperty(CVS_TAG_PROP_NAME);
    static String cvsTag = null;
    static Logger logger = Logger.getLogger(MetricsRunner.class.getName());
    
    private static JavacTaskImpl task;
    private static TaskEvent event;
    
 
    private static JavaFileManager fileManager;    
    private static Javac attributes;
    private static boolean initialized ;    
    private static JavaFile lastJavaFile; 
    private static SourceRoot  lastSourceRoot; 
    
    private static List<MetricBuilder> metrics = new ArrayList<MetricBuilder>(); 
    
    public static CompilationUnitTree getCompilationUnitTree() {
        return event.getCompilationUnit();
    }
    
    public static void setFileManager(JavaFileManager jfm) {
        fileManager = jfm;
    }
    public static JavaFileManager getFileManager() {
        return fileManager;
    }
    
    public static JavaFileObject getFileObject() {
        return getCompilationUnitTree().getSourceFile();
    }
    
    public static JavaFile getJavaFile(Element e) {
        if (lastJavaFile == null) {
            CompilationUnitTree cut = MetricsRunner.getCompilationUnitTree();
            if (cut == null) {
                logger.severe("compilation unit tree is null for Element " + e + "and event " + event);
                return null;
            }
            String packageName = cut.getPackageName().toString();
            Tree tree = cut.getTypeDecls().iterator().next();
            if (tree.getKind() == Kind.CLASS) {
              // scan class
              return JavaFile.getJavaFile(MetricsRunner.getFile(),packageName); 
            }
        }
        return lastJavaFile;
    }
    
    public static File getFile() {
        try {
            // XXX getFileObject().toUri().toURL() throw IllegalArgumentException URI is not 
            // absolute, therefore such dirty workaround
            File f = new File(getFileObject().toUri().getPath());
            if (f.exists()) {
                return f;
            }
            return new File (getFileObject().toUri().toURL().getFile());
        } catch (MalformedURLException e) {
            MetricUtil.notify(e);
        }
        return null;
    }
    
    /** Creates a new instance of MetricsRunner */
    public MetricsRunner( Javac attributes ) {
        this.attributes = attributes;
    }
    
    public void started(TaskEvent e) {
    }

    public void finished(TaskEvent e) {
        if ( e.getKind() == TaskEvent.Kind.ANALYZE ) {
            this.event = e;
            try {
                // reset last javafile
                lastJavaFile = null;
                lastJavaFile = getJavaFile(e.getTypeElement());
                if (lastJavaFile != null) {
                    lastSourceRoot = lastJavaFile.getPackage().getSourceRoot();
                    lastSourceRoot.addCvsTag(cvsTag);
                    CVSVersionsByPant cvspant = lastJavaFile.getMetric(CVSVersionsByPant.class);
                    String version = lastJavaFile.getCVSVersionName();
                    if (version != null) {
                        boolean containsRev = cvspant.containsRevision(version);
                        boolean containsTag = cvspant.containsTag(cvsTag);
                        if (!containsTag && version != null) {
                            cvspant.addRevision(version,cvsTag);
                            lastJavaFile.setMetric(cvspant);
                        }
                        for (MetricBuilder metric : metrics) {
                            if (!containsRev || metric.canProcessTheSameRevision() ) {
                                metric.visit( e.getTypeElement() );
                            } else {
                                logger.fine("Already processed: " + lastJavaFile.getName() + " " + metric.getClass());
                            }
                        }
                    }
               } 
            } catch (Exception ex) {
                Logger.getLogger(MetricsRunner.class.getName()).log(Level.SEVERE,ex.getMessage(),ex);
            }
        }
    }
    
  
    public static void initialize() {
        if (!initialized) {
           metrics.clear();
           for (MetricBuilder builder : Lookup.getDefault().lookupAll(MetricBuilder.class)) {
               metrics.add(builder);
           }
           initialized = true;
        }
    }
    
    /** register new custom MetricBuilder and set metrics initialized
     */
    public static void addMetric(MetricBuilder builder) {
        metrics.add(builder);
        initialized = true;
    }
    /** Remove all registered MetricBuilder
     */
    public static void clearMetrics() {
        metrics.clear();
    }
    public static void setTask(JavacTaskImpl task) {
        MetricsRunner.task = task;
    }
    public static JavacTaskImpl getTask() {
        return task;
    }
    public static void saveResults() {
        for (MetricBuilder metric : metrics) {
            System.out.println( metric.getName() );
            System.out.println( metric );
        } 
    }

    public static SourceRoot getLastSourceRoot() {
        return lastSourceRoot;
    }

    public static void setLastSourceRoot(SourceRoot lastSourceRoot) {
        MetricsRunner.lastSourceRoot = lastSourceRoot;
    }
    
    static void initCvsTag() {
        MetricsRunner.cvsTag = System.getProperty(MeasuringJavac.CVS_TAG_PROP_NAME);
    } 
}
