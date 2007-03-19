
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
 *Index all usages where were found bugs to files with defeloper's name
 * @author pzajac
 */
public class UsageOwnerBeforeFixIndexer extends UsageOwnerIndexer {
    static Logger logger = Logger.getLogger(UsageReporterIndexer.class.getName());
    /** Creates a new instance of UsagesReporterIndexer */
    public UsageOwnerBeforeFixIndexer() {
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
                       return version.getUser();
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
        return "UsageOwnerBeforeFixIndexer";
    }
    
    @Override
    public String getDescription() {
        return "Usage of bug's indexed by developer before bugfix";
    }
    
}
