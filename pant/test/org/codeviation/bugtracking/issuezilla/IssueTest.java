
package org.codeviation.bugtracking.issuezilla;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;
import org.codeviation.model.Version;
import org.codeviation.model.Version.State;

/**
 *
 * @author pzajac
 */
public class IssueTest extends TestCase {
    
    public IssueTest(String testName) {
        super(testName);
    }

    public void testIssue() throws SQLException, IOException {
        Issue issue = Issue.readIssue(65440);
        assertNotNull(issue);
        assertEquals("Compoment is Editor.", "editor",issue.getComponent());
    }
    
    public void testVersionIssues() throws SQLException, IOException, ParseException{
        Issue issue = Issue.readIssue(65352);
        Version version = new Version("1.22",
                                     "#65352 - do not set ",getDate("2005/09/27 15:09:13"),"rondruska",State.EXP);
        int issues[] = version.getDefectNumbers();
        assertEquals(1,issues.length);
        assertEquals(65352,issues[0]);
        assertEquals("Fix of defect",Version.IssueType.DEFECT, version.getIssueType());
    }
    
    static private Date getDate(String value) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        return sdf.parse(value);
    }
}
