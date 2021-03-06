/*
 * BlocksMetricTest.java
 * JUnit based test
 *
 * Created on March 14, 2007, 8:07 PM
 */

package org.codeviation.javac;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.Iterator;
import junit.framework.TestCase;
import org.codeviation.javac.impl.blocks.Blocks;
import org.codeviation.model.JavaFile;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.PositionIntervalResultGraph.Item;
import org.codeviation.model.PositionVersionIntervalResultContainer;
import org.codeviation.model.TestUtil;
import org.codeviation.model.Version;
import org.codeviation.model.vcs.CVSMetric;
import org.codeviation.model.vcs.ExamplesSetup;
import org.codeviation.javac.impl.blocks.BlocksBuilder;
import org.codeviation.javac.impl.blocks.BlocksItem;
import org.codeviation.javac.impl.blocks.BlocksMetric;
import org.codeviation.model.PositionIntervalResultGraph;
import org.codeviation.model.VersionInterval;

/**
 *
 * @author pzajac
 */
public class BlocksMetricTest extends TestCase {
   private File testPrjF;
  
   static class BlocksHandler extends Handler {
          Set<String> values;
          List<String> positions;
          byte fileText[];
          
          BlocksHandler(Set<String> values ,List<String> positions,byte[] fileText) {
            this.values = values;
            this.positions = positions;
            this.fileText = fileText;
          }
            public void publish(LogRecord rec) {
                String tokens [] = rec.getMessage().split(":"); 
                if (tokens.length == 4 && "BlockItem".equals(tokens[0])) {
                    int start = Integer.parseInt(tokens[1]);
                    int end = Integer.parseInt(tokens[2]);
                    String type = tokens[3];
                    if (end >= fileText.length) {
                        end = fileText.length - 2;
                    }
                    String val = tokens[3] + ":"  + new String(fileText,start, end - start + 1);
                    values.add(val);
                    positions.add(tokens[0] +":" +  tokens[1] + ":" +  tokens[2]);
                }
                
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
       };
  
    public BlocksMetricTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        ExamplesSetup.checkoutExamples = false;
        TestUtil.clearCache();
        TestUtil.enableMetricLogger();
        testPrjF = ExamplesSetup.getBlockTestDir();
        MetricsRunner.clearMetrics();
        MetricsRunner.addMetric(new BlocksBuilder());
     }
    
    public void testSimple() throws IOException, InterruptedException {
       Blocks.setDebug(true); 
       String filePath = "pantexamples/testblockmetrics/src/testblockmetrics/Simple.java";
       File file = new File(testPrjF,"src/testblockmetrics/Simple.java");
       ExamplesSetup.updateFile(filePath, "1.2");
       
       final byte fileText[] = getBytes(file);
       final Set<String> values = new HashSet<String>();
       final List<String> posValues = new ArrayList<String>();
       Logger blocksLogger = Logger.getLogger(Blocks.class.getName());
       blocksLogger.setLevel(Level.FINE);
       blocksLogger.addHandler(new BlocksHandler(values,posValues,fileText));

       RunJavacTest.runJavac(testPrjF);
       JavaFile jf = JavaFile.getJavaFile(file, "testblockmetrics");
       BlocksMetric bm = jf.getMetric(BlocksMetric.class);
       assertNotNull(bm);
        CVSMetric cvsm = jf.getCVSResultMetric();
       PositionVersionIntervalResultContainer<BlocksItem> container = bm.getStorage(); 
       Set<PositionIntervalResult<BlocksItem>> results = container.getAllObjects();
       for (PositionIntervalResult<BlocksItem> result : results) {
           PositionInterval i = result.getInterval();
           String val = result.getObject() + ":" + cvsm.getContent(i.getStartPosition(),i.getEndPosition());
           assertTrue(val,values.contains(val));
       }
       
       PositionVersionIntervalResultContainer<String> classes = bm.getClasses();
       assertEquals(1,classes.getAllObjects().size());
       PositionIntervalResult<String> res = classes.getAllObjects().iterator().next();
       assertEquals("class name","testblockmetrics.Simple",res.getObject());
       System.out.println("startPosition:" + res.getInterval().getStartPosition());
       System.out.println("endPosition:" + res.getInterval().getEndPosition());
       
       PositionVersionIntervalResultContainer<String> methods = bm.getMethods();
       assertEquals(1,classes.getAllObjects().size());
       List<PositionIntervalResult<String>> pirs = new ArrayList<PositionIntervalResult<String>>(methods.getAllObjects());
       Collections.sort(pirs, new PIRComparator<String>());
       assertEquals("pirs size",2,pirs.size());
       Iterator<PositionIntervalResult<String>> pirIt = pirs.iterator();
   //    pirIt.next();
       PositionIntervalResult<String> pir = pirIt.next();
       assertEquals(pir.getObject(),"Simple(boolean)",pir.getObject());
       pir = pirIt.next();
       assertEquals(pir.getObject(),"methodA(boolean,int)",pir.getObject());
//       System.out.println("startPosition:" + pir.getInterval().getStartPosition());
//       System.out.println("endPosition:" + pir.getInterval().getEndPosition());
//        Set<PositionIntervalResult<BlocksItem>> allObjects = bm.getStorage().getAllObjects();
//        for (PositionIntervalResult<BlocksItem> positionIntervalResult : allObjects) {
//            System.out.println(positionIntervalResult.getObject());
//            System.out.println(positionIntervalResult.getInterval().getStartPosition());
//            System.out.println(positionIntervalResult.getInterval().getEndPosition());
//        }
        doPositionIntervalResultGraph();
    }
   public void testMany() throws IOException, InterruptedException {
       Blocks.setDebug(true); 
       String filePath = "pantexamples/testblockmetrics/src/testblockmetrics/Simple.java";
       File file = new File(testPrjF,"src/testblockmetrics/Simple.java");
       ExamplesSetup.updateFile(filePath, "1.2");
       
   
       final byte[] fileText = getBytes(file);
       final Set<String> values = new HashSet<String>();
       final List<String> posValues = new ArrayList<String>();
       Logger blocksLogger = Logger.getLogger(Blocks.class.getName());
       blocksLogger.setLevel(Level.FINE);
       blocksLogger.addHandler(new BlocksHandler(values,posValues,fileText));

       RunJavacTest.runJavac(testPrjF);       
       ExamplesSetup.updateFile(filePath, "1.3");
       RunJavacTest.runJavac(testPrjF);       
       JavaFile jf = JavaFile.getJavaFile(file, "testblockmetrics");
       CVSMetric cvsm = jf.getCVSResultMetric();
       BlocksMetric bm = jf.getMetric(BlocksMetric.class);
       assertNotNull(bm);
       PositionVersionIntervalResultContainer<BlocksItem> container = bm.getStorage(); 
       Set<PositionIntervalResult<BlocksItem>> results = container.getAllObjects();
       for (PositionIntervalResult<BlocksItem> result : results) {
           PositionInterval i = result.getInterval();
           String val = result.getObject() + ":" + cvsm.getContent(i.getStartPosition(),i.getEndPosition());
//           System.out.println("'" + val + "'\n" + i);
           VersionInterval vi = container.get(result);
//           System.out.println(vi.getFrom());
//           System.out.println(vi.getTo());
           assertTrue(val,values.contains(val));
       }

   }
 
   public void doPositionIntervalResultGraph () {
       String filePath = "pantexamples/testblockmetrics/src/testblockmetrics/Simple.java";
       File file = new File(testPrjF,"src/testblockmetrics/Simple.java");
       JavaFile jf = JavaFile.getJavaFile(file, "testblockmetrics");
       BlocksMetric bm = jf.getMetric(BlocksMetric.class);
       PositionVersionIntervalResultContainer<String> classes = bm.getClasses();
       PositionVersionIntervalResultContainer<String> methods = bm.getMethods();
       Version version = jf.getCVSResultMetric().getRootVersion().getVersion("1.2");
       List<PositionVersionIntervalResultContainer<?>> all = new ArrayList<PositionVersionIntervalResultContainer<?>>();
       all.add(classes);
       all.add(methods);
       EnumSet<BlocksItem> filter = EnumSet.of(BlocksItem.IF, BlocksItem.ELSE,BlocksItem.SWITCH);
       PositionVersionIntervalResultContainer<BlocksItem> filter1 = bm.filter(filter);
       all.add(filter1);
       PositionIntervalResultGraph pirg = PositionIntervalResultGraph.createGraph(all, version, 1);
       List<PositionIntervalResultGraph.Item> items = new ArrayList(pirg.getItems(1));
       Collections.sort(items);
       assertEquals(2,items.size());
       PositionIntervalResultGraph.Item item = items.get(0);
       assertEquals("Simple(boolean)", item.getPir().getObject());
        List<Item> children = item.getChildren();
        assertEquals(3, children.size());
        PositionIntervalResultGraph.Item item1 = children.get(0);
        BlocksItem bi = (BlocksItem)item1.getPir().getObject();
        assertEquals(BlocksItem.IF,bi);
 
//        for (Item itemx: items) {
//           System.out.println(itemx.getPir().getObject());
//           System.out.println("Children:");
//            for (Item item11 : itemx.getChildren()) {
//               System.out.println(item11.getPir().getObject());
//               System.out.println("  Children2:");
//               for (Item item2 : item11.getChildren()) {
//                   System.out.println(item2.getPir().getObject());
//               }
//           }
//       }
   }
   private  byte[] getBytes(File file) throws IOException {
       FileInputStream fis = new FileInputStream(file);
       byte bytes[] = new byte[(int)file.length()];
       try {
           fis.read(bytes);
        } finally {
           fis.close();
        }
       return bytes;
   }
}
