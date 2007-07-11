/*
 * MatrixUtilTest.java
 * 
 * Created on Jul 10, 2007, 7:49:35 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

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
}
