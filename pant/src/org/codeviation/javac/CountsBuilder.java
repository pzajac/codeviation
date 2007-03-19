
package org.codeviation.javac;

//import com.sun.source.tree.CompilationUnitTree;
import javax.lang.model.element.Element;
import javax.lang.model.util.ElementScanner6;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Version;

/**
 *
 * @author Petr Zajac, Petr Hrebejk
 */
public class CountsBuilder implements MetricBuilder {
    private Scanner scanner = new Scanner(); 

    public String getName() {
        return "Counter";
    }

    public String getDescription() {
        return "Counts various elements";
    }

    public void visit(Element e) {
        JavaFile jf = MetricsRunner.getJavaFile(e);
        if (jf != null) {
            Version version = jf.getCVSVersion();
            if (version != null) {
                CountsItem ci = new CountsItem();
                CountsMetric cm = jf.getMetric(CountsMetric.class);
                if (cm == null) {
                    cm = new CountsMetric();
                }
                scanner.c = ci;
                scanner.scan(e);
                cm.put(version,ci);
                jf.setMetric(cm);
            }
        }
    }
    
    
    private class Scanner extends ElementScanner6<Void,Void> {
        CountsItem c;
        public Void scan( Element e, Void v ) {
                        
            switch( e.getKind() ) {
                case CLASS:
                    c.incClasses();
                    break;
                case INTERFACE:
                    c.incInterfaces();
                    break;    
                case ENUM:
                    c.incEnums();
                    break;
                case ANNOTATION_TYPE:
                    c.incAnnotations();
                    break;
                case METHOD:
                    c.incMethods();
                    break;
                case CONSTRUCTOR:
                    c.incConstructors();
                    break;
                case FIELD:
                    c.incFields();
                    break;
                case PARAMETER:
                    c.incParameters();
                    break;    
                case LOCAL_VARIABLE:
                    c.incVariables();
                    break;
                case STATIC_INIT:
                    c.incStaticInits();
                    break;
                case ENUM_CONSTANT:
                    c.incEnumConstants();
                    break; 
                case EXCEPTION_PARAMETER:
                    c.incExceptionParameters();
                    break;
                     
            }
            return super.scan( e,v);
         }
    }       

    public boolean canProcessTheSameRevision() {
        return false;
    }
}
