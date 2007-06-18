
package org.codeviation.statistics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
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
            float width = 3.0f;    
            BasicStroke strokes[] = new BasicStroke[] {
            new BasicStroke(width),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{4,4}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{10,4}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{16,6}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{16,4,4,4}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{12,4,4,4,4,4}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{4,4,12,4,12,4}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{12,2,12,2,2,2}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_BUTT, BasicStroke.CAP_BUTT, 2.0f, new float[]{8,2,8,2}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{4,4,1}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{1,1}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f),
            new BasicStroke(width,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{0,2,0,2,0,6}, 0.0f)
        };
        Paint[] defaultPaints = new Paint[]{
          ChartColor.GREEN, 
          Color.RED,
          ChartColor.BLUE,
          ChartColor.VERY_DARK_MAGENTA,
          ChartColor.VERY_DARK_YELLOW,
          ChartColor.VERY_DARK_CYAN,
          ChartColor.ORANGE,
          ChartColor.VERY_DARK_RED,
          Color.BLACK
        };
            
        // fix graphs
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer render =  (XYLineAndShapeRenderer) plot.getRenderer();
        for (int si  = 0 ; si <  series ; si++) {
            render.setSeriesStroke(si, strokes[si]);    
        }
        for (int si  = 0 ; si <  series && si <defaultPaints.length ; si++) {
            render.setSeriesPaint(si,  defaultPaints[si]);    
        }
        
        chart.setBackgroundPaint(Color.WHITE);
        chart.setTitle((String)null);
        plot.setBackgroundPaint(Color.WHITE);
   //     plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);

        render.setLegendLine(new Line2D.Double(-18.0, 0.0, 18.0, 0.0));
        render.setBaseStroke(new BasicStroke(4));
        render.setOutlineStroke(new BasicStroke(4));
        Font font = new Font("SansSerif", Font.PLAIN, 26);
        render.setItemLabelFont(font);
        render.setBaseItemLabelFont(font);
        chart.getLegend().setItemFont(font);
        chart.getLegend().setWidth(800);
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
