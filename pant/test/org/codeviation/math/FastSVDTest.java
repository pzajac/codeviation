/*
 * FastSVDTest.java
 * JUnit based test
 *
 * Created on June 22, 2007, 8:51 PM
 */

package org.codeviation.math;

import java.util.Random;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author pzajac
 */
public class FastSVDTest extends TestCase {
    
    public FastSVDTest(String testName) {
        super(testName);
    }

    public void testSimple() throws NotConvergedException {
        int k = 20;
        FlexCompRowMatrix mat = new FlexCompRowMatrix(500,k);
        double val = 1;
        Random random = new Random(1003); 
        for (int i = 0; i < mat.numRows() ; i++) {
            for (int j = 0 ; j < mat.numColumns() ; j++) {
                mat.set(i,j,random.nextDouble());
            }
            random.setSeed(i*13);
        }
//        mat.set(0,1,0.1);
//        mat.set(1,0,0.2);
//        mat.set(2,1,0.1);
//        mat.set(2,0,0.1);
 //       LogsUtil.printMatrix(mat);

        FastSVD fsvd = new FastSVD(mat);
        fsvd.setRandom(random);
        DenseMatrix v = fsvd.factorize(k, 200,20);
//        DenseMatrix u = new DenseMatrix(1000,k);
//        mat.mult(v, u);
//
//        for (int i = 0 ; i < u.numColumns() ; i++) {
//            
//        }
   //     LogsUtil.printMatrix(v);
        
        System.out.println("----------------");
        SVD svd = SVD.factorize(mat);
 //       LogsUtil.printMatrix(svd.getVt());
        System.out.println("--");
        double s[] = svd.getS();
        for (int i = 0 ; i < s.length ; i++) {
            System.out.println(s[i]);
        }
        v.add(-1,svd.getVt().transpose());
        LogsUtil.printMatrix(v);
        System.out.println("------------");
        System.out.println("Error:" + v.norm(Matrix.Norm.Frobenius));
    }
    
    
}
