package org.codeviation.math;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author pzajac
 */
public final class LinearRegression {
    
    
    /** solve a'a x = a'b
     */
    public static Vector solve (Matrix a,Vector v) {
        int size = a.numColumns();
        Matrix aa = new DenseMatrix(size,size);
        Matrix at = new DenseMatrix(a.numColumns(),a.numRows());
        a.transpose(at);
        at.mult(a, aa);
        Vector ab = new DenseVector(size);
        at.mult(v, ab);
        Vector result = new DenseVector(size);
        return aa.solve(ab, result);
    }   

 
    // Statistics of Bris, chapter 11.4
    /** solve linear regression 
     * y = b0 + b1*x
     * @return array {b0,b1}
     */ 
    public static double[] solve (Vector xVec,Vector yVec) {
        if (xVec.size() < 1 || xVec.size() != yVec.size()) {
            throw new IllegalStateException("size");
        }
        double yAvg = avg(yVec);
        double xAvg = avg(xVec);
        
        double size = xVec.size();
        double yCoef = 0;
        
        double sumXres = 0 ;
        for (int i = 0 ; i < xVec.size() ; i++) {
            double xRes = xVec.get(i) - xAvg; 
            yCoef += xRes*yVec.get(i);
            sumXres += xRes*xRes;
        }
        double b1 = yCoef /sumXres;
        double b0 = yAvg  - b1*xAvg;
        return new double[]{b0,b1}; 
        
        
    }
    /** Convert series to two vectors.
     * @param series series with points in 2d
     * @return array {x,y} coords
     */ 
    public static Vector[] convertSeriesToVectors (XYSeries series) {
            Vector xCoords = new DenseVector(series.getItemCount());
            Vector yCoords = new DenseVector(series.getItemCount());
            for (int i = 0 ; i < series.getItemCount() ; i++) {
                xCoords.set(i,series.getX(i).doubleValue());
                yCoords.set(i,series.getY(i).doubleValue());
            }
            return  new Vector[]{xCoords,yCoords};
    }
    /** solve linear regression square residual sum     
     * y = b0 + b1*x
     * @param xVec  x coords
     * @param yVec  y coords
     * @param b0 line param1
     * @param b1 line param2
     * @return S_R*S_R
     */
      public static double residuumSquareSum(Vector xVec,Vector yVec,double b0,double b1) {
          double SrSr =  0;
          for (int i = 0 ; i < xVec.size() ; i++) {
              double x =  xVec.get(i);
              double y = yVec.get(i);
              double yy = b0 + b1*x;
              SrSr += (yy - y)*(yy - y);
          } 
          return SrSr;
    }
    public static double residuum(double SrSr,int size ) {
        return Math.sqrt(SrSr/size);
    }
    static double avg(Vector vec) {
        double sum = 0;
        for (int i = 0 ; i < vec.size() ; i++) {
            sum += vec.get(i); 
        }
        return sum/vec.size();
    }
}
