/*
 * MatrixUtil.java
 * 
 * Created on Jul 10, 2007, 6:52:21 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 *  Some utils with matrices operation
 * @author pzajac
 */
public final class MatrixUtil {

    public static LowerSymmDenseMatrix multAAt(FlexCompRowMatrix matrixA) {
        LowerSymmDenseMatrix result = new LowerSymmDenseMatrix(matrixA.numRows());
        for (int r = 0 ; r < matrixA.numRows() ;r++) {
            for (int c = r ; c  < matrixA.numRows() ; c++) {
                double val = dotSparseVec(matrixA.getRow(r),matrixA.getRow(c));
                result.set(c, r, val);
            } 
        }
        return result;
    }

    private static double dotSparseVec(SparseVector a, SparseVector b) {
        int ias[] = a.getIndex();
        int ibs[] = b.getIndex();
        int ia = 0,iaVal = ias[0];
        int ib = 0, ibVal = ibs[0];
        double result = 0;
        int maxa = ias.length - 1;
        int maxb = ibs.length -1;
        // it's strange: the lengh of index can be bigger than lenght of vector
        if (maxa >= a.size() ) {
            maxa = a.size() - 1;
        }
        if (maxb >= b.size() ) {
            maxb = b.size() - 1;
        }
        while (ia <=  maxa && ib <= maxb) {
            if (iaVal == ibVal) {
                result += a.get(iaVal)*b.get(ibVal);
                ++ia;
                ++ib;
                if (ia > maxa || ib > maxb) {
                    return result;
                }
                iaVal = ias[ia];
                ibVal = ibs[ib];
            } else {
                if (iaVal < ibVal ) {
                    do {
                        if (ia == maxa) {
                            if (iaVal == ibVal) {
                                result += a.get(iaVal)*b.get(ibVal);
                            }
                            return result;
                        }
                        iaVal = ias[++ia];
                    } while(iaVal < ibVal);
                } else if (ibVal < iaVal) {
                    do {
                        if (ib == maxb) {
                            if (iaVal == ibVal) {
                                result += a.get(iaVal)*b.get(ibVal);
                            }
                            return result;
                        }
                        ibVal = ibs[++ib];
                    } while (ibVal < iaVal ) ;
                } else {
                    return result;
                }
            }
        }
        return result;
    }

    
}
