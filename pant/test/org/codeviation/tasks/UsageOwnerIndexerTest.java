package org.codeviation.tasks;

import java.io.File;
import junit.framework.TestCase;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class UsageOwnerIndexerTest extends TestCase {
    
    public UsageOwnerIndexerTest(String testName) {
        super(testName);
    }
    
    public void test1() throws NotConvergedException {
        ExamplesSetup.initNbCvsTestingCache();
        UsageOwnerIndexer indexer = new UsageOwnerIndexer();
        indexer.setOutDir(TestUtil.getTmpFolder("usagemetricsIndexer"));
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        RepositoryProcessEnv env = new RepositoryProcessEnv();
        env.setWorkDir(TestUtil.getTmpFolder("UsageOwnerIndexer"));
        
        Matrix matrix[] = new Matrix[1];
        UsageOwnerIndexer.setOutMatrix(matrix);
        env.setSourceRootFilter(new SourceRootFilter() {
            public boolean accept(SourceRoot srcRoot) {
//                  return true;
                return (srcRoot.getRelPath().startsWith("clazz/src")) ;
            }
       });
       indexer.execute(rep,env);    
       assertTrue("non empty matrix file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME).length() > 0);
       assertTrue("non empty matrix's rows file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME + ".rows").length() > 0);
       assertTrue("non empty matrix's columns file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME + ".columns").length() > 0);
       
       // try svd
       SVD svd =  SVD.factorize(matrix[0]);
       for (double s : svd.getS() ) {
          System.out.println(s);
       }
    }
    
}
