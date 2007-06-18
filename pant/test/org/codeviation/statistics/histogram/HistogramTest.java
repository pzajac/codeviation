
package org.codeviation.statistics.histogram;

import junit.framework.TestCase;
import org.codeviation.statistics.math.Histogram;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author pzajac
 */
public class HistogramTest extends TestCase {
    
    public HistogramTest(String testName) {
        super(testName);
    }
    
    public void testHistogram() {
        Histogram hist = new Histogram();
        try {
          hist.generate(true);
          fail("IllegalStateExpception");
        } catch (IllegalStateException ise) {
            
        }
        hist.addValue(0.1, 1);
        hist.addValue(0.1, 1);
        hist.addValue(0.2, 1);
        hist.addValue(0.3, 2);
        hist.addValue(2, 1);
        hist.addValue(3, 1);
        
        try {
            hist.getMax();
            fail("IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
        try {
            hist.getMin();
            fail("IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
        hist.setSteps(3);
        
        double vals[] = hist.generate(false);
        double EPSILON = 1e-10;
        assertEquals(5,vals[0], EPSILON);
        assertEquals(1,vals[1], EPSILON);
        assertEquals(1,vals[2], EPSILON);
        
        assertEquals(0.1,hist.getMin(),EPSILON);
        assertEquals(3,hist.getMax(),EPSILON);
        
        vals = hist.generate(true);
        double sum = 1.0 + 0.2 + 0.2;
        assertEquals(1.0/sum,vals[0], EPSILON);
        assertEquals(0.2/sum,vals[1], EPSILON);
        assertEquals(0.2/sum,vals[2], EPSILON);
        
        assertEquals(0.1,hist.getMin(),EPSILON);
        assertEquals(3,hist.getMax(),EPSILON);
        
        XYSeries series =  hist.getXYSeries(true, "Test");
        assertEquals(1.0/sum,series.getY(0).doubleValue(), EPSILON);
        assertEquals(0.2/sum,series.getY(1).doubleValue(), EPSILON);
        assertEquals(0.2/sum,series.getY(2).doubleValue(), EPSILON);
       
        assertEquals(0.1,series.getX(0).doubleValue(), EPSILON);
        assertEquals(0.1 + (3 - 0.1)/3. ,series.getX(1).doubleValue(), EPSILON);
        assertEquals(0.1 + 2*(3 - 0.1)/3.,series.getX(2).doubleValue(), EPSILON);

    }          
    public void testHistogramDistributionDensityFunction() {
        Histogram hist = new Histogram();
        hist.setSteps(30);
        for (int i = 0 ; i < 100 ; i++) {
            hist.addValue(Math.random()*20,Math.random()*15);
        }
        XYSeries xYSeries = hist.getXYSeries(true, "test",0,20,Histogram.GraphType.PROBABILITY_DISTRIBUTION);
        assertEquals("test max value of distribution function", 1,1,1e-8);
    }
    
    public void testFilterSeries() {
        Histogram hist = new Histogram();
        hist.setSteps(50);
    
        double x = 0;
        double y = 0;
        for (int i = 0 ; i < 100 ; i++) {
            x += 0.1;
            y += 0.2;
            hist.addValue(x,y);
        }
        XYSeries ser1 = hist.getXYSeries(true, "ahoj");
        assertEquals(50,ser1.getItemCount(),50); 
        
        hist.setTwoPointsDistance(2,1,1);
        ser1 = hist.getXYSeries(true, "ahoj");
        double xd =  ser1.getX(0).doubleValue() - ser1.getX(1).doubleValue();
        double yd =  ser1.getY(0).doubleValue() - ser1.getY(1).doubleValue();
        double diff = Math.sqrt(xd*xd + yd*yd);
        assertTrue( diff >=  2);
    }
}
