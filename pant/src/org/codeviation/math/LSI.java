/*
 * LSI.java
 * 
 * Created on Jun 10, 2007, 7:58:44 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.codeviation.math.RowComparator.Type;
import org.codeviation.statistics.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;

/**
 *
 * @param ROW 
 * @param COLUMN 
 * @author pzajac
 */
public class LSI<ROW extends Serializable ,COLUMN extends Serializable> {
    private AnotatedMatrix<ROW,COLUMN> am;
    private SVD svd;
    private int rank ;
    private static int DEFAULT_RANK = 0;
    private RowComparator.Type rowComparator ;
    private Logger logger = Logger.getLogger(LSI.class.getName());
    
    public LSI(AnotatedMatrix<ROW,COLUMN> am) throws NotConvergedException {
        this.am = am;        
    }
    
    public void compute() throws NotConvergedException {
        svd = SVD.factorize(am.getMatrix());
       if  (svd.getS().length < rank ) {
           rank = svd.getS().length;
       }
    }

    /** 
     * A_ji = f_ji log(n/n_i)
     * 
     * f_ji - frequency of term i in document j, 
     * n is the number of documents in the collection,
     * n_i is the number of ducuments in which the term i appears
     */ 
    public void tfidf() {
        FlexCompRowMatrix sparseMatrix = am.getMatrix();
        int ni[] = new int[sparseMatrix.numColumns()];
        int n = sparseMatrix.numRows();
        
        // compute ni 
        for (int i = 0 ; i < n ; i++ ) {
           SparseVector row = sparseMatrix.getRow(i);
           for (VectorEntry ve :row) {
                ni[ve.index()]++;
           }
        }
        
        for (int i = 0 ; i < n ; i++ ) {
           SparseVector row = sparseMatrix.getRow(i);
           double words = 0;
           for (VectorEntry ve :row) {
                words+= ve.get();
           }
           for (VectorEntry ve :row) {
                double num = ve.get();
                double fji = num/words;
               ve.set(fji * Math.log(n/(double)ni[ve.index()]));
           }
        }
    }

    public Vector query(Vector q) {
       // u*s*vt*q
      DenseMatrix vt = svd.getVt();
      DenseVector result = new DenseVector(q.size());
      
      // s*vt*q
      double s[] = svd.getS();
      for (int row = 0 ; row < getRank() ; row++) {
          double sum = 0;
          for (int col = 0 ; col < vt.numColumns() ; col++) {
              sum += vt.get(row, col) * q.get(col);
          }
          result.set(row,sum*s[row]);
      } 
      
      
      // u*s*vt*q
      DenseMatrix u = svd.getU();
   
      DenseVector result2 = new DenseVector(q.size());
      for (int row = 0 ; row < u.numRows() ; row++) {
          double sum = 0;
          for (int col = 0 ; col < getRank() ; col++) {
              sum += u.get(row, col) * result.get(col);
          }
          result2.set(row,sum);
      } 
      return result2;
    }
    public  void centerColumns() {
        FlexCompRowMatrix sparseMatrix = am.getMatrix();
        double colsSum[] = new double[sparseMatrix.numColumns()];
        int colsCount[] = new int[sparseMatrix.numColumns()];
        double colsAvg[] = new double[sparseMatrix.numColumns()];
        for (int i = 0 ; i < sparseMatrix.numRows() ; i++ ) {
           SparseVector row = sparseMatrix.getRow(i);
           for (VectorEntry ve :row) {
                colsCount[ve.index()]++;
                colsSum[ve.index()]+= ve.get();
           }
        }
        for (int i = 0 ; i < colsCount.length ; i++) {
             if (colsCount[i] != 0) {
                 colsAvg[i] = colsSum[i]/colsCount[i];
             }
        }
        for (int i = 0 ; i < sparseMatrix.numRows() ; i++ ) {
           SparseVector row = sparseMatrix.getRow(i);
           for (VectorEntry ve :row) {
                colsCount[ve.index()]++;
                ve.set(ve.get() - colsAvg[ve.index()]);
           }
        }
    }

/** make sum of squares to 1
*/
     public void normalizeRows() {
        for (int i = 0 ; i < am.getMatrix().numRows() ; i++ ) {
           SparseVector row = am.getMatrix().getRow(i);
           double  sum = 0;
           int count = 0;
           for (VectorEntry ve :row) {
               sum += ve.get();
               count++;
           }
           // XXXX
           if (sum < 1e-5) {
               logger.info(" empty row"); 
           } else {
                // 6 
               for (VectorEntry ve :row) {
                   ve.set(Math.sqrt(ve.get()/sum));
               }
           }
        }
    }
     /** XXX experimental
      */
    public void plotSVD () throws IOException {
        List<ROW> rows = am.getRows();
        int size = rows.size();
        float data[][] = new float[2][size];
        DenseMatrix u = svd.getU();
        
        System.out.println(u.numRows() + "," + u.numColumns());
        for (int i = 0 ; i < size ; i++){
            data[0][i] = (float) u.get(i, 0);
            data[1][i] = (float) u.get(i, 1);
//            System.out.println(rows.get(i) + ":" + data[0][i] + "," + data[1][i]);
        }
        System.out.println("Singular values:");
        for(double s : svd.getS()) {
            System.out.println(s);
        }
        final NumberAxis domainAxis = new NumberAxis("X");
        final NumberAxis rangeAxis = new NumberAxis("Y");
        FastScatterPlot fsp = new FastScatterPlot(data, domainAxis, rangeAxis);
        JFreeChart chart = new JFreeChart("Fast Scatter Plot", fsp);
        ChartUtils.chartToFile(new File("/tmp/chart.png"), chart, 500, 500);
    
        Map<Double,Integer> rowsMap = new TreeMap<Double,Integer>();
        for (int r = 0 ; r < rows.size() ; r++) {
            rowsMap.put(u.get(r, 0), r);
        }
        
        for (Map.Entry<Double,Integer> entry : rowsMap.entrySet()) {
            int r = entry.getValue();
            System.out.println(rows.get(r) + " : " + u.get(r, 0) + " , " + u.get(r, 1) + "," + u.get(r, 2));
            
        }
        // find similar developers
        System.out.println("---------------------");
        Integer indexes[] = new Integer[rows.size()];
        for (int r = 0 ; r < u.numRows(); r++) {
            System.out.print(rows.get(r) + " : ");
            for (int i = 0 ; i < rows.size(); i++) {
                indexes[i] = i;
            }
            RowComparator comparator = new RowComparator(u,rank,r,svd.getS(),getRowComparatorType());
            Arrays.sort(indexes,comparator);
            for (int i = 0 ; i < 10 ; i++) {
                System.out.print("" + rows.get(indexes[i]) + comparator.product(r, indexes[i]) +":");
            }
            System.out.println("");
        }
    }

    public AnotatedMatrix<ROW, COLUMN> getAm() {
        return am;
    }

    public SVD getSvd() {
        return svd;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
    public RowComparator.Type getRowComparatorType() {
        return (rowComparator == null) ? 
            RowComparator.Type.DOT_PRODUCT : rowComparator ;
    }

    public void setRowComparator(Type rowComparator) {
        this.rowComparator = rowComparator;
    }
    
}
