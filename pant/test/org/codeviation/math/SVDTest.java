/*
 * SVDTest.java
 * JUnit based test
 *
 * Created on July 10, 2007, 6:01 PM
 */

package org.codeviation.math;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.codeviation.javac.UsageItem;

/**
 *
 * @author pzajac
 */
public class SVDTest extends TestCase {
    
    public SVDTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void computeA(FlexCompRowMatrix mat, DenseMatrix vt, DenseMatrix u, DenseVector s) {
        mat = new FlexCompRowMatrix(mat);
        DenseMatrix ss = new DenseMatrix(s.size(), u.numColumns());
        for (int i = 0; i < s.size(); i++) {
            ss.set(i, i, s.get(i));
        }
        DenseMatrix uss = new DenseMatrix(u.numRows(), ss.numColumns());
        u.mult(ss, uss);
        Matrix ret = new DenseMatrix(uss.numRows(), vt.numColumns());
        uss.mult(vt, ret);

        ret.scale(-1);
        Matrix b = new DenseMatrix(ret.numColumns(), ret.numRows());
        ret.transpose(b);

        mat.add(b);
        assertZeroMatrix(mat, 1.0E-6);
    }

    
//    public void testExampleHelsinki() throws NotConvergedException {
//        FlexCompRowMatrix mat = org.codeviation.math.LSITest.createExampleHelsinkiMatrix();
//        SVD svd = SVD.factorize(mat, 3);
//        DenseVector s = svd.getS();
//        System.out.println("s = " + s );
//         LogsUtil.printMatrix(svd.getV());
//  //      LogsUtil.printMatrix(svd.getU());
//    }

    
    private FlexCompRowMatrix createMatrix() {
        FlexCompRowMatrix mat = new FlexCompRowMatrix(2,3);
        mat.set(0, 0, 1.);
        mat.set(0, 2, 2.);
        mat.set(1, 1, 3.);
        mat.set(1, 0, 4.);
        mat.set(1, 2, 5.);
        return mat;
    }
    public void testSimple() throws NotConvergedException {
        FlexCompRowMatrix mat = createMatrix();
       
        SVD svd = SVD.factorize(mat, 2);
        DenseVector s = svd.getS();
        DenseMatrix vt = svd.getVt();
        DenseMatrix u = svd.getU();
        DenseMatrix ut = new DenseMatrix(u.numColumns(),u.numRows());
        checkOrtogonality(vt);
        u.transpose(ut);
        checkOrtogonality(ut);
        computeA(mat,vt, u, s);    
    }

    public void testSimpleRanked() throws NotConvergedException {
        FlexCompRowMatrix mat = createMatrix();
//        System.out.print("A = ");
//        LogsUtil.printMatrix(mat);
       
        SVD svd = SVD.factorize(mat, 2);
        DenseVector s = svd.getS();
//        System.out.println("s = " + s );
        DenseMatrix vt = svd.getVt();
        DenseMatrix u = svd.getU();
        DenseMatrix ut = new DenseMatrix(u.numColumns(),u.numRows());
        checkOrtogonality(vt);
        u.transpose(ut);
        checkOrtogonality(vt);
    }
    
    public void testBigger() throws NotConvergedException {
        FlexCompRowMatrix mat = createBiggerMatrix();
//        System.out.print("A = ");
//        LogsUtil.printMatrix(mat);
       
        SVD svd = SVD.factorize(mat, 3);
        DenseVector s = svd.getS();
//        System.out.println("s = " + s );
        DenseMatrix vt = svd.getVt();
        DenseMatrix u = svd.getU();
//        System.out.print("u = ");
//        LogsUtil.printMatrix(u);
        DenseMatrix ut = new DenseMatrix(u.numColumns(),u.numRows());
        checkOrtogonality(vt);
        u.transpose(ut);
        checkOrtogonality(ut);
        computeA(mat,vt, u, s);    
        
    }
    
