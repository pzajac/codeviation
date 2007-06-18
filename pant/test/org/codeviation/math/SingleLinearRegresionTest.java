/*
 * SingleLinearRegresionTest.java
 * JUnit based test
 *
 * Created on June 7, 2007, 11:17 PM
 */

package org.codeviation.math;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public class SingleLinearRegresionTest extends TestCase {
    
    public SingleLinearRegresionTest(String testName) {
        super(testName);
    }

    
    public void testBrisStatistika() {
        Vector xVec = new DenseVector(new double[]{7,  6, 5,  1, 5, 4,  7, 3, 4, 2,  8, 5, 2, 5, 7,  1, 4,5});
        Vector yVec = new DenseVector(new double[]{97,86, 78,10,75,62,101,39,53,33,118,65,25,71,105,17,49,68});
        SingleLinearRegresion slr = new SingleLinearRegresion(xVec,yVec);
        assertEquals(-2.3221476510067163,slr.getB0(),1e-8);
        assertEquals(14.738255033557047,slr.getB1(),1e-8);
//        System.out.println(slr.getVarianceEstimation());
//        System.out.println(slr.getB0Variance());
//        System.out.println(slr.getB1Variance());
//        System.out.println(slr.getB0EstimatedError());
//        System.out.println(slr.getB1EstimatedError());
//        
//        System.out.println(slr.testB0(0, 0.05));
//        System.out.println(slr.testB1(0, 0.05));
    }
    
//    public void testX() {
//        TDistribution td = new TDistribution(18);
//        System.out.println("td" + td.cumulative(0.005)); 
//        System.out.println("td" + td.probability(0.005)); 
//        System.out.println("td" + td.inverse(1-0.005)); 
//    }
    
    public void test2() {
       Vector xVec = new DenseVector(new double[]{0.99,1.02,1.15,1.29,1.46,1.36,0.87,1.23,1.55,1.4,1.19,1.15,0.98,1.01,1.11,1.2,1.26,1.32,1.43,0.95});
       Vector yVec = new DenseVector(new double[]{90.01,89.05,91.43,93.74,96.73,94.45,87.59,91.77,99.42,93.65,93.54,92.52,90.56,89.54,89.85,90.39,93.25,93.41,94.98,87.33});
       
       SingleLinearRegresion slr = new SingleLinearRegresion(xVec,yVec);
       assertEquals(74.283,slr.getB0(),1e-3);
       assertEquals(14.947,slr.getB1(),1e-3);
     
       assertEquals(1.18, slr.getVarianceEstimation(),1e-2);
       assertEquals(1.593,slr.getB0EstimatedError(),1e-3);
       assertEquals(1.317,slr.getB1EstimatedError(),1e-3);
       assertFalse(slr.testB1(0, 0.01));
       assertEquals(0.877,slr.getDeterminanceIndex(),1e-3);
     }
}
