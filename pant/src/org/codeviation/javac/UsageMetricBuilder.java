
package org.codeviation.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.Element;
import org.codeviation.model.JavaFile;
import org.codeviation.javac.MetricsRunner;
import org.codeviation.javac.UsagesVisitor;

/**
 *
 * @author pzajac
 */
public class UsageMetricBuilder implements MetricBuilder {
    /** Creates a new instance of Usage */
    public UsageMetricBuilder() {
    }
    
    public Object getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getName() {
        return "Usage metric";
    }
    
    public String getDescription() {
        return "Usage for all files";
    }
    
    public void visit(Element e) {
      CompilationUnitTree cut = MetricsRunner.getCompilationUnitTree();
//      String packageName = cut.getPackageName().toString();
      Tree tree = cut.getTypeDecls().iterator().next();
            
      // scan class
//          ClassTree ctree = (ClassTree)tree;
      UsagesVisitor visitor = new UsagesVisitor(MetricsRunner.getTask(),MetricsRunner.getCompilationUnitTree(),MetricsRunner.getFileManager(),MetricsRunner.getFileObject());
//          Usages usages = new Usages(JavaFile.getJavaFile(MetricsRunner.getFile(),packageName),e.toString());
      JavaFile jf = MetricsRunner.getJavaFile(e);
      if (jf != null) {
          Usages usages = new Usages(jf,e.toString());           
          visitor.scan(tree, usages);
          usages.log();
          usages.persists();
      }
    }    

    public boolean canProcessTheSameRevision() {
        return false;
    }
}
