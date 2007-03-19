
package org.codeviation.tasks;

import java.io.File;
import java.text.ParseException;
import java.util.Set;
import junit.framework.TestCase;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class IssuesPageRankProcessTest extends TestCase {
    
    public IssuesPageRankProcessTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        ExamplesSetup.initNbCvsTestingCache();
    }

    public void testOpenideFs() throws ParseException {
        final String SRC_ROOT = "openide/fs/src";
        Repository rep =  PersistenceManager.getDefault().getRepository("nbcvs");
        Set<String> tags = rep.getSourceRoot(SRC_ROOT).getCvsTags();
        RepositoryProcess process = new IssuesPageRankProcess();
        File pwd = new File(TestUtil.getWorkDir(),"IssuesPageRankProcessTest.testOpenideFs");
        TestUtil.deleteFile(pwd);
        pwd.mkdirs();
        RepositoryProcessEnv env = new RepositoryProcessEnv();
        env.setWorkDir(pwd);
        env.setTags(tags);
        env.setSourceRootFilter(new SourceRootFilter() {
            public boolean accept(SourceRoot srcRoot) {
                return srcRoot.getRelPath().equals(SRC_ROOT);
            }
        });
        process.execute(rep, env);
    }
}
