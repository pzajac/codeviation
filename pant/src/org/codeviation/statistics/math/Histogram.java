
package org.codeviation.statistics.math;

import java.util.ArrayList;
import java.util.List;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.codeviation.math.LinearRegression;
import org.codeviation.math.PolynomialLinearRegression;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

/**
 *Generates histogram from x -> y values.
 * @author pzajac
 */
public final class Histogram implements java.io.Serializable{
   private static final long serialVersionUID = 2;

    
    /**
     * Type of graph shown in FreeChart.
     */
    public enum GraphType  {
        /** probability density function
         */ 
        PROPABILITY_DENSITY,
        /** probability distribution function
         */
        PROBABILITY_DISTRIBUTION,
        /**
         * Linear regression y = ax + b
         */
        REGRESSION
    }
    
    List<Double> keys = new ArrayList<Double>();
    List<Double> values = new ArrayList<Double>();
    double min;
    double max;
    double diff;
    int steps;
    boolean computed;
    double groups[];
    final static int DEFAULT_STEPS = 50;

    //XXX - remove transient after computing of RQR2007 data 
    transient int regressionDeg;
    /** if is > 0:
     *    XYSeries will contains points with this distance 
     */ 
    transient double twoPointsDistance;
    transient double xFactor;
    transient double yFactor;
    
    // on not null groups /= divideGroups
    transient double divideGroups[];
    //XXX
    static final double EPSILON = 1e-10;
    // total number of elements
    int counts;
    public void addValue(double x,double y) {
        counts++;
        keys.add(x);
        values.add(y);
        computed = false;
    }
    
    public double getMax() {
        if (!computed) {
            throw new IllegalStateException("computed = false");
        }
        return max;
    }

