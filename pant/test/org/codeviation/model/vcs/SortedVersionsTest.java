/*
 * SortedVersionsTest.java
 * JUnit based test
 *
 * Created on July 12, 2007, 9:02 PM
 */

package org.codeviation.model.vcs;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import org.codeviation.model.JavaFile;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.TestUtil;

/**
 *
 * @author pzajac
 */
public class SortedVersionsTest extends TestCase {

    private File prj;

    public SortedVersionsTest(String testName) {
        super(testName);
    }


//    public void testExample() throws IOException, ParseException {
//        TestUtil.clearCache();
//        prj = ExamplesSetup.getTestJ2seProjectDir();
//
//        JavaFile jf = doFile("Main.java");
//        doFile("MenuBar.java");        
//        doFile("NbTopManager.java");
//        doFile("BaseOptionsBeanInfo.java");
//        Repository rep = jf.getPackage().getSourceRoot().getRepository();
//        SortedVersions sv = SortedVersions.createSortedVersions(rep);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        Date dateFrom = sdf.parse("2006101");
//        Date dateTo = sdf.parse("2008101");
//        List<SortedVersions.Item> items = sv.getItems(dateFrom, dateTo);
//        long prevTime = -1;
//        for (SortedVersions.Item it: items) {
//           long time = it.getVersion().getDate().getTime();
//           assertTrue( prevTime <= time);
//        }
//        List<List<SortedVersions.Item>> transactions = sv.getTransaction(dateFrom, dateTo);
//        assertEquals("9 transactions",9,transactions.size());
//        
////        for (List<SortedVersions.Item> tr: transactions) {
////            SortedVersions.Item item = tr.get(0);
////            System.out.println(tr.size() + ", " + item.getJavaFile() + "," + item.getDeveloper() + "," + item.getVersion());
////        }
////        
////        for (SortedVersions.Item it : items) {
////            System.out.println(it.getVersion().getDate() + ", " + it.getJavaFile().getName() + "," + it.getVersion().getRevision());
////        }
//        
//        
//    }

    public void testNbCVS() throws ParseException {
        ExamplesSetup.initNbCvsTestingCache();
        initTime();
        printTime();
        SortedVersions sv = SortedVersions.openSortedVersions(PersistenceManager.getDefault().getRepository("nbcvs"));
        printTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date dateFrom = sdf.parse("2006101");
        Date dateTo = sdf.parse("20061010");
        List<SortedVersions.Item> items = sv.getItems(dateFrom, dateTo);
        printTime();
//        dateFrom = sdf.parse("20040101");
//        dateTo = sdf.parse("20070101");
//        long step = (dateTo.getTime() - dateFrom.getTime()) /60;
//        
//        Date datePrev = new Date(dateFrom.getTime());
//        for (long time = dateFrom.getTime() ; time < dateTo.getTime() ; time+= step) {
//              Date dateNext = new Date(time);
//              items = sv.getItems(dateFrom, dateTo);
//              System.out.print(datePrev + "-> " + dateNext + ": ");
//              printTime();
//              datePrev = dateNext;
//        }
        List<Transaction> transactions = sv.getTransaction(dateFrom, dateTo);
//        assertEquals("9 transactions",9,transactions.size());
        
        for (Transaction tran: transactions) {
            List<SortedVersions.Item> tr  = tran.getItems();
            if (!tr.isEmpty()) {
                System.out.println("---");
                for (SortedVersions.Item item : tr) {
                    System.out.println( item.getJavaFile() + "," + item.getDeveloper() + "," + item.getVersion().getComment());
                }
            }
        }
        items = sv.getItems(dateFrom, dateTo);
        printTime();
        sv.closeDb();
///        for (SortedVersions.Item it : items) {
////            System.out.println(it.getVersion().getDate() + ", " + it.getJavaFile().getName() + "," + it.getVersion().getRevision());
//        }

    }
    private JavaFile  doFile(String name) throws IOException {
        File javaMainFile = new File(prj, "src/testj2seexample/" + name);
        JavaFile jf = JavaFile.getJavaFile(javaMainFile, "testj2seexample");
        return jf;
     
    }

    long startTime;
    private void initTime() {
        startTime = System.currentTimeMillis();
    }

    private void printTime() {
        long time = System.currentTimeMillis() - startTime;
        System.out.println(time);
    }
}
