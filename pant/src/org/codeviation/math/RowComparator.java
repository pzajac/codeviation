/*
 * RowComparator.java
 *
 * Created on Jun 10, 2007, 8:00:12 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.util.Comparator;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *Compares of rows R1,R - R2,R  of matrix. Used by LSI.
 * @author pzajac
 */

public class RowComparator implements Comparator<Integer> {
    public static final double TOLERANCE = 1e-8;
    
    Matrix matrix;
    int rank;
//    int row;
    
    double s[];
    Type type;
    private Vector refRow;
    
   public  enum Type {
        EUCLIDIAN,
        DOT_PRODUCT,
        NOT_NORMALIZED_DOT_PRODUCT
    }
    
/**
 *  @param matrix left matrix of svd decomposition
 *  @param rank rank of SVD
 *  @param row primary row
 *  @param s singular values
 *  @param type type of norm
 *  
 */
    public RowComparator(Matrix matrix, int rank, int row,double s[],Type type) {
        this.matrix = matrix;
        this.rank = rank;
//        this.row = row;
        refRow = new DenseVector(matrix.numColumns());
        for (int col = 0 ; col < refRow.size() ; col++) {
            refRow.set(col,matrix.get(row,col));
        }
        this.s = s;
        this.type = type;
    }
    public RowComparator(Matrix matrix,int rank, Vector refRow,double s[],Type type) {
        this.matrix = matrix;
        this.rank = rank;
//        this.row = row;
        this.refRow = refRow;
        this.s = s;
        this.type = type;
    }
    
    
    public int compare(Integer row1, Integer row2) {
        double res1 = product(row1);
        double res2 = product(row2);
        return (res1 -res2) > 0 ? 1 : -1;
    }
    
    public double product(Integer row1) {
        int r1 = row1;
        double v1 = 0;
 
        switch (type) {
        case EUCLIDIAN:
            for (int i = 0 ; i < rank ; i++) {
                double x = refRow.get(i);
                double x1 = matrix.get(r1,i) - x ;
                double ss = s[i];
                v1 += x1*x1*ss*ss;
            }
            break;
        case DOT_PRODUCT:
        case NOT_NORMALIZED_DOT_PRODUCT:
            double ratio1 = 0;
            double ratio2 = 0;
            for (int i = 0 ; i < rank ; i++) {
                double x = refRow.get(i);
                double x1 = matrix.get(r1,i)  ;
                double ss = s[i];
                ss *= ss;
                v1 += x1*x*ss;
                ratio1 += x1*x1*ss;
                ratio2 += x*x*ss;
                
            }
            if (type == Type.DOT_PRODUCT) {
                v1 /= Math.sqrt(ratio1*ratio2);
            }
            break;
        default:
            throw new IllegalStateException("Invalid norm type:" + type);
        }  
        return Math.abs(v1);
    }
}
