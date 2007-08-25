/*
 * AnotatedMatrix.java
 * 
 * Created on Jun 8, 2007, 8:06:46 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 * A sparse matrix with labeled rows and columns
 * @param ROW  for example string 
 * @param COLUMN 
 * @author pzajac
 */
public class AnotatedMatrix<ROW extends Serializable,COLUMN extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1;
    private transient FlexCompRowMatrix matrix ;
    private List<ROW> rows; 
    private List<COLUMN> columns;
    

    public AnotatedMatrix(FlexCompRowMatrix matrix, List<ROW> rows, List<COLUMN> columns) {
        this.matrix = matrix;
        this.rows = rows;
        this.columns = columns;
        
        
    }

    public Map<COLUMN,Integer> getColumnToNumberMap() {
        Map<COLUMN,Integer> map = new HashMap<COLUMN, Integer>();
        for (int i = 0 ; i < columns.size() ; i++) {
            map.put(columns.get(i),i);
        }
        return map;
    }
    
    public Map<ROW,Integer> getRowToNumberMap() {
        Map<ROW,Integer> map = new HashMap<ROW, Integer>();
        for (int i = 0 ; i < rows.size() ; i++) {
            map.put(rows.get(i),i);
        }
        return map;
    }
        
    public List<COLUMN> getColumns() {
        return columns;
    }
    

    public FlexCompRowMatrix getMatrix() {
        return matrix;
    }

    public List<ROW> getRows() {
        return rows;
    }

    private void writeObject (ObjectOutputStream oos) throws IOException {
        oos.writeObject(rows);
        oos.writeObject(columns);
        oos.writeInt(matrix.numRows());
        oos.writeInt(matrix.numColumns());
        for (int r = 0 ; r < matrix.numRows() ; r++) {
            SparseVector row = matrix.getRow(r);
            int used = row.getUsed();
            oos.writeInt(used);
            for (VectorEntry ve :row) {
                oos.writeInt(ve.index());
                oos.writeDouble(ve.get());
            }
        }
    }
    @SuppressWarnings("unchecked")
    private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
        rows = (List<ROW>) ois.readObject();
        columns = (List<COLUMN>) ois.readObject();
        
        int numRows = ois.readInt();
        int numCols = ois.readInt();
        matrix  = new FlexCompRowMatrix(numRows,numCols);
        for (int r  = 0; r < numRows; r++) {
            int cols = ois.readInt();
            for (int c = 0 ; c < cols ; c++) {
                matrix.set(r,ois.readInt(), ois.readDouble());
            }
        }
    }
    

    /** replaces rows and columns
     */
    AnotatedMatrix<COLUMN,ROW> getTransposedMatrix() {
        FlexCompRowMatrix newMat = new FlexCompRowMatrix(matrix.numColumns(),matrix.numRows());
        for (MatrixEntry me : matrix) {
            newMat.set(me.column(), me.row(), me.get());
        }
        return new AnotatedMatrix<COLUMN,ROW>(newMat,columns,rows);        
    }    
}
