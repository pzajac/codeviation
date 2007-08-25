/*
 * DevelopersFastSVDTest.java
 * JUnit based test
 *
 * Created on June 28, 2007, 9:53 PM
 */

package org.codeviation.math;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmDenseEVD;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.codeviation.tasks.UsageOwnerIndexerTest;

/**
 *
 * @author pzajac
 */
public class DevelopersFastSVDTest extends TestCase {
    long startTime ;
    public DevelopersFastSVDTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        startTime = System.currentTimeMillis();
    }

    public void testDevelopersMatrix() throws  IOException, ClassNotFoundException, NotConvergedException {
        FileInputStream fis = new FileInputStream(new File(UsageOwnerIndexerTest.getOutDir(),"develmatrix.ser"));
        ObjectInputStream ois = new ObjectInputStream(fis);
        AnotatedMatrix am = (AnotatedMatrix) ois.readObject();
        FlexCompRowMatrix tm = am.getMatrix();
        printTime();
        LowerSymmDenseMatrix multAAt = org.codeviation.math.MatrixUtil.multAAt(am.getMatrix());
        System.out.println("multTime:");
        printTime();
        int sum = 0;
        for (int i = 0 ; i < tm.numRows() ; i++) {
            sum += tm.getRow(i).getUsed();
        }
        System.out.println("used = " + sum/tm.numRows());
//        Matrix m  = am.getTransposedMatrix().getMatrix();
//       
//        
//        am = am.getTransposedMatrix();
//        System.out.println(am.getColumns().size());
//        System.out.println(am.getRows().size());
//        int siz = am.getColumns().size();
//        DenseMatrix res = new DenseMatrix(siz,siz);
//        printTime();
//        m.transAmult(m, res);
//        printTime();
//        
        System.out.println("-------");
        printTime();
        SymmDenseEVD sdevd = SymmDenseEVD.factorize(multAAt);
        double vals[] = sdevd.getEigenvalues();
        
        printTime();
        System.out.println("---");
//        for (int i = 0; i < vals.length; i++) {
//            System.out.println(vals[i]);
//        }
        DenseMatrix eigenvectors = sdevd.getEigenvectors();
        
//        am.getMatrix().rank1(arg0)
//        FlexCompRowMatrix matrix = am.getMatrix();
//        FastSVD svd = new FastSVD(matrix);
//        svd.factorize(matrix.numColumns(), matrix.numColumns()*2, 10);
    }

    private void printTime() {
        long time = System.currentTimeMillis() - startTime;
        System.out.println("time(ms): " + time);
    }
    
}
