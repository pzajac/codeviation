
package org.codeviation.javac.impl.blocks;


import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import org.codeviation.model.JavaFile;
import org.codeviation.javac.MetricBuilder;
import org.codeviation.javac.MetricsRunner;

/**
 *
 * @author pzajac
 */
public class BlocksBuilder implements MetricBuilder {
    
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
      Tree tree = cut.getTypeDecls().iterator().next();
            
      BlocksVisitor visitor = new BlocksVisitor(MetricsRunner.getTask(),MetricsRunner.getCompilationUnitTree(),MetricsRunner.getFileManager(),MetricsRunner.getFileObject());
      JavaFile jf = MetricsRunner.getJavaFile(e);
      if (jf != null) {
          Blocks blocks = new Blocks(jf,e.toString());
          visitor.scan(new TreePath(cut), blocks);
          blocks.persists();
      }
    }    

    public boolean canProcessTheSameRevision() {
        return false;
    }
}
