
package org.codeviation.main;

import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public class ClassRankMatrixTest extends TestCase{
    
    /** Creates a new instance of ClassRankMatrixTest */
    public ClassRankMatrixTest(String name) {
        super(name);
    }
    
    public void testPageRankDemo() {
        String N1 = "1",N2 = "2", N3="3",N4="4",N5 = "5",N6 = "6";
        ClassRankMatrixGenerator cr = new ClassRankMatrixGenerator();
        cr.initItem(N1,null);
        cr.initItem(N2,null);
        cr.initItem(N3,null);
        cr.initItem(N4,null);
        cr.initItem(N5,null);
        cr.initItem(N6,null);
        int i1 =cr.getItemIndex(N1);
        int i2 =cr.getItemIndex(N2);
        int i3 =cr.getItemIndex(N3);
        int i4 =cr.getItemIndex(N4);
        int i5 =cr.getItemIndex(N5);
        int i6 =cr.getItemIndex(N6);
        
        cr.addClassUsage(N1, N2);
        cr.addClassUsage(N1, N3);

        
        cr.addClassUsage(N3, N1);
        cr.addClassUsage(N3, N2);
        cr.addClassUsage(N3, N5);
        
        cr.addClassUsage(N4, N5);
        cr.addClassUsage(N4, N6);
        
        cr.addClassUsage(N5, N4);
        cr.addClassUsage(N5, N6);
        
        cr.addClassUsage(N6, N4);
        
        double alpha = .9;
        cr.normalizeMatrix(alpha);
        
        Matrix m = cr.getMatrix();
        assertEquals("numColumns",6,m.numColumns());
        assertEquals("numRows",6,m.numRows());
        
        assertDbl(alpha*0.5,m.get(i1,i2));
        assertDbl(alpha*0.5,m.get(i1,i3));
        assertDbl(0.0,m.get(i2,i1));
        assertDbl(0.0,m.get(i2,i2));
        assertDbl(0.0,m.get(i2,i5));
        assertDbl(alpha*1.0/3,m.get(i3, i1));
        assertDbl(alpha*1.0/3,m.get(i3,i2));
        assertDbl(alpha*1.0/3,m.get(i3, i5));
        assertDbl(alpha,m.get(i6,i4));
        
        Vector vec = new DenseVector(cr.size());
        double initValue = 1;
        for (int i = 0 ; i < vec.size() ; i++) {
            vec.set(i, initValue);
            
        }
        int iterations = cr.compute(vec);
        assertTrue("matrix has convert", iterations != -1);
        assertDbl(0.03721,vec.get(i1));
        assertDbl(0.05396,vec.get(i2));
        assertDbl(0.04151,vec.get(i3));
        assertDbl(0.3751,vec.get(i4));
        assertDbl(0.206,vec.get(i5));
        assertDbl(0.2862,vec.get(i6));
    }
 
    public void testSimpliestPageRank() {
        ClassRankMatrixGenerator cr = new ClassRankMatrixGenerator();
        cr.initItem("A",null);
        cr.initItem("B",null);
        cr.initItem("C",null);
        cr.addClassUsage("A", "B");
        cr.addClassUsage("B","C");
        cr.normalizeMatrix(0.9);
        Vector initV = new DenseVector(new double[]{1,1,1});
        int iter = cr.compute(initV);
        assertTrue("convergence",iter != -1);
        assertDbl(0.17825311942959005,initV.get(cr.getItemIndex("A")));
        assertDbl(0.33868092691622104,initV.get(cr.getItemIndex("B")));
        assertDbl(0.48306595365418903,initV.get(cr.getItemIndex("C")));
    }
    public void assertDbl(double val1,double val2) {
        final double EPSILON = 1e-4;
        assertTrue("assert double: " + val1 + " != " + val2 ,  Math.abs(val1 - val2) < EPSILON);
        
    }
}
