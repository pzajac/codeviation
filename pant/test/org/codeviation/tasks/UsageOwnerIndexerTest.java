package org.codeviation.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import junit.framework.TestCase;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.codeviation.javac.UsageItem;
import org.codeviation.math.AnotatedMatrix;
import org.codeviation.math.LSI;
import org.codeviation.math.Matlab;
import org.codeviation.math.RowComparator;
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
    
    public static File getOutDir() {
        File file = new File (TestUtil.getWorkDir(),"fastsvdmatrix");
        file.mkdirs();
        return file;
    }
    public void test1() throws NotConvergedException, IOException {
        ExamplesSetup.initNbCvsTestingCache();

       File folder = getOutDir(); 
       assertTrue("folder exists",folder.isDirectory());
       genMatric(UsageOwnerIndexer.ColumnType.CLASS,folder,"matrix_class.ser","matrix_class.m");
       genMatric(UsageOwnerIndexer.ColumnType.PACKAGE,folder,"matrix_package.ser","matrix_package.m");
       genMatric(UsageOwnerIndexer.ColumnType.METHOD,folder,"matrix_method.ser","matrix_method.m");

       
//       LSI<String,ArrayList<String>> lsi = new LSI<String,ArrayList<String>>(matrix);
//       lsi.setRank(10);
//       lsi.setRowComparator(RowComparator.Type.EUCLIDIAN);
//       lsi.normalizeRows();
////       lsi.tfidf();
//       lsi.compute();
//       
//       for (double s : lsi.getSvd().getS() ) {
//          System.out.println(s);
//       }
//
//       lsi.plotSVD();
    }
    public AnotatedMatrix<String,ArrayList<String>> genMatric(UsageOwnerIndexer.ColumnType columnType,File folder,String serMatrixFile,String matlabMatrixFile ) throws IOException {
        final UsageOwnerIndexer indexer = new UsageOwnerIndexer();
        indexer.setColumnType(columnType);
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
                  return true;
 //               return (srcRoot.getRelPath().startsWith("core")) ;
            }
       });
       indexer.execute(rep,env);    
       assertTrue("non empty matrix file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME).length() > 0);
       assertTrue("non empty matrix's rows file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME + ".rows").length() > 0);
       assertTrue("non empty matrix's columns file",new File(indexer.getOutDir(),UsageOwnerIndexer.MATRIX_FILE_NAME + ".columns").length() > 0);

        @SuppressWarnings("unchecked")
       AnotatedMatrix<String,ArrayList<String>> matrix = am[0];
       
       ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(folder,serMatrixFile)));
       oos.writeObject(matrix);
       oos.close();      
       System.out.println("Columns: " + matrix.getMatrix().numRows());
       System.out.println("Rows:" + matrix.getMatrix().numColumns());
       Matlab.toMFile(matrix.getMatrix(), new File(folder,matlabMatrixFile));
       
       return  matrix;
    }
}
