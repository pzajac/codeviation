package org.codeviation.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import junit.framework.TestCase;
import no.uib.cipr.matrix.NotConvergedException;
import org.codeviation.javac.UsageItem;
import org.codeviation.math.AnotatedMatrix;
import org.codeviation.math.LSI;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;
import org.codeviation.tasks.RepositoryProcessEnv.Event;

/**
 *
 * @author pzajac
 */
public class UsageOwnerIndexerTest extends TestCase {
    
    public UsageOwnerIndexerTest(String testName) {
        super(testName);
    }
    
    public void test1() throws NotConvergedException, IOException {
        ExamplesSetup.initNbCvsTestingCache();
        final UsageOwnerIndexer indexer = new UsageOwnerIndexer();
        indexer.setOutDir(TestUtil.getTmpFolder("usagemetricsIndexer"));
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        RepositoryProcessEnv env = new RepositoryProcessEnv();
        env.setWorkDir(TestUtil.getTmpFolder("UsageOwnerIndexer"));
        
        final AnotatedMatrix[] am = new AnotatedMatrix[1];
        env.addListener(new RepositoryProcessEnv.ProcessListener<AnotatedMatrix<String,ArrayList<UsageItem>>>(){

            public void actionPerformed(RepositoryProcess process, Event<AnotatedMatrix<String, ArrayList<UsageItem>>> event) {
                if (process == indexer) {
                    am[0] = event.getParam();
                }
            }
            
        });
        //UsageOwnerIndexer.setOutMatrix(matrix);
        env.setSourceRootFilter(new SourceRootFilter() {
            public boolean accept(SourceRoot srcRoot) {
 //                 return true;
                return (srcRoot.getRelPath().startsWith("core")) ;
            }
       });
       indexer.execute(rep,env);    
       assertTrue("non empty matrix file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME).length() > 0);
       assertTrue("non empty matrix's rows file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME + ".rows").length() > 0);
       assertTrue("non empty matrix's columns file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME + ".columns").length() > 0);
       
       
       // try svd
        @SuppressWarnings("unchecked")
        AnotatedMatrix<String,ArrayList<String>> matrix = am[0];
       System.out.println(matrix.getRows().size());
       for(String row : matrix.getRows()) {
           System.out.println(row);
       }
       ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/tmp/develmatrix.ser"));
       oos.writeObject(am[0]);
       oos.close();       
       LSI<String,ArrayList<String>> lsi = new LSI<String,ArrayList<String>>(matrix);
 
       for (double s : lsi.getSvd().getS() ) {
          System.out.println(s);
       }

       lsi.plotSVD(matrix.getRows());
    }
  
}
