/*
 * AnnotatedMatrixTest.java
 * 
 * Created on Jun 8, 2007, 8:27:41 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author pzajac
 */
public class AnnotatedMatrixTest extends TestCase {

    public AnnotatedMatrixTest(String name) {
        super(name);
    }

    public void testPersistence() throws IOException, ClassNotFoundException {
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        FlexCompRowMatrix matrix = new FlexCompRowMatrix(2,2);
        matrix.set(0, 0, 2);
        matrix.set(1,1,4);
        
        List<String> rows = new ArrayList<String>();
        rows.add(a);
        rows.add(b);
        
        List<String> cols = new ArrayList<String>();
        cols.add(c);
        cols.add(d);
        
        AnotatedMatrix<String,String> mat = new AnotatedMatrix<String,String>(matrix,rows,cols);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(mat);
        
       ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
       ObjectInputStream ois = new ObjectInputStream(bis);
       
        @SuppressWarnings("unchecked")
        AnotatedMatrix<String,String> mat2 = (AnotatedMatrix<String, String>) ois.readObject();
       
       assertTrue("Rows",mat.getRows().equals(mat2.getRows()));
       assertTrue("Columns",mat.getColumns().equals(mat2.getColumns()));
       
       FlexCompRowMatrix matrix2 = mat2.getMatrix();
       
       assertEquals("rsize",2,matrix2.numRows());
       assertEquals("csize",2,matrix2.numColumns());
       
       assertEquals(2., matrix2.get(0,0));
       assertEquals(4., matrix2.get(1,1));
       assertEquals(0., matrix2.get(1,0));
       assertEquals(0., matrix2.get(0,1));
    }
}
        