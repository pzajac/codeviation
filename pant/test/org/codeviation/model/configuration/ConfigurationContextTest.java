
package org.codeviation.model.configuration;

import java.io.File;
import java.util.Random;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.codeviation.model.DefaultLookupUtil;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.model.TestUtil;

/**
 *
 * @author pzajac
 */
public class ConfigurationContextTest extends TestCase{
    public static final String repName1 = "testrep1";
    public static final String repName2 = "testrep2";
    Logger logger = Logger.getLogger(ConfigurationContextTest.class.getName());
     int finishedThreads ;
     static final int threadsCount = 10;
     Random random = new Random();
     
    public ConfigurationContextTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        DefaultLookupUtil.setLookup(new Object[]{new TestConfiguratioProvider()});
    }
    
    public void testConfigurationProviderRegistration() throws InterruptedException {
        // create testing repository
         File cacheFolder = TestUtil.getTmpFolder("RepositoryTest.test1");
        cacheFolder.mkdirs();
        assertTrue(cacheFolder.isDirectory());
        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER, cacheFolder.getAbsolutePath());
        PersistenceManager manager = PersistenceManager.getDefault();
        assertTrue(manager.getFolder().exists());
        File repFile1 = TestUtil.getTmpFolder(repName1);
        File repFile2 = TestUtil.getTmpFolder(repName2);
        repFile1.mkdirs();
        repFile2.mkdirs();
        Repository rep1 = manager.getOrCreateRepository(repFile1,repName1);
        Repository rep2 = manager.getOrCreateRepository(repFile2,repName2); 
        assertNotSame(rep2, rep1);
        // create testing ConfigurationProvider
        for (int i = 0 ; i < threadsCount ; i++) {
            Thread th = new Thread(new WaitingRunnable());
            th.start();
        }
        synchronized(this) {
            if (finishedThreads < threadsCount) {
                 wait();
            }
        }
        // test it 
    }
    
     
     class WaitingRunnable implements Runnable {

        public void run() {
            try {
                for (int i = 0 ; i < 10; i++) {
                    Thread.sleep(Math.abs(random.nextInt()/1000000000));
                    Repository rep = PersistenceManager.getDefault().getRepository(repName1);
                    
                    assertSame("" + i + "," + rep, rep, ConfigurationContext.getCurrent().getParam(TestConfiguratioProvider.class));
                    Thread.sleep(Math.abs(random.nextInt()/1000000000));
                    rep = PersistenceManager.getDefault().getRepository(repName2);
                    assertSame("" + i + "," + rep,rep, ConfigurationContext.getCurrent().getParam(TestConfiguratioProvider.class));
                }
                synchronized (ConfigurationContextTest.this) {
                    if (++finishedThreads == threadsCount) {
                        ConfigurationContextTest.this.notify();
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Assert.fail();
            }
        }
        
     }
     class TestConfiguratioProvider implements  ConfigurationProvider {

        public void register(ConfigurationContext context, Repository oldRepository, Repository newRepository) {
           context.setParam(TestConfiguratioProvider.class, newRepository);
            context.getId();
        }

        private String getNameRep(Repository rep) {
            if (rep == null) {
                return "null";
            } else {
                return rep.getName();
            }
        }
        
    }
}
