
package org.codeviation.table;

import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.SourceRoot;

/**
 *
 * @author pzajac
 */
public interface TableConf {
    /** Invoked before fisr addJavaFile
     */
    void init();
    void addJavaFile(JavaFile jf);
    public void addSourceRoot(SourceRoot srcRoot) ;
    public void addPackage(Package pack) ;
     
    /** Get computed table. It is invoked after all getTable 
     */
    Table getTable() ;
   
    void clear();
}
