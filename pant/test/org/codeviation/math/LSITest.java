/*
 * LSITest.java
 * JUnit based test
 *
 * Created on June 13, 2007, 10:04 PM
 */

package org.codeviation.math;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 *
 * @author pzajac
 */
public class LSITest extends TestCase {
    
    public LSITest(String testName) {
        super(testName);
    }

    static FlexCompRowMatrix createExampleHelsinkiMatrix() {
        FlexCompRowMatrix mat = new FlexCompRowMatrix(7,9);
         mat.set(1,0,1);
         mat.set(3,0,1);
         mat.set(4,0,1);
         mat.set(6,0,1);
         mat.set(1,1,1);
         mat.set(2,1,1);
         mat.set(5,2,1);
         mat.set(6,2,1);
         mat.set(3,3,1);
         mat.set(1,4,1);
         mat.set(2,4,1);
         mat.set(0,5,1);
         mat.set(3,5,1);
         mat.set(4,6,1);
         mat.set(5,6,1);
         mat.set(2,7,1);
         mat.set(3,7,1);
         mat.set(0,8,1);
         mat.set(3,8,1);
         return mat;
    }
    /** Test example from lsi chapter of 
     * Helsinki univesity
     * lecture7.pdf
     */ 
    public void  testExampleHelsinkiMtj() throws NotConvergedException, IOException, ClassNotFoundException {
        exampleHelsinkiLsi(LSI.Engine.MTJ_SVD);        
    }

    public void  testExampleHelsinkiAtA() throws NotConvergedException, IOException, ClassNotFoundException {
        exampleHelsinkiLsi(LSI.Engine.AtA_SVD);        
    }

    private AnotatedMatrix<String, String> createAM() {
        FlexCompRowMatrix mat = createExampleHelsinkiMatrix();

        List<String> terms = new ArrayList<String>();
        terms.add("Baby");
        terms.add("Child");
        terms.add("Guide");
        terms.add("Health");
        terms.add("Home");
        terms.add("Infant");
        terms.add("Proofing");
        terms.add("Safety");
        terms.add("Toddler");

        // Documents
        // D1: Infant & Toddler First Aid
        // D2: Babies and Children's Room (for your  Home);
        // D3: Child and Safety at Home
        // D4: YOu Baby's Health and Safety: From Infant to Toddler
        // D5: Baby Proofing Basics
        // D6: YOur Guide to Easy Rust Proofing
        // D7: Beanie Babies Collector's Guide
        List<String> documents = new ArrayList<String>();
        for (int i = 0; i < 8; i++) {
            documents.add("D" + i);
        }
        AnotatedMatrix<String, String> am = new AnotatedMatrix<String, String>(mat, documents, terms);
        return am;
    }

    public void testSVD() throws NotConvergedException {
        AnotatedMatrix<String, String> am = createAM();
        LSI<String, String> lsiMtj = new LSI<String, String>(am,LSI.Engine.MTJ_SVD);
        LSI<String, String> lsi = new LSI<String, String>(am,LSI.Engine.AtA_SVD);
        
        lsiMtj.compute();
        lsi.compute();
//        System.out.println("Mtj:");
//        System.out.println("U:");
//        LogsUtil.printMatrix(lsiMtj.getU());
//        System.out.println("V:");
//        LogsUtil.printMatrix(lsiMtj.getVt());
//        
//        System.out.println("AA':");
//        System.out.println("U:");
//        LogsUtil.printMatrix(lsi.getU());
//        System.out.println("V:");
//        LogsUtil.printMatrix(lsi.getVt());
        
    }
    private void exampleHelsinkiLsi(LSI.Engine engine) throws NotConvergedException, IOException, ClassNotFoundException {
        AnotatedMatrix<String, String> am = createAM();
        LSI<String, String> lsi = new LSI<String, String>(am,engine);
        lsi.normalizeRows();
        FlexCompRowMatrix matrix = lsi.getAm().getMatrix();
        assertEquals(0.5773, matrix.get(1, 0), 0.0010);
        lsi.setRank(2);
        assertEquals("rank of SVD", 2, lsi.getRank());
        lsi.compute();
        assertEquals("rank of SVD", 2, lsi.getRank());
        DenseMatrix dm = lsi.getU();
        double[] s = lsi.getS();
//        for (int i = 0; i < dm.numRows(); i++) {
//            for (int j = 0; j < dm.numColumns(); j++) {
//                System.out.print(" " + dm.get(i, j) * s[j]);
//            }
//            System.out.println();
//        }

        // testing query
        SparseVector q = new SparseVector(9);
        double val = 0.577;
        q.set(1, val);
        q.set(4, val);
        q.set(7, val);


        Vector result = lsi.query(q);
        assertEquals(0.9788, result.get(0), 0.0010);
        assertEquals(0.8716, result.get(1), 0.0010);
        assertEquals(1, result.get(2), 0.0010);
        assertEquals(0.9760, result.get(3), 0.0010);
        assertEquals(0.1923, result.get(4), 0.0010);
        assertEquals(0.2328, result.get(5), 0.0010);
        assertEquals(0.1923, result.get(6), 0.0010);
//        for (int i = 0 ; i < result.size() ; i++) {
//            System.out.print(" " + result.get(i));
//        }
        q = am.getMatrix().getRow(0);
        result = lsi.query(q);
        for (int i = 0 ; i < result.size() ; i++) {
            System.out.println(result.get(i));
        }

        // test engine persistence
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(lsi);
        

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        
        LSI newLsi = (LSI) ois.readObject();
        Matrix m = newLsi.getU().scale(-1);
        double norm = m.add(lsi.getU()).norm(Matrix.Norm.Frobenius);
        assertEquals("the same u",norm,norm, 1e-14);
        
        m = newLsi.getVt().scale(-1);
        norm = m.add(lsi.getVt()).norm(Matrix.Norm.Frobenius);
        assertEquals("the same vt",norm,norm, 1e-14);
        
        DenseVector s1 = new DenseVector(newLsi.getS());
        DenseVector s2 = new DenseVector(lsi.getS());

        s1.scale(-1);
        norm = s1.add(s2).norm(Vector.Norm.One);
        assertEquals("the same s",norm,norm, 1e-14);


    }


}

