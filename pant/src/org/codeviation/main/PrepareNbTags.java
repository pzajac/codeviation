
package org.codeviation.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 *  It parsers a log file of nbbuild/build.xml and filters tags. 
 * @author Petr Zajac
 */
public class PrepareNbTags {
    File logFile;
    File outFile;
    Date from;
    Date to;
    int stepInDays;
    Set<Date> dates = new TreeSet<Date>();
    private static SimpleDateFormat nbSimpleFormat = new SimpleDateFormat("yyyyMMddHHmm");
    private static SimpleDateFormat tagDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    
    /** Creates a new instance of PrepareNbTags */
    public PrepareNbTags() {
    }
    
    public void execute() throws IOException{
        dates.clear();
        // parse dates
        //
        BufferedReader reader = new BufferedReader(new FileReader (logFile));
        try {
            String line = null;
            while ((line = reader.readLine()) !=  null) {
                line = line.trim();
                if (line.startsWith("BLD")) {
                    int sep = line.indexOf(":");
                    if (sep != -1) {
                        // BLDyyyMMddhhmm  
                        String dateString = line.substring(3,3 + 4 + 2 + 2 + 2 + 2);
                        try {
                            Date date = nbSimpleFormat.parse(dateString);
                            if (date.compareTo(from) > 0 && date.compareTo(to) < 0) {
                                dates.add(date);
                            }
                        } catch (ParseException pe) {
                            // nothing
                        }
                    }
                }
            }
        } finally {
            reader.close();
        }
        // filter dates
        long stepsInMilis = 1000L * 3600L * 24L * stepInDays;
        PrintWriter writer = new PrintWriter(new FileWriter(outFile));
        try {
            long nextTime = 0;
            for (Date date : dates) {
                long time = date.getTime();
                if (nextTime < time) {
                    writer.println("BLD" + nbSimpleFormat.format(date));
                    nextTime = time + stepsInMilis;
                }
            }
        } finally {
            writer.close();
        }
    }    
    
    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }
    
    public void setFromDate(String dateString) throws ParseException {
        from = parseDate(dateString);
    }
    
    public void setToDate(String dateString) throws ParseException {
        to = parseDate(dateString);
    }
    public void setStepsInDay(int steps) {
        stepInDays = steps;
    } 
    
    public static Date parseDate(String dateString) throws ParseException {
        return nbSimpleFormat.parse(dateString);
    }
    
    public static Date parseTagDate(String tagDate) throws ParseException {
        
        // NetBeans format BLDyyyyMMddHHmm
        if (tagDate.startsWith("BLD")) {
            return parseDate(tagDate.substring(3));
        } else {
            // YYYY/MM/dd HH:mm:ss
            StringTokenizer tokenizer = new StringTokenizer(tagDate,"/: ");
            StringBuilder dateBuf = new StringBuilder();
            while(tokenizer.hasMoreElements()) {
                dateBuf.append(tokenizer.nextElement());
            }
            // yyyy + MM + dd + HH + mm + ss;
            final int dateLen =  4 + 2 + 2 + 2 + 2 + 2; 
            while (dateBuf.length() < dateLen) {
                dateBuf.append("00");
            }
            String dateString = dateBuf.toString();
            if (dateString.length() > dateLen) {
                dateString = dateString.substring(0,dateLen);
            }
            return tagDateFormat.parse(dateString);
        }
    }
    /**
     * Parameters:
     *   <ul>
     *      <ol>cvs log file
     *      <ol>output of file
     *      <ol> fromDate - lower limit (yyyyMMdd)
     *      <ol> toDate - upper limit (yyyyMMdd)
     *      <ol> stepsInDays 
     *  </ul>     
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.out.println("Illegal number of parameterers.");
        }
        PrepareNbTags task = new PrepareNbTags();
        
        // parsing parameters
        //
        File logFile = new File(args[0]) ;
        if (!logFile.exists()) {
            System.out.println("Log file " + logFile + " doesn't exist.");
            return;
        }
        task.setLogFile(logFile);
        File outFile = new File(args[1]);
        if (!logFile.exists()) {
            System.out.println("Out file " + outFile + " doesn't exist.");
            return;
        }
        task.setOutFile(outFile);
        try {
            task.setFromDate(args[2]); 
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.out.println("Invalid from date " + args[2]);
            return;
        }
        try {
            task.setToDate(args[3]); 
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.out.println("Invalid to date " + args[3]);
            return;
        }
        task.setStepsInDay(Integer.parseInt(args[4]));
       
        task.execute();
    }
}
