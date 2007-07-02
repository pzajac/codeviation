/*
 * FastSVD.java
 * 
 * Created on Jun 20, 2007, 10:08:49 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 *Factorize matrix A(m x n) ~ U*S*Vt, where U and V are ortonormal. And S is diagonal.
 * @author pzajac
 */
public class FastSVD {
    FlexCompRowMatrix A;
    private int m;
    private int n;    

//    double lastError;
//    double sumError;
    Random random;
    
    /**
     * 
     * @param mat factorized matrix A(m x n)
     */
    public FastSVD(FlexCompRowMatrix mat) {
        this.A = mat;
        m = A.numRows();
        n = A.numColumns();
        random = new Random(System.currentTimeMillis());
    }


    /**
     * Factorize matrix A(m x n) ~ U*S*Vt, wher U and V are ortonormal. And S is diagonal.
     *   
     * @param k rank of factorization - number of nonzerro elements in S.
     * @param s - number of rows from A  for a iteration 
     * @param iters number of iteration
     * @return Vt  
     * @throws no.uib.cipr.matrix.NotConvergedException 
     */
    public DenseMatrix factorize(int k,int s,int iters) throws NotConvergedException {
    
        DenseMatrix sum = new DenseMatrix(A.numColumns(),k);
        DenseMatrix H = new DenseMatrix(A.numColumns(),k);
        //for i=1:m,
        // ER(i) = norm(A(i,:))^2;
        //end
        //PROB(1) = ER(1);
        //for i=2:m,
        // PROB(i) = PROB(i-1) + ER(i);
        //end
        DenseVector ER = new DenseVector(m);
        DenseVector PROB = new DenseVector(m);
        double lastProb = 0;
        for (int i = 0 ; i < m ; i++) {
            double norm = A.getRow(i).norm(Vector.Norm.Two);
            
            norm *= norm;    
            ER.set(i, norm);
            lastProb += norm;
            PROB.set(i,lastProb);
        }
        //PROB(1) = ER(1);
        //for i=2:m,
        // PROB(i) = PROB(i-1) + ER(i);
        //end
        //NORMAFRO ee= norm(A,'fro')^2;
        double NORMAFRO = A.norm(Matrix.Norm.Frobenius);
        NORMAFRO *= NORMAFRO;
        //co = s/NORMAFRO;
        double co = s/NORMAFRO;
        for (int i = 0 ; i < iters ; i++) {
            random.setSeed(System.nanoTime()*i);
            doIteration(k,s,H,NORMAFRO,co,ER,PROB);
            sum.add(H);
        }
        return (DenseMatrix) sum.scale(1.0/iters);
    }


