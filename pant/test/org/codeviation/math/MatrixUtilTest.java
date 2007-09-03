/*
 * MatrixUtilTest.java
 * 
 * Created on Jul 10, 2007, 7:49:35 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.codeviation.math.MatrixUtil.MatrixHeader;

/**
 *
 * @author pzajac
 */
public class MatrixUtilTest extends TestCase {

    public MatrixUtilTest(String name) {
        super(name);
    }

    public void testAAt() {
        FlexCompRowMatrix matA = new FlexCompRowMatrix(2,3);
        matA.set(0, 0, 1.);
        matA.set(0, 2, 2.);
        matA.set(1, 1, 3.);
        matA.set(1, 0, 4.);
        matA.set(1, 2, 5.);
        LowerSymmDenseMatrix multAAt = org.codeviation.math.MatrixUtil.multAAt(matA);
        Matrix check = new DenseMatrix(multAAt).scale(-1.);
       
        check.rank1(matA);        
        assertEquals("norm = 0",0.0, check.norm(Matrix.Norm.Frobenius),1e-8 );
    }
    
    public void testEmptyRow() {
        FlexCompRowMatrix matA = new FlexCompRowMatrix(2,3);
        matA.set(1, 1, 3.);
        matA.set(1, 0, 4.);
        matA.set(1, 2, 5.);
        LowerSymmDenseMatrix multAAt = org.codeviation.math.MatrixUtil.multAAt(matA);
        Matrix check = new DenseMatrix(multAAt).scale(-1.);
       
        check.rank1(matA);        
        assertEquals("norm = 0",0.0, check.norm(Matrix.Norm.Frobenius),1e-8 );
        
    }

    public void testPersistence() throws IOException {
        Matrix mat = new DenseMatrix(2,3);
        mat.set(0, 0, 1);
        mat.set(0, 1, 2);
        mat.set(1, 0, 3);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);
        
        MatrixUtil.writerMatrixHeader(mat, oos);
        MatrixUtil.writeMatrix(mat, oos);
        
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        MatrixHeader header = MatrixUtil.readMatrixHeader(ois);
        Matrix mat2 = new DenseMatrix (header.getNumRows(),header.getNumColumns());
        MatrixUtil.readMatrix(mat2, ois);
        Matrix addMat = mat.add(mat2.scale(-1.0));
        double norm = addMat.norm(Matrix.Norm.Frobenius);
        assertEquals("null norm", 0.0,norm, 1e-10);
        
    }
    

}
