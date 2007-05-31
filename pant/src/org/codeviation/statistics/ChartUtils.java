
package org.codeviation.statistics;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

/**
 *
 * @author pzajac
 */
public final class ChartUtils {
    

    /**
     * Writes content of chart to file as PNG
     * @param file output file
     * @param chart chart value 
     * @param xSize image's width in pixels 
     * @param ySize image's higth in pixel
     * @throws java.io.IOException on IO errors
     */
    public static void chartToFile(File file, JFreeChart chart,int xSize,int ySize) throws IOException {
            FileOutputStream fos = new FileOutputStream(file);
            ChartUtilities.writeChartAsPNG(fos,chart,xSize,ySize);
            fos.close();
    }
    
    public static void makeSeriesChartPrintable(JFreeChart chart,int series) {
            BasicStroke strokes[] = new BasicStroke[] {
            new BasicStroke(4.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{2,2,2,2
            }, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{4,4,4}, 0.0f),
            new BasicStroke(2.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 1.0f, new float[]{3,3}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{4,2,2}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{2,2,6}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{2,2,8}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{6,6,6,6}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{6,1,6,1}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f),
            new BasicStroke(4.0f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f)
        };
       
        // fix graphs
        XYLineAndShapeRenderer render =  (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        for (int si  = 0 ; si <  series ; si++) {
            render.setSeriesStroke(si, strokes[si]);    
        }
        render.setLegendLine(new Line2D.Double(-14.0, 0.0, 14.0, 0.0));
        render.setBaseStroke(new BasicStroke(4));
        render.setOutlineStroke(new BasicStroke(4));
        Font font = new Font("SansSerif", Font.PLAIN, 26);
        render.setItemLabelFont(font);
        render.setBaseItemLabelFont(font);
        chart.getLegend().setItemFont(font);
        chart.getLegend().setWidth(600);
        ValueAxis axis = chart.getXYPlot().getDomainAxis();
        chart.getXYPlot().setDomainGridlineStroke(new BasicStroke(2));
        chart.getXYPlot().setRangeGridlineStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 1.0f, new float[]{2,6,2,6
            }, 0.0f));
        axis.setAxisLineStroke(new BasicStroke(2));
        axis.setLabelFont(font);
        axis.setTickLabelFont(font);
        axis = chart.getXYPlot().getRangeAxis();
        axis.setLabelFont(font);
        axis.setTickLabelFont(font);
        axis.setAxisLineStroke(new BasicStroke(2));
        chart.setBorderStroke(new BasicStroke(4));
//        
//        File pngFile = new java.io.File(env.getWorkDir(), fileName);
//          ChartPanel chartpanel = new ChartPanel(chart, true, true, true, false, true);
//        chartpanel.setPreferredSize(new Dimension(500, 270));

    }
}
