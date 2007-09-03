/*
 * MatrixUtil.java
 * 
 * Created on Jul 10, 2007, 6:52:21 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import no.uib.cipr.matrix.AbstractMatrix;
import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
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
        if (ias.length == 0 || ibs.length == 0) {
            return 0;
        }
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
        int prevIaVal = -1;
        int prevIbVal = -1;
        while (ia <=  maxa && ib <= maxb) {
            if (iaVal == ibVal && iaVal > prevIaVal && ibVal > prevIbVal) {
                result += a.get(iaVal)*b.get(ibVal);
                ++ia;
                ++ib;
                if (ia > maxa || ib > maxb) {
                    return result;
                }
                prevIaVal = iaVal;
                prevIbVal = ibVal;
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

    /** write all entries of matrix
     */
    public static void writeMatrix(Matrix mat,ObjectOutputStream oos) throws IOException {
        for (MatrixEntry entry : mat) {
            oos.writeInt(entry.row());
            oos.writeInt(entry.column());
            oos.writeDouble(entry.get());
        }
        oos.writeInt(-1);
    }

    /** read all entries of matrix
    */
    public static void readMatrix(Matrix mat,ObjectInputStream ois) throws IOException {
        int row ;
        while ((row = ois.readInt()) != -1) {
            int col = ois.readInt();
            double val = ois.readDouble();
            mat.set(row,col,val);
        }
    }
    /** Matrix header for serialization of Matrices
    */
    public static final class MatrixHeader {
        int numRows;
        int numColumns;

        public MatrixHeader(int numRows, int numColumns) {
            this.numRows = numRows;
            this.numColumns = numColumns;
        }

        public int getNumColumns() {
            return numColumns;
        }

        public int getNumRows() {
            return numRows;
        }
    }
   public static void writerMatrixHeader(Matrix mat,ObjectOutputStream oos) throws IOException {
        if (mat == null) {
            oos.writeInt(-1);
        } else {
            oos.writeInt(mat.numRows());
            oos.writeInt(mat.numColumns());
        }
    }

 
    public static MatrixHeader readMatrixHeader (ObjectInputStream ois) throws IOException {
        int numRows = ois.readInt();
        if (numRows == -1) {
            return null;
        } else {
            return new MatrixHeader(numRows,ois.readInt());
        }
    }
    
}