    public double getMin() {
        if (!computed) {
            throw new IllegalStateException("computed = false");
        }
        return min;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    /** generate histogram from values and keys
     *  @param normalize normalizes the return values to 1
     *  @return histogram
     */
    public double[] generate(boolean normalize) {
        if (steps < 1) {
            throw new IllegalStateException("Wrong steps number : " + steps);
        }
        initMinMax();
        
        double delta = steps/(max - min) ;
        groups = new double[steps];
        
        for (int i = 0 ; i < keys.size() ; i++) {
            double val = values.get(i);
            double key = keys.get(i);
            double dblIdx = (key - min)*delta;
            int groupI = (int) dblIdx;
            if (groupI >= steps) {
                groupI = steps - 1;
            } 
            groups[groupI]+= val;
        }
        if (divideGroups != null) {
            for (int i = 0 ; i < groups.length ; i++) {
               groups[i] /= divideGroups[i];
            }
        }
        // normalize values
        if (normalize) {
            double sum = 0;
            for (double val : groups) {
                    sum += val;
            }
            for (int i = 0 ; i < groups.length ; i++) {
                groups[i] /= sum;
            }
        }
        computed = true;
        return groups;
    }

    /** Get JFreeChart's data
     * @param normalized - normalize histogram
     * @param name - label of series
     * @return XYSeries of histogram
     */
    public XYSeries getXYSeries(boolean normalized, String name) {
        if (steps < 1) {
            steps = DEFAULT_STEPS;
        }
        generate(normalized);
        XYSeries series = new XYSeries(name);
        double delta = (getMax() - getMin()) /steps;
        for (int g = 0 ; g < groups.length ; g++ ) {
            series.add(getMin() + g * delta,groups[g]);
        }
        filterSeries(series);
        return series;
    }
    
    public void divideByHistogram(Histogram hist) {
        divideGroups = hist.generate(true);
    }
    /** generate series for ratio h1/h2
     * XXX not finished and testesd
     * @param normalized - normalize histogram
     * @param name - label of series
     * @return XYSeries of histogram
     */
    public static XYSeries getHistogramsRatio(boolean normalized,Histogram h1,Histogram h2,String name,double minKey, double maxKey) {
        if (h1.steps < 1 || h2.steps < 1) {
            h1.steps = h2.steps = DEFAULT_STEPS;
        }
        if (h1.steps != h2.steps) {
            throw new IllegalStateException("Different steps : " + h1.steps + "," + h2.steps);
        }
        h1.initMinMax();
        h2.initMinMax();
        h1.addValue(h2.getMin(),0);
        h2.addValue(h1.getMin(),0);
        h1.addValue(h2.getMax(),0);
        h2.addValue(h1.getMax(),0);
        h1.generate(normalized);
        h2.generate(normalized);
        XYSeries series = new XYSeries(name);
        double delta = (h1.getMax() - h1.getMin()) /h1.steps;
        for (int g = 0 ; g < h1.groups.length ; g++ ) {
            double val1 = h1.groups[g];
            double val2 = h2.groups[g];
            if (Math.abs(val2) > EPSILON) { 
                series.add(h1.getMin() + g *delta,val1/val2);
            }
        }
        return series;
    }
    /** Get JFreeChart's data
     * @param normalized - normalize histogram
     * @param name - label of series
     * @param type - (histogram or distribution)
     * @return XYSeries of histogram
     */
    public XYSeries getXYSeries(boolean normalized, String name,double minKey,double maxKey,GraphType type) {
        if (steps < 1) {
            steps = DEFAULT_STEPS;
        }        

        generate(normalized);
        if (getMin() > minKey) {
            minKey = getMin();
        }
        if (getMax() < maxKey) {
            maxKey = getMax();
        }        
        XYSeries series = new XYSeries(name);
        double delta = (getMax() - getMin()) /steps;
        double value = 0;
        double x = 0;
        for (int g = 0 ; g < groups.length && x < maxKey ; g++ ) {
            x = getMin() + g * delta;
            if (type != GraphType.PROBABILITY_DISTRIBUTION) {
                value = 0;
            }            
            if (x >= minKey && x <= maxKey) {
                value += groups[g];
                series.add(x,value);
            }
        }
        // linear regression
        if (type == GraphType.REGRESSION && series.getItemCount() > 0)  {
            Vector xCoords = new DenseVector(series.getItemCount());
            Vector yCoords = new DenseVector(series.getItemCount());
            for (int i = 0 ; i < series.getItemCount() ; i++) {
                xCoords.set(i,series.getX(i).doubleValue());
                yCoords.set(i,series.getY(i).doubleValue());
            }
            series.clear();
            if (regressionDeg < 2 ) {
            // y = p[0]*x + p[1]
                double coefs[] = LinearRegression.solve(xCoords, yCoords);
                series.add(minKey, coefs[0] + coefs[1]*minKey);
                series.add(maxKey, coefs[0] + coefs[1]*maxKey);
            } else {
                PolynomialLinearRegression plr = new PolynomialLinearRegression(regressionDeg,xCoords,yCoords);
                double xdelta = (maxKey - minKey)/steps;  
                
                for (x = minKey ; x <= maxKey ; x += xdelta) {
                    double y = plr.getAproximatedValue(x);
                    series.add(x,y);
                }
                
            }
        }
        filterSeries(series);
        return series;
    }
    
    public boolean isEmpty() {
        return keys.isEmpty();
    }
    private void initMinMax() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        for (int i = 0 ; i < keys.size() ; i++) {
            double val = keys.get(i);
            if (min > val ) {
                min = val;
            }
            if (max < val) {
                max = val;
            }
        }
    }
    
       /**
        * @return number of added items
        */
       public int getCounts() {
           return counts;
 
    }
       
    public double[] getGroups() {
        return groups;
    }
    
    public void setRegressionDeg(int deg) {
        this.regressionDeg = deg;       
    }

   

    public void setTwoPointsDistance(double twoPointsDistance,double xFactor,double yFactor) {
        this.twoPointsDistance = twoPointsDistance;
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }    
    
    private void filterSeries(XYSeries series) {
        if (twoPointsDistance > 0) {
            for (int i = 1  ; i < series.getItemCount() - 1 ;) {
                XYDataItem current = series.getDataItem(i);
                XYDataItem prev = series.getDataItem(i - 1);
                double xDiff = current.getX().doubleValue() - prev.getX().doubleValue();
                double yDiff = current.getY().doubleValue() - prev.getY().doubleValue();
                double dist = Math.sqrt(xDiff*xDiff*xFactor*xFactor + yDiff*yDiff*yFactor*yFactor);
                if (dist < twoPointsDistance) {
                    series.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
}

