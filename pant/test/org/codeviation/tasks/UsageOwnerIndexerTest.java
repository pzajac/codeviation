package org.codeviation.tasks;

import junit.framework.TestCase;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.TestUtil;
import org.codeviation.model.vcs.ExamplesSetup;

/**
 *
 * @author pzajac
 */
public class UsageOwnerIndexerTest extends TestCase {
    
    public UsageOwnerIndexerTest(String testName) {
        super(testName);
    }
    
    public void test1() {
        ExamplesSetup.initNbCvsTestingCache();
        UsageOwnerIndexer indexer = new UsageOwnerIndexer();
        indexer.setOutDir(TestUtil.getTmpFolder("usagemetricsIndexer"));
        Repository rep = PersistenceManager.getDefault().getRepository("nbcvs");
        RepositoryProcessEnv env = new RepositoryProcessEnv();
        env.setWorkDir(TestUtil.getTmpFolder("UsageOwnerIndexer"));
        indexer.execute(rep,env);    
    }
    
}
