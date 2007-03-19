/*
 * UsagesReporterIndexer.java
 *
 * Created on February 13, 2007, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.tasks;

import org.codeviation.tasks.UsageOwnerIndexer;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.JavaFile;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.bugtracking.issuezilla.Issue;
import org.codeviation.javac.UsageItem;

/**
 *Index all usageas where were found bugs to files with reporter's name
 * @author pzajac
 */
public class UsageReporterIndexer extends UsageOwnerIndexer {
    static Logger logger = Logger.getLogger(UsageReporterIndexer.class.getName());
    /** Creates a new instance of UsagesReporterIndexer */
    public UsageReporterIndexer() {
    }
    @Override
    protected boolean prepareFile(JavaFile jf) {
         CVSMetric cvsm = jf.getCVSResultMetric();
         if (cvsm == null) {
             return false;
         } else {
             cvsm.updateReplaceLineVersions();
             return true;
         }
    }
    @Override
    protected String getUser(PositionIntervalResult<UsageItem> usagePos) {
        try {
            Version version = usagePos.getInterval().getStartPosition().getLine().getReplaceVersion();
            if (version != null) {
                int defects[] = version.getDefectNumbers();
                for  (int id : defects) {
                   Issue issue = Issue.readIssue(defects[0]);
                   if (issue != null) {
                       return issue.getReporter();
                   }
                }
            }
        } catch (SQLException sqe) {
            logger.log(Level.SEVERE, sqe.getMessage(),sqe);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE,ioe.getMessage(),ioe);
        }
        return null;
    }
    
    @Override 
    public String getName() {
        return "UsageReporterIndexer";
    }
    
    @Override
    public String getDescription() {
        return "Usage of bug reporter before bugfix";
    }
    
}
