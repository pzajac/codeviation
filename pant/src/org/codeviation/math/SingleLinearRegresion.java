/*
 * SingleLinearRegresion.java
 * 
 * Created on Jun 7, 2007, 9:48:54 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import JSci.maths.statistics.TDistribution;
import no.uib.cipr.matrix.Vector;

/**
 * Book: Applied statistics and probability for engineering.
 * Chapter 11
 * 
 * y = b0 + b1*x
 * @author pzajac
 */
public class SingleLinearRegresion {
    Vector xCoords;
    Vector yCoords;
    double b0;
    double b1;
    
    
    public SingleLinearRegresion(Vector xCoords, Vector yCoords) {
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        if (xCoords.size() != yCoords.size() || xCoords.size() < 3) {
            throw new IllegalArgumentException("Invalid arguments:" + xCoords.size() + "," + yCoords.size());
        }
        double vals[] = LinearRegression.solve(xCoords, yCoords);
        b0 = vals[0];
        b1 = vals[1];
    }
    /** return SSE
     */
    public double getResiduumSquare() {
        return LinearRegression.residuumSquareSum(xCoords, yCoords, b0, b1);
    }
    
    /** @return estimation of sigma^2 (also SR^2)
     */ 
    public double getVarianceEstimation() {
       return getResiduumSquare()/(xCoords.size() - 2); 
    }

    public double getB0() {
        return b0;
    }

    public double getB1() {
        return b1;
    }
    

    public double getSxx() {
        double Xavg = LinearRegression.avg(xCoords);
        double sum = 0.0;
        for (int i = 0 ; i < xCoords.size() ; i++) {
           double a = xCoords.get(i) - Xavg;
           sum += a*a;
        }
        return sum;
    }
    
    public double getSxy() {
       double Xavg = LinearRegression.avg(xCoords);
        double sum = 0.0;
        for (int i = 0 ; i < xCoords.size() ; i++) {
           double a = xCoords.get(i) - Xavg;
           sum += a*a*yCoords.get(i);
        }
        return sum;        
    }
    public double getB0Variance() {
        double ve = getVarianceEstimation();
        double xAvg = LinearRegression.avg(xCoords);
       return ve * (1.0/xCoords.size() + xAvg*xAvg/getSxx());
    } 
    public double getB1Variance() {
        return getVarianceEstimation() / getSxx();
    }
    
    public double getB0EstimatedError() {
        return Math.sqrt(getB0Variance());
    }
    public double getB1EstimatedError() {
        return Math.sqrt(getB1Variance());
    }

    public boolean  testB0(double b0,double probability) {
        double ve = getVarianceEstimation();
        double t0 = (getB0() - b0)/ (getB0EstimatedError());
        return Math.abs(t0) <= getTAlpha(probability);
    }
    public boolean  testB1(double b1,double probability) {
        double ve = getVarianceEstimation();
        double t0 = (getB1() - b1)/ (getB1EstimatedError());
        return Math.abs(t0) <= getTAlpha(probability);
    }

    public double getDeterminanceIndex() {
        double modelSum = 0;
        double realSum = 0;
        double yAvg = LinearRegression.avg(yCoords);
        for (int i = 0 ; i < yCoords.size() ; i++) {
           double mi = b0 + b1*xCoords.get(i) - yAvg;
           modelSum += mi*mi; 
           double mr = yCoords.get(i) - yAvg;
           realSum += mr*mr;
        }
        return modelSum/realSum;
    }
    private double getTAlpha(double probability) {
        TDistribution td = new TDistribution(xCoords.size() - 2);
        return td.inverse(1.0 - probability/2);
    }
 
    
}
