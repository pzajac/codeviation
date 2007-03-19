
package org.codeviation.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

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
}
