/*
 * RowComparatorTest.java
 * JUnit based test
 *
 * Created on July 2, 2007, 3:55 PM
 */

package org.codeviation.math;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author pzajac
 */
public class RowComparatorTest extends TestCase {
    
    public RowComparatorTest(String testName) {
        super(testName);
    }

    public void testRowComparator() {
        double marr[][] = new double[][] {
            {0,1,0,1},
            {1,0,1,1},
            {1,1,1,1}
        };
        Matrix matrix = new DenseMatrix(marr);
        double s[] = new double[] {3,2,1};
        RowComparator rc = new RowComparator(matrix, 3, 1, s, RowComparator.Type.DOT_PRODUCT);
        assertTrue(rc.compare(0, 2) < 0);
        assertTrue(rc.compare(2, 0) > 0);
        assertTrue(rc.compare(0, 0) == 0);
                
        rc = new RowComparator(matrix, 3, 1, s, RowComparator.Type.EUCLIDIAN);
        assertTrue(rc.compare(0, 2) > 0);
        assertTrue(rc.compare(2, 0) < 0);
        assertTrue(rc.compare(0, 0) == 0);
        
    }
    
}
