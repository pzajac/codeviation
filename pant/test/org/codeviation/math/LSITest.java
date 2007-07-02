/*
 * LSITest.java
 * JUnit based test
 *
 * Created on June 13, 2007, 10:04 PM
 */

package org.codeviation.math;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
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

    /** Test example from lsi chapter of 
     * Helsinki univesity
     * lecture7.pdf
     */ 
    public void  testExampleHelsinki() throws NotConvergedException {
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
         for (int i = 0 ; i < 8 ; i++) {
            documents.add("D" + i);
         }
         AnotatedMatrix<String,String> am = new AnotatedMatrix<String,String>(mat,documents,terms);
         LSI<String,String> lsi = new LSI<String,String>(am);
         lsi.normalizeRows();
         FlexCompRowMatrix matrix = lsi.getAm().getMatrix();
         assertEquals(0.5773,matrix.get(1,0),1e-3);
         lsi.setRank(2);
         assertEquals("rank of SVD",2,lsi.getRank());
         lsi.compute();
         assertEquals("rank of SVD",2,lsi.getRank());
         DenseMatrix dm = lsi.getSvd().getU();
         double s[] = lsi.getSvd().getS();
//         for (int i = 0 ; i < dm.numRows() ; i++) {
//             for (int j = 0 ; j < dm.numColumns() ; j++) {
//                    System.out.print(" " + dm.get(i, j)*s[j] );
//             }
//             System.out.println();   
//        }
         
        // testing query 
        SparseVector q = new SparseVector(9);
        double val = 0.577;
        q.set(1,val);
        q.set(4,val);
        q.set(7,val);
        

        Vector result = lsi.query(q);
        System.out.println("result:");
        assertEquals(0.3956,result.get(0),1e-3);
        assertEquals(0.4532,result.get(1),1e-3);
        assertEquals(0.4658,result.get(2),1e-3);
        assertEquals(0.5348,result.get(3),1e-3);
        assertEquals(0.1096,result.get(4),1e-3);
        assertEquals(-0.1284,result.get(5),1e-3);
        assertEquals(0.10967,result.get(6),1e-3);
//        for (int i = 0 ; i < result.size() ; i++) {
//            System.out.print(" " + result.get(i));
//        }
        
    }   
}

