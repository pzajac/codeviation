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
import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author pzajac
 */

public class RowComparator implements Comparator<Integer> {
    Matrix matrix;
    int rank;
    int row;
    double s[];
    Type type;
    
    enum Type {
        EUCLIDIAN,
        DOT_PRODUCT
    }
    
    public RowComparator(Matrix matrix, int rank, int row,double s[],Type type) {
        System.out.println("Rank:" + rank + "," + s.length);
        this.matrix = matrix;
        this.rank = rank;
        this.row = row;
        this.s = s;
        this.type = type;
    }
    
    
    public int compare(Integer row1, Integer row2) {
        return (product(row1,row2)) > 0 ? 1 : -1;
    }
    
    public double product(Integer row1, Integer row2) {
        int r1 = row1;
        int r2 = row2;
        double v1 = 0;
        double v2 = 0;

        if (type.equals(Type.EUCLIDIAN)) {
            for (int i = 0 ; i < rank ; i++) {
                double x = matrix.get(row, i);
                double x1 = matrix.get(r1,i) - x ;
                double x2 = matrix.get(r2,i) - x;
                double ss = s[i];
                v1 += x1*x1*ss*ss;
                v2 += x2*x2*ss*ss;
            }
        } else if (type.equals(Type.DOT_PRODUCT)) {
            for (int i = 0 ; i < rank ; i++) {
                double x = matrix.get(row, i);
                double x1 = matrix.get(r1,i) - x ;
                double x2 = matrix.get(r2,i) - x;
                double ss = s[i];
                ss *= ss;
                v1 += x1*x*ss;
                v2 += x2*x*ss;
            }
            
        }
        return v1 - v2;
        
    }
}



