/*
 * Matlab.java
 * 
 * Created on May 11, 2007, 10:43:03 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author pzajac
 */
public class Matlab {


    /** It writes matrix to matlab m-file. It ignores empty cells - good for 
     * sparse matrices.
     * @param matrix marix
     * @throws java.io.IOException 
     * @param outFile mfile
     */
    public static void toMFile(Matrix matrix,File outFile ) throws IOException {
        PrintWriter matWriter = new PrintWriter(new FileWriter(outFile));
        try {
            matWriter.println("% rows: " +  matrix.numRows());
            matWriter.println("% columns: " +  matrix.numColumns());

            for (int r = 0 ; r < matrix.numRows() ; r++) {
               for (int c = 0 ; c < matrix.numColumns() ; c++) {
                  double val = matrix.get(r, c);
                  if (val != 0.0) {
                      int ri = r + 1;
                      int ci = c + 1;
                      matWriter.println("mat(" + ri + "," + ci + ") = " + val + ";");
                  }
               } 
               matWriter.println();
            }
        } finally {
            matWriter.close();
        }
    }
}
