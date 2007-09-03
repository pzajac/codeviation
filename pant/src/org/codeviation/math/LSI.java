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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.NotConvergedException;
//import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.codeviation.math.LSI.Engine;
import org.codeviation.math.LSI.Result;
import org.codeviation.math.MatrixUtil.MatrixHeader;
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
public final class LSI<ROW extends Serializable ,COLUMN extends Serializable> implements Serializable {
    private AnotatedMatrix<ROW,COLUMN> am;
    private no.uib.cipr.matrix.SVD mtjSvd;
    private SVD aTaSvd;
    private int rank ;
    private RowComparator.Type rowComparator ;
    private Logger logger = Logger.getLogger(LSI.class.getName());
    private Engine engine;
    DenseMatrix u;
    DenseMatrix vt;
    private double s[];

    private static final long serialVersionUID = 1;
    
    public LSI(AnotatedMatrix<ROW,COLUMN> am) throws NotConvergedException {
        this.am = am;     
        engine = Engine.MTJ_SVD;
    }
    
    public LSI (AnotatedMatrix<ROW,COLUMN> am,Engine engine) {
        this.am = am;
        this.engine = engine;
        rank = Math.min(am.getMatrix().numColumns(),am.getMatrix().numRows());
    }
    public void compute() throws NotConvergedException {
        switch (engine) {
        case MTJ_SVD:
            mtjSvd = no.uib.cipr.matrix.SVD.factorize(am.getMatrix());
           if  (mtjSvd.getS().length < rank ) {
               rank = mtjSvd.getS().length;
           }      
           break;
        case AtA_SVD:
            aTaSvd = SVD.factorize(am.getMatrix(), rank);
         
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

    /** Result item with simolarity factor
     */
    public  class Result implements Comparable<Result> {
        ROW item;
        float value;

         Result(ROW item, float value) {
            this.item = item;
            this.value = value;
        }

        public ROW getItem() {
            return item;
        }

        public float getValue() {
            return value;
        }

        public int compareTo(Result o) {
            float diff =  o.getValue() - getValue() ;
            return (diff > 0 ) ? 1 : -1;
         }
    }

    
    /**
     * LSI query with norm of LSI. 
     * @param q query vector
     * @param numResults number of results, ignored on -1
     * @return sorted results
     */
    public List<Result> query(Vector q, int numResults) {
        Vector resultVector = query(q);
        List<Result> results = new ArrayList<Result>(resultVector.size());
        for (int rIt = 0 ; rIt < resultVector.size() ; rIt++) {
            results.add(new Result(getAm().getRows().get(rIt),(float)resultVector.get(rIt)));
        }
        Collections.sort(results);
        
        if (numResults == -1 ) {
            return results;
        }
        if (resultVector.size() < numResults ) {
            numResults = resultVector.size();
        }
        List<Result> filterRes = new ArrayList<Result> (numResults);
        for (int rIt = 0 ; rIt < numResults  ; rIt++) {
            filterRes.add(results.get(rIt));
        }
        return filterRes;
    }
    public Vector query(Vector q) {
       // s*vt*q
      DenseMatrix vt = getVt();
      DenseVector result = new DenseVector(getRank());
      DenseMatrix u = getU();
      DenseVector result2 = new DenseVector(u.numRows());
      
 
          // s*vt*q
          double s[] = getS();
          for (int row = 0 ; row < getRank() ; row++) {
              double sum = 0;
              for (int col = 0 ; col < vt.numColumns() ; col++) {
                  sum += vt.get(row, col) * q.get(col);
              }
             result.set(row,sum/s[row]);
//              result.set(row,sum);
          } 
          // u*(s*vt*q)
          // replace with row comparator
          RowComparator rcom = new RowComparator(u,getRank(),result,s,getRowComparatorType());
          for (int r = 0 ; r < result2.size() ; r++) {
              result2.set(r, rcom.product(r));

          }
//            for (int row = 0 ; row < u.numRows() ; row++) {
//              double sum = 0;
//              for (int col = 0 ; col < getRank() ; col++) {
//                  sum += u.get(row, col) * result.get(col);
//              }
//              result2.set(row,sum);
//          } 
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
      * @throws java.io.IOException 
      */
    public void plotSVD () throws IOException {
        List<ROW> rows = am.getRows();
        int size = rows.size();
        float data[][] = new float[2][size];
        DenseMatrix u = getU();
        
        System.out.println(u.numRows() + "," + u.numColumns());
        for (int i = 0 ; i < size ; i++){
            data[0][i] = (float) u.get(i, 0);
            data[1][i] = (float) u.get(i, 1);
//            System.out.println(rows.get(i) + ":" + data[0][i] + "," + data[1][i]);
        }
        System.out.println("Singular values:");
        for(double s : getS()) {
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
            RowComparator comparator = new RowComparator(u,rank,r,mtjSvd.getS(),getRowComparatorType());
            Arrays.sort(indexes,comparator);
            for (int i = 0 ; i < 10 ; i++) {
                System.out.print("" + rows.get(indexes[i]) + comparator.product(indexes[i]) +":");
            }
            System.out.println("");
        }
    }

    public AnotatedMatrix<ROW, COLUMN> getAm() {
        return am;
    }

//    private no.uib.cipr.matrix.SVD getSvd() {
//        return mtjSvd;
//    }

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

    public  double[] getS()  {
        if (s == null) { 
            try {
                switch(engine ) {
                case MTJ_SVD:
                    s = mtjSvd.getS();
                    break;
                case AtA_SVD:
                    s = Matrices.getArray(aTaSvd.getS());
                    break;
                default :
                    throw new IllegalStateException();
                }
            } catch (NotConvergedException nce) {
                throw new IllegalStateException(nce);
            }
        }
        return s;
        
    }

    public DenseMatrix getU()  {
        try {
            if (u != null) {
                return u;
            }
            switch (engine) {
                case MTJ_SVD:
                    u =  mtjSvd.getU();
                    break;
                case AtA_SVD:
                    DenseMatrix ut =  aTaSvd.getVt();
                    u = new DenseMatrix(ut.numColumns(),ut.numRows());
                    ut.transpose(u);
                    break;
                default:
                    throw new IllegalStateException();
            }
        } catch (NotConvergedException nce) {
            throw new IllegalStateException(nce);
        }
        return u;
    }

    public DenseMatrix getVt() {
        try {
            if (vt != null) {
                return vt;
            }
            switch (engine) {
                case MTJ_SVD:
                    vt =  mtjSvd.getVt();
                    break;
                case AtA_SVD:
                    DenseMatrix v = aTaSvd.getU();
                    vt = new DenseMatrix(v.numColumns(),v.numRows());
                    v.transpose(vt);
                    break;
                default:
                    throw new IllegalStateException();
            }
        } catch (NotConvergedException nce) {
            throw new IllegalStateException(nce);
        }
        return vt;
    }

    private DenseMatrix readDenseMatrix(ObjectInputStream ois) throws IOException {
        MatrixHeader readMatrixHeader = MatrixUtil.readMatrixHeader(ois);
        if (readMatrixHeader != null) {
            DenseMatrix mat = new DenseMatrix(readMatrixHeader.getNumRows(),readMatrixHeader.getNumColumns());
            MatrixUtil.readMatrix(mat, ois);
            return mat;
        }
        return null;
    }

    private void writeDenseMatrix(DenseMatrix u,ObjectOutputStream oos) throws IOException {
        MatrixUtil.writerMatrixHeader(u, oos);
        MatrixUtil.writeMatrix(u, oos);
    }
    
    /** Type of svd engine
     */
    public enum Engine {
        /** svd based on MTJ svd library*/
        MTJ_SVD,
        /** svd bsed on praks AtA matrix multi*/
        AtA_SVD
    } 
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(am);
        oos.writeObject(engine);
        oos.writeInt(rank);
        writeDenseMatrix(getU(),oos);
        writeDenseMatrix(getVt(),oos);
        getS();
        if (s == null) {
            oos.writeInt(-1);
        } else {
            oos.writeInt(s.length);
            for (double val : s) {
                oos.writeDouble(val);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        am = (AnotatedMatrix<ROW, COLUMN>) ois.readObject();
        engine = (Engine) ois.readObject();
        rank = ois.readInt();
        u = readDenseMatrix(ois);
        vt = readDenseMatrix(ois);
        int sizeS = ois.readInt();
        if (sizeS > -1 ) {
            s = new double[sizeS];
        }
        for(int i = 0 ; i < s.length ; i++) {
            s[i] = ois.readInt();
        }
        
    }
        
}