    private void constructS(DenseMatrix S,int s,double NORMAFRO,DenseVector PROB,DenseVector ER,double co) {
        //for i=1:s,
        //  %
        //  % pick random number ROW from 1 up to NORMAFRO.
        //  %
        //  ROW = ceil (rand * NORMAFRO);
        //  %
        //  % perform binary search in the PROB vector to locate the number
        //  % ROW and pick the corresponding ROW of A to be included  in S.
        //  %
        //  left = 1;
        //  right = m;
        //  while abs(left-right) > 1
        //   if ROW > PROB(ceil((left+right)/2))
        //      left = ceil((left+right)/2);
        //   else  right = ceil((left+right)/2);
        //   end
        //  end
        //  %
        //  % We normalize the row of A to include it in S
        //  %
        //end
        Set<Integer> genRows = new HashSet<Integer>();
        for (int i = 0 ; i < s ; ) {
            double ROW =  (random.nextDouble() * NORMAFRO);
            int left = 0;
            int right = m - 1 ;
            int idx = 0;
            double prevIdx = 10;
            while (right - left > 1 ) {
                prevIdx = idx;
                idx = (left + right)/2 ;  
               // idx = Math.ceil(idx);
                double prb = PROB.get(idx);
                if (ROW > prb) {
                    left = idx;
                }  else {
                    right = idx;
                }
            }
            if (ROW > PROB.get(left)) {
                 left = right;
            }
            if (genRows.contains(left)) {
                continue;
            } 
            int index = left;
            genRows.add(index);
            //  S(i,:) = A(left,:)/(sqrt(co*ER(left)));
            double val = Math.sqrt(co*ER.get(index));
            for (int col = 0 ; col < S.numColumns() ; col++) {
                S.set(i, col, A.get(index, col)/val);
            }
            i++;
            
        }
    }

/** @return error
*/
    private void doIteration(int k, int s, DenseMatrix H,double NORMAFRO,double co,DenseVector ER,DenseVector PROB) throws NotConvergedException {
        //  function [error,H] = fastsvd(A,s,k);
        //%
        //% [ROWS_PICKED,H] = fastsvd(A,s,k)
        //% Given an m-by-n matrix A, FASTSVD computes an approximation
        //% to the top k right singular vectors of A, by sampling s rows
        //% of A. It also returns an m-vector ROWS_PICKED that contains
        //% 1 if a certain row was picked and 0 otherwise.
        //%
        //
        //%
        //% Some initializations. The ER vector contains the norms of each
        //% row of A. The PROB vector is built with the following formula:
        //% $PROB(i) = \sum_{j=1}^i ER(j)$.
        //%
        //[m,n]  = size(A);
        //rand('state',sum(100*clock));
        //
        //
        //
        //%
        //% Pick a random subset of s rows of A to form S. We pick according
        //% to the probabilities described in our paper. The following proce-
        //% dure ensures it. We also normalize every row of A that is to
        //% be included in S. This is also described in our paper.
        //%
        //
        //% Initialize S to be an s-by-n all zeros matrix.
        //
        //S = zeros(s,n);
        //
        DenseMatrix S = new DenseMatrix(s,n);
        //for i=1:s,
        //  %
        //  % pick random number ROW from 1 up to NORMAFRO.
        //  %
        //  ROW = ceil (rand * NORMAFRO);
        //  %
        //  % perform binary search in the PROB vector to locate the number
        //  % ROW and pick the corresponding ROW of A to be included  in S.
        //  %
        //  left = 1;
        //  right = m;
        //  while abs(left-right) > 1
        //   if ROW > PROB(ceil((left+right)/2))
        //      left = ceil((left+right)/2);
        //   else  right = ceil((left+right)/2);
        //   end
        //  end
        //  %
        //  % We normalize the row of A to include it in S
        //  %
        //  S(i,:) = A(left,:)/(sqrt(co*ER(left)));
        //end
        constructS(S, s, NORMAFRO, PROB,ER,co);
        //
        //%
        //% compute the svd of S*S' (S' is the transpose of S). The
        //% matrix G is a diagonal matrix containing the singular
        //%
        //% values of S*S'. We need their square roots for a sub-
        //% sequent normalization.
        //[P,G,Q]=svd(full(S*S'));
        
        int ssSize = Math.max(S.numRows(),S.numColumns());
        DenseMatrix SS = new DenseMatrix(ssSize,ssSize);
        Matrix SSrank1 = SS.rank1(S);
        SVD svd = SVD.factorize(SSrank1);
        Matrix P = svd.getU();
        double  G[] = svd.getS();
        //d = diag(G);
        //d = sqrt(d);
        //
        for (int i = 0 ; i < G.length ; i++) {
            G[i] = Math.sqrt(G[i]);
        }
        //%
        
        //% divide the columns of P (right singular vectors - don't
        //% forget that P = Q because SS' is symmetric) by the cor-
        //% responding (square root of) singular value.
        //%
        //P = P(:,1:k);
        //i=1;
        //while i<=k
        // if abs(d(i)) >= 1e-2
        //  P(:,i) = P(:,i)/d(i);
        // else
        //  P(:,i) = zeros(s,1);
        // end
        //  i = i + 1;
        //end;
        //
        DenseMatrix PP = new DenseMatrix(P.numRows(),k);
        for (int i = 0 ; i < k ; i++) {
            double d = G[i];
            if (Math.abs(d) >= 1e-2) {
                for (int j = 0 ; j < P.numRows() ; j++) {
                    PP.set(j,i,P.get(j, i)/d);
                }
            } else {
                for (int j = 0 ; j < P.numRows() ; j++) {
                    PP.set(j,i,0);
                }
            }
        }
        //%
        //% form matrix H according to the paper
        //%
        //H = S'*P;
        //
           
          S.transAmult(PP, H);
          
        //%
        //% Now we have computed the approximation that we need.
        //% It will be the following matrix product: D = A*H*H'
        //% Here we compute the percentage of the Frobenius norm
        //% of A-AHH' (squared) over NORMAFRO. This is a measure
        //% for the error (see paper).
        //%
        //
       
        //t = norm(A*H,'fro')^2;
        //XXX just for testing this line should be removed
     //   A.getRow(s)
//  
    DenseVector newRow = new DenseVector(A.numColumns());
    double norm = 0;
    
    for (int r = 0 ; r < A.numRows() ; r++) {
            SparseVector row = A.getRow(r);
            double vecSum = 0;    
//            for (VectorEntry ve : row) {
//                vecSum = H.get(r, r)
//            }
//            H.transMult(row, newRow);
            norm += newRow.dot(newRow);
    }
    System.out.println("lastError:" + (1 - norm/NORMAFRO));
//        DenseMatrix tt = new DenseMatrix(A.numRows(),H.numColumns());
//        double lastFro  = A.mult(H, tt).norm(Matrix.Norm.Frobenius);
//        System.out.println("lastFro = " + lastFro);
//        lastError = 1 - lastFro*lastFro/NORMAFRO;
        //error = 1 - t/NORMAFRO;
    }

//    public double getSumError() {
//        return sumError;
//    }

    public void setRandom(Random random) {
        this.random = random;
    }   
}
