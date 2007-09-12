/*
 * SVD.java
 * 
 * Created on Jul 10, 2007, 9:17:56 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.util.Arrays;
import java.util.Comparator;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.LowerSymmDenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmDenseEVD;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 * SVD of matrix. A computed be using of eigenvalue facurizatioon of matrix
 *  AA'.The nuber of rows in matrix A is lower then number of columns.
 * 
 * @author pzajac
 */
public class SVD {
    FlexCompRowMatrix matrix;
    int rank;
    DenseVector s;
//    DenseVector v;
    private DenseMatrix vt;
    private DenseMatrix u;
    
    private boolean transposed;
    
    private SVD(FlexCompRowMatrix matrix, int rank) {
        if (matrix.numColumns() < matrix.numRows()) {
            // transpose matrix;
              this.matrix = new FlexCompRowMatrix(matrix.numColumns(),matrix.numRows());
              for (MatrixEntry me : matrix) {
                 this.matrix.set(me.column(), me.row(), me.get());
              }
              transposed = true;
        } else {
            this.matrix = matrix;
        }
        this.rank = rank;
        int maxRank = Math.min(matrix.numColumns(),matrix.numRows());
        if (rank > maxRank) {
            throw new IllegalArgumentException(" rank :" + rank + " >= " + maxRank);
        }
    }
    

    /** factorize transpozed matrix with rank k
     */
    public static SVD factorize(FlexCompRowMatrix matrix,int rank) {
        return new SVD(matrix,rank);
    }
    
    /** @return singular values, do not modify returned value! 
     */
    public DenseVector getS() throws NotConvergedException {
        computeSingularValues();
        return s;
    }
     /** @return U vector of factrorization, do not modify returned value! 
     */
    public DenseMatrix getVt() throws NotConvergedException {
       if (transposed) {
           if (u == null) {
                computeU();
                DenseMatrix ut = new DenseMatrix(u.numColumns(),u.numRows());
                u.transpose(ut);
                u = ut;
           }
           return u;
       } else {
            computeSingularValues();
            return vt;
       }
    }
    public DenseMatrix getU() throws NotConvergedException {
        if (transposed) {
            computeSingularValues();
            DenseMatrix uu = new DenseMatrix(vt.numColumns(), vt.numRows());
            vt.transpose(uu);
            return uu;
        } else {
            computeU();
            return u;
        }
    }
    
    private void computeSingularValues() throws NotConvergedException {
        if (s == null) {
            LowerSymmDenseMatrix multAAt = org.codeviation.math.MatrixUtil.multAAt(matrix);
            SymmDenseEVD sdevd = SymmDenseEVD.factorize(multAAt);
            final double eigs[] = sdevd.getEigenvalues();

            // sort eigen values
            Integer order[] = new Integer[eigs.length];
            for (int i = 0; i < order.length; i++) {
                order[i] = Integer.valueOf(i);
            }

            Arrays.sort(order,new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    return (eigs[o1] > eigs[o2]) ? -1 : 1;
                }
            }); 

            s = new DenseVector(rank);
            vt = sdevd.getEigenvectors();
            for (int i = 0; i < s.size(); i++) {
                double eigv = eigs[order[i]];
                if (eigv < 0 ) {
                    eigv = 0;
                }
                s.set(i, Math.sqrt(eigv));
                swapURows(i,order[i],vt);
            }
            // set the correct rank
            DenseMatrix rankv = new DenseMatrix(rank,vt.numRows());
            for (int r = 0 ; r < rankv.numRows() ; r++) {
                for (int c = 0 ; c < rankv.numColumns() ; c++) {
                    rankv.set(r,c,vt.get(c,r));
                }
            }
            vt = rankv;
        }
    }

   
    
    private void swapURows(int r1,int r2,Matrix v) {
        if (r1 < r2) {
            for (int col = 0 ; col < v.numRows() ; col++) {
                double tmp = v.get(col,r1);
                v.set(col,r1,v.get(col,r2));
                v.set(col,r2,tmp);
            }
        }
    }
    
    private void computeU() throws NotConvergedException {
        if (u == null)  {
            computeSingularValues();
            
            u = new DenseMatrix(matrix.numColumns(),vt.numRows());
            // XXX this row should be optimized
            matrix.transABmult(vt, u);
            
            for (int c = 0 ; c < rank ; c++) {
                double sr = s.get(c);
                if (Math.abs(sr) < MIN_SINGULAR_VALUE) {
                    sr = 0;
                } else {
                    sr = 1./sr;
                }
                for (int r = 0 ; r < u.numRows(); r++) {
                    u.set(r, c, u.get(r, c)*sr);
                }
            }
        }
    }
    /** @return  qc = (((q’ * A) * V) * pinv(S)) * pinv(S);
     */ 
    public Vector query(Vector q) throws NotConvergedException {
        Vector qc = new DenseVector(matrix.numColumns());
        matrix.transMult(q,qc);
        Matrix matVt = getVt();
        Vector qAV = new DenseVector(matVt.numRows()); 
        matVt.mult(qc, qAV);
        DenseVector qAVpinvS =  multPinvS(qAV);
        return qAVpinvS;
    }
    private DenseVector multPinvS(Vector vec) throws NotConvergedException {
        DenseVector sLoc = getS();
        DenseVector vecPinvS = new DenseVector(rank);
        for (int c = 0 ; c < rank ; c++) {
            double sr = sLoc.get(c);
            if (Math.abs(sr) < MIN_SINGULAR_VALUE) {
                sr = 0;
            } else {
                sr = 1./sr;
            }
            vecPinvS.set(c,sr*sr*vec.get(c));
        }
        return vecPinvS;
    }
    private double MIN_SINGULAR_VALUE = 1e-6;
}
