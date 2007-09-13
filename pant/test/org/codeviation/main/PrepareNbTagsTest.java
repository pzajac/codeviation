/*
 * PrepareNbTagsTest.java
 * JUnit based test
 *
 * Created on January 1, 2007, 2:33 PM
 */

package org.codeviation.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author pzajac
 */
public class PrepareNbTagsTest extends TestCase {
    
    public PrepareNbTagsTest(String testName) {
        super(testName);
    }
    
    public void testParseLog() throws IOException {
        // create logfile
        File logFile = createLogFile();
        // create outfile
        File outFile = File.createTempFile("PrepareNbTags", "outFile");
        try {
            // execute
            String args[] = new String[] {
                logFile.getAbsolutePath(),
                outFile.getAbsolutePath(),
                "200602010000",
                "200612010000",
                "24"
            };
            PrepareNbTags.main(args);
            byte bytes [] = new byte[(int)outFile.length()];
            FileInputStream fis = new FileInputStream(outFile);
            try {
                fis.read(bytes);
            } finally {
                fis.close();
            }
            String outStr = new String(bytes);
            String refStr = "BLD200602011032\n" +
                    "BLD200602261900\n" +
                    "BLD200603231900\n" +
                    "BLD200604171800\n" +
                    "BLD200605141800\n" +
                    "BLD200606081800\n" +
                    "BLD200607031800\n" +
                    "BLD200607281800\n" +
                    "BLD200608221800\n" +
                    "BLD200609161800\n" +
                    "BLD200610111800\n" +
                    "BLD200611041900\n" +
                    "BLD200611300230\n" ;
            assertEquals("Filtered builds ",refStr,outStr);
            // compare outfiles
        } finally {
            logFile.delete();
            //           outFile.delete();
        }
    }
    
    public void testGenTwoYears() throws IOException {
        File logFile = createLogFile();
        // create outfile
        File outFile = File.createTempFile("PrepareNbTags", "outFileTwo");
        // execute
        String args[] = new String[] {
            logFile.getAbsolutePath(),
            outFile.getAbsolutePath(),
            "200501011900",
            "200612011900",
            "14"
        };
        PrepareNbTags.main(args);
    }
    private File createLogFile() throws IOException {
        File file = File.createTempFile("PrepareNbTags", "logFile");
        FileOutputStream fos = new FileOutputStream(file);
        try {
            InputStream is  = getClass().getResourceAsStream("build.xml.log");
            try {
                byte buff[] = new byte[100000];
                int size = -1;
                while ((size = is.read(buff) ) != -1) {
                    fos.write(buff, 0, size);
                }
            } finally {
                is.close();
            }
        } finally {
            fos.close();
        }
        return file;
    }
    
    public void testParseTagDate() throws ParseException {
        SimpleDateFormat tagDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = PrepareNbTags.parseTagDate("BLD200401020300");
        assertEquals("20040102030000",tagDateFormat.format(date));
        date = PrepareNbTags.parseTagDate("2005/01/02 03:00");
        assertEquals("20050102030000",tagDateFormat.format(date));        
        date = PrepareNbTags.parseTagDate("2005/01/02 03:00:01");
        assertEquals("20050102030001",tagDateFormat.format(date));        
        date = PrepareNbTags.parseTagDate("2005/01/02");
        assertEquals("20050102000000",tagDateFormat.format(date));        
    }
}
