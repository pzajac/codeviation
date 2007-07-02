/*
 * DevelopersFastSVDTest.java
 * JUnit based test
 *
 * Created on June 28, 2007, 9:53 PM
 */

package org.codeviation.math;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import junit.framework.TestCase;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author pzajac
 */
public class DevelopersFastSVDTest extends TestCase {
    
    public DevelopersFastSVDTest(String testName) {
        super(testName);
    }

    public void testDevelopersMatrix() throws  IOException, ClassNotFoundException, NotConvergedException {
        FileInputStream fis = new FileInputStream("/home/pzajac/skola/fastsvd/develmatrix.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        AnotatedMatrix am = (AnotatedMatrix) ois.readObject();
        am = am.getTransposedMatrix();
        System.out.println(am.getColumns().size());
        System.out.println(am.getRows().size());

        FlexCompRowMatrix matrix = am.getMatrix();
        FastSVD svd = new FastSVD(matrix);
        svd.factorize(matrix.numColumns(), matrix.numColumns()*2, 10);
    }
    
}
