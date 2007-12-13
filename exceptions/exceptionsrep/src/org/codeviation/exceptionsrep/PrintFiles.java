/*
 * TransactionReview.java
 * 
 * Created on Aug 16, 2007, 10:15:38 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.exceptionsrep;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import no.uib.cipr.matrix.NotConvergedException;
import org.codeviation.javac.UsageItem;
import org.codeviation.math.LSI;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Line;
import org.codeviation.model.Package;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;

/**
 * Choose best candidate for maintaining module. 
 * @author pzajac
 */
public class PrintFiles {

    
    LSI<String,ArrayList<UsageItem>> lsi;
     

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, NotConvergedException {
        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, "/cvss/pantcache");
        CVSMetric.setUpdateCVS(false);

        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        //SourceRoot srcRoot = rep.getSourceRoot("apisupport/project/src");
        SourceRoot srcRoot = rep.getSourceRoot("openide/fs/src");
//               if (srcRoot.getRelPath().indexOf("project") != -1) {
        for (Package pack : srcRoot.getPackages()) {
            for (JavaFile jf : pack) {
                System.out.println(jf.getName());
                CVSMetric cvsm = jf.getCVSResultMetric();
                if (cvsm != null) {
                    Version version = cvsm.getRootVersion();
                    if (version != null) {

                        while (true) {
                            Version v2 = version.getNext();
                            if (v2 == null) {
                                break;
                            }
                            version = v2;
                        }
                        System.out.println(version);
                        List<Line> lines = jf.getLines(version);
                        for (int i = 0; i < lines.size() && i < 5; i++) {
                            Line line = lines.get(i);
                            Version v = line.getPosition().getVersion();
                            System.out.println(v.getRevision() + " " + v.getUser() + " " + line.getNewContent());
                        }
                    } else {
                        System.out.println("null");
                    }
                } else {
                    System.out.println("null cvsm");
                }
            }
        }

           // SourceRoot rel path x UsageItem
           
    
    }    
}
