/*
 * LinearRegressionTest.java
 * JUnit based test
 *
 * Created on March 5, 2007, 7:20 PM
 */

package org.codeviation.math;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public class LinearRegressionTest extends TestCase {
    
    public LinearRegressionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSolve() {
        Matrix mat = new DenseMatrix(new double[][] {
            {1,1},
            {2,1},
            {3,1}});
        Vector vec = new DenseVector(new double[] {0,4,4});
        Vector res = LinearRegression.solve(mat, vec);
        assertEquals("vector size",2,res.size());
        assertEquals(2,res.get(0),1e-8);
        assertEquals(-4.0/3.0,res.get(1),1e-8);
       
    }
    
    public void testSolve2() {
        Vector xVec = new DenseVector(new double[]{7,  6, 5,  1, 5, 4,  7, 3, 4, 2,  8, 5, 2, 5, 7,  1, 4,5});
        Vector yVec = new DenseVector(new double[]{97,86, 78,10,75,62,101,39,53,33,118,65,25,71,105,17,49,68});
        double ret[] = LinearRegression.solve(xVec, yVec);
        assertEquals(-2.3221476510067163,ret[0],1e-8);
        assertEquals(14.738255033557047,ret[1],1e-8);
    }

}
