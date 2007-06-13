/*
 * PolynomialLinearReagression.java
 * 
 * Created on Jun 8, 2007, 11:43:51 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public final class PolynomialLinearRegression {
    private int deg;
    private Vector xCoords;
    private Vector yCoords;
    // parameters
    private Vector b;

    public PolynomialLinearRegression(int deg, Vector xCoords, Vector yCoords) {
        this.deg = deg;
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        
        // comute 
        DenseMatrix matrixA = new DenseMatrix(xCoords.size(),deg + 1);
  //      DenseVector vecB = new DenseVector(xCoords.size());
        for (int row = 0 ; row < xCoords.size() ;  row++ ) {
            matrixA.set(row, 0, 1.0);
            double x = xCoords.get(row);
            double xx = x;
            for (int col = 1 ; col < deg + 1  ; col++) {
                matrixA.set(row,col,xx);
                xx *= x; 
            }
        }
        b = LinearRegression.solve(matrixA, yCoords);
    }
    
    public double getAproximatedValue(double x) {
        double y = 0 ;
        double xx  = 1;
        for (int i = 0 ; i < b.size() ; i++) {
            y += xx*b.get(i);
            xx *= x;
        }
        return y;
    }
    
    /** don't modify the returned value
     * @return return array of coefs (b0 + b1x^2 + b2x^3 ...)
     */
    public Vector getCoefs() {
        return b;
    }
  public double getDeterminanceIndex() {
        double modelSum = 0;
        double realSum = 0;
        double yAvg = LinearRegression.avg(yCoords);
        for (int i = 0 ; i < yCoords.size() ; i++) {
           double mi = getAproximatedValue(xCoords.get(i)) - yAvg;
           modelSum += mi*mi; 
           double mr = yCoords.get(i) - yAvg;
           realSum += mr*mr;
        }
        return modelSum/realSum;
    }
}
