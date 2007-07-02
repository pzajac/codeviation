/*
 * LogsUtil.java
 * 
 * Created on Jun 22, 2007, 9:27:21 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.math;

import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author pzajac
 */
public class LogsUtil {

    private LogsUtil() {
    }

    public static void printMatrix (Matrix m) {
        for(int i = 0 ; i < m.numRows() ; i++) {
            for(int j = 0 ; j < m.numColumns() ; j++) {
                System.out.print(m.get(i, j) + " ");
           }
           System.out.println("");
        }
    }
}