    public void testPackagesMatrix() throws NotConvergedException, IOException, ClassNotFoundException {
        FlexCompRowMatrix mat = getPackagesMatrix();
        
        LowerSymmDenseMatrix multAAt = org.codeviation.math.MatrixUtil.multAAt(mat);
        Matrix check = new DenseMatrix(multAAt).scale(-1.);
       
        check.rank1(mat);        
        assertEquals("norm = 0",0.0, check.norm(Matrix.Norm.Frobenius),1e-8 );
        System.out.println("Dim " + mat.numColumns() + "," + mat.numRows());
        SVD svd = SVD.factorize(mat, 159);
        DenseVector s = svd.getS();
        System.out.println(s.toString());
//        System.out.println("s = " + s );
        DenseMatrix vt = svd.getVt();
        DenseMatrix u = svd.getU();
//        System.out.print("u = ");
//        LogsUtil.printMatrix(u);
        DenseMatrix ut = new DenseMatrix(u.numColumns(),u.numRows());
        checkOrtogonality(vt);
        u.transpose(ut);
        checkOrtogonality(ut);
        computeA(mat,vt, u, s);    
    }
    public void testBiggerRanked() throws NotConvergedException {
        FlexCompRowMatrix mat = createBiggerMatrix();
       
        SVD svd = SVD.factorize(mat, 2);
        DenseVector s = svd.getS();
        assertEquals(2,s.size());
//        System.out.println("s = " + s );
        DenseMatrix vt = svd.getVt();
//        System.out.print("vr = ");
//        LogsUtil.printMatrix(vt);
        DenseMatrix u = svd.getU();
        DenseMatrix ut = new DenseMatrix(u.numColumns(),u.numRows());
//        checkOrtogonality(vt);
        u.transpose(ut);
        checkOrtogonality(ut);
//        computeA(mat,vt, u, s);    
    }
    private void assertZeroMatrix(FlexCompRowMatrix mat,double tol) {
        for (int r = 0 ; r < mat.numRows() ; r++) {
            for (int c = 0 ; c < mat.numColumns() ; c++) {
                double val = mat.get(r,c);
                assertEquals(" mat " + r + ", " + c + " : " + val, 0,val,tol);
            }
        }
    }
    
    
    private void checkOrtogonality(DenseMatrix u) {
        for (int i = 0 ; i < u.numRows() ; i++) {
            for (int j = 0 ; j < u.numRows() ; j++) {
                if (i != j) {
                    double val = 0.0;
                    for (int k = 0 ; k < u.numColumns() ;k++) {
                        val += u.get(i, k)*u.get(j,k);
                    }
                    assertEquals(0,val,1e-4);
                }
            }
        }
    }
    private FlexCompRowMatrix createBiggerMatrix() {
        FlexCompRowMatrix mat = new FlexCompRowMatrix(3,4);
        mat.set(0, 0, 1.);
        mat.set(0, 2, 2.);
        mat.set(0, 3, 4);
        mat.set(1, 0, 4.);
        mat.set(1, 1, 3.);
        mat.set(1, 2, 5.);
        mat.set(1, 3, 4.);
        mat.set(2, 0, 3.);
        mat.set(2, 1, 4.);
        mat.set(2, 2, 5.);
        mat.set(2, 3, 4.);
        return mat;
    }
    
    public void testTransposedMatrix() throws NotConvergedException {
        // transpose matrix
        FlexCompRowMatrix matt = createBiggerMatrix();
        FlexCompRowMatrix matrix = new FlexCompRowMatrix(matt.numColumns(), matt.numRows());
        for (MatrixEntry me : matt) {
            matrix.set(me.column(), me.row(), me.get());
        }

        SVD svd = SVD.factorize(matrix, 3);
        DenseVector s = svd.getS();
//        System.out.println("s = " + s );
        DenseMatrix vt = svd.getVt();
        DenseMatrix u = svd.getU();
//        System.out.print("u = ");
//        LogsUtil.printMatrix(u);
        DenseMatrix ut = new DenseMatrix(u.numColumns(), u.numRows());
        checkOrtogonality(vt);
        u.transpose(ut);
        checkOrtogonality(ut);

        SVD svd2 = SVD.factorize(matt, 3);
        System.out.println("u1:" );
        LogsUtil.printMatrix(u);
        System.out.println("u2:" );
        LogsUtil.printMatrix(svd2.getU());
        System.out.println("v1:" );
        LogsUtil.printMatrix(vt);
        System.out.println("v2:" );
        LogsUtil.printMatrix(svd2.getVt());

        computeA(matrix, vt, u, s);
    }

    private FlexCompRowMatrix getPackagesMatrix() throws IOException, ClassNotFoundException {
        File dataFolder = new File("/home/pzajac/skola/prakssvd/data");
        File file = new File(dataFolder, "matrix_package.ser");
        System.out.println("read");
        FileInputStream fis = new FileInputStream(file);
        try {
            ObjectInputStream ois = new ObjectInputStream(fis);
            try {
                @SuppressWarnings(value = "unchecked")
                AnotatedMatrix<String, ArrayList<UsageItem>> am = (AnotatedMatrix<String, ArrayList<UsageItem>>) ois.readObject();
                System.out.println("computing ...");
                return am.getMatrix();
            } finally {
                ois.close();
            }
        } finally {
            fis.close();
        }
    }
}
