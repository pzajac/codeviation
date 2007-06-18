/*
 * PolynomialLinearRegresionTest.java
 * 
 * Created on Jun 8, 2007, 2:00:31 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public class PolynomialLinearRegressionTest extends TestCase{

    public PolynomialLinearRegressionTest(String name) {
        super(name);
    }
    
    /** test y = x*x + 1
     */ 
    public void testXXPlus1() {
        Vector xCoords = new DenseVector(new double[]{0,1,2,3});
        Vector yCoords = new DenseVector(new double[]{1,2,5,10});
        PolynomialLinearRegression plr = new PolynomialLinearRegression(2,xCoords,yCoords);
        assertEquals(1,plr.getAproximatedValue(0),1e-8);
        assertEquals(2,plr.getAproximatedValue(1),1e-8);
    }
    
    public void testSingleLinear() {
        Vector xVec = new DenseVector(new double[]{7,  6, 5,  1, 5, 4,  7, 3, 4, 2,  8, 5, 2, 5, 7,  1, 4,5});
        Vector yVec = new DenseVector(new double[]{97,86, 78,10,75,62,101,39,53,33,118,65,25,71,105,17,49,68});
        PolynomialLinearRegression plr = new PolynomialLinearRegression(1,xVec,yVec);
        Vector coefs = plr.getCoefs();
        assertEquals(-2.3221476510067163,coefs.get(0),1e-8);
        assertEquals(14.738255033557047,coefs.get(1),1e-8);
        plr.getDeterminanceIndex();
        plr = new PolynomialLinearRegression(2,xVec,yVec);
        plr.getDeterminanceIndex();
//  
    }
    
}
