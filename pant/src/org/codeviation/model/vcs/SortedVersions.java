/*
 * SortedVersions.java
 * 
 * Created on Jul 11, 2007, 5:15:59 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.model.vcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.chaidb.db.api.BTree;
import org.chaidb.db.api.BTreeStoreMode;
import org.chaidb.db.api.BTreeType;
import org.chaidb.db.api.Database;
import org.chaidb.db.api.keys.DateKey;
import org.chaidb.db.exception.ChaiDBException;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.Repository;
import org.codeviation.model.SourceRoot;
import org.codeviation.model.Version;

/**
 *  Sort all file revisions  in Repository in persistent BTree
 * @author pzajac
 */
public final class SortedVersions {
    final static String FILE_NAME = "SortedVersions.btree";
    private Repository rep;
    private static Database db;
    private BTree testTree;

    /** delay between two revisions to detect transaction
     * in milis   
     */
    private long transactionDelay = 10000;
    
    private SortedVersions(Repository rep)  {
        this.rep = rep;
        try  {
            if (db == null) {
                db = new Database(rep.getCacheRoot().getAbsolutePath());
            }
           testTree = db.openBTree(rep.getName() + "_" + FILE_NAME,BTreeType.BTREE);
        } catch (ChaiDBException chaiDBException) {
            throw new IllegalStateException(chaiDBException);
        }
        
    }

    private void addItemToTrans(Map<String,List<Item>> computingTrans,
            List<List<Item>> transactions, 
            SortedVersions.Item it) {
        String developer = it.getDeveloper();        
        List<Item> items = computingTrans.get(developer);    
        if (items != null) {
            Item last = items.get(items.size() - 1);
            if (it.date.getTime() - last.date.getTime() < transactionDelay) {
                items.add(it);  
            } else {
                transactions.add(items);
                items = new ArrayList<Item>();
                items.add(it);
                computingTrans.put(developer,items);
            }

        } else {
              items = new ArrayList<Item>();
              items.add(it);
              computingTrans.put(developer,items);
        }
    }
    
    /** A revision description 
     */
    public final class Item {
        private final String srcRoot;
        private final String packName;
        private final String name;
        private final String developer;
        private final String version;
        private final Date date;

        // transitive data
        SourceRoot sourceRoot;
        Package pack;
        Version cvsVersion;
        JavaFile jf;
        
        Item(String srcRoot, String packName, String name, String developer,String version,Date date) {
            this.srcRoot = srcRoot;
            this.packName = packName;
            this.name = name;
            this.developer = developer;
            this.version = version;
            this.date = date;

            if (srcRoot == null) {
                throw new IllegalArgumentException("srcRoot");
            }
            if (packName == null) {
                throw new IllegalArgumentException("packName");
            }
            if (name == null) {
                throw new IllegalArgumentException("name");
            }
            if (developer == null) {
                throw new IllegalArgumentException("developer");
            }
            if (version == null) {
                throw new IllegalArgumentException("version");
            }  
            if (date == null) {
                throw new IllegalArgumentException("date");
            }  
        }

        public String getDeveloper() {
            return developer;
        }

        public String getName() {
            return name;
        }
        
        public JavaFile getJavaFile() {
            if (jf == null) {
                if (getPackage() != null) {
                    jf = pack.getJavaFile(name);
                }
            }
            return jf;
        }
 
        public Package getPackage() {
            if (pack == null) {
                 SourceRoot root = getSourceRoot();
                if (root != null) {
                    pack =  root.getPackage(packName, false);
                }
            }
            return pack;
                    
        }

        public SourceRoot getSourceRoot() {
            if (sourceRoot == null) {
                sourceRoot =  rep.getSourceRoot(srcRoot);
            }
            return sourceRoot;
 
        }

        public Version getVersion() {
            if (cvsVersion == null) {
                JavaFile jfl = getJavaFile();
                if (jfl != null) {
                    CVSMetric cvsm = jfl.getCVSResultMetric();
                    if (cvsm != null) {
                        cvsVersion = cvsm.getRootVersion().getVersion(version);
                    }
                }
            }
            return cvsVersion;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Item item = (Item) obj;
            if (item == this) {
                return true;
            } 
            
            return item.srcRoot.equals(srcRoot) && 
                   item.packName.equals(packName) &&  
                   item.name.equals(name) && 
                   item.developer.equals(developer) && 
                   item.version.equals(version) ;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.srcRoot != null ? this.srcRoot.hashCode() : 0);
            hash = 37 * hash + (this.packName != null ? this.packName.hashCode() : 0);
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 37 * hash + (this.version != null ? this.version.hashCode() : 0);
            return hash;
        }

        
        
    }
    
    /** rebuild persistent BTree for repository
     */    
    public static SortedVersions createSortedVersions(Repository rep)  {
        SortedVersions sv = new SortedVersions(rep);
        sv.recreate();
        return sv;
    }

    /** Open already rebuilt persistent BTree
     */
    public static SortedVersions openSortedVersions(Repository rep)  { 
        return  new SortedVersions(rep);  
    }

 
    /**  get items for interval
     */ 
    public List<Item> getItems(Date from,Date to) {
        try {
            ArrayList results = testTree.rangeLookup(new DateKey(from), new DateKey(to), true, true);
            List<Item> items = new ArrayList<Item>();
            for (Object o : results) {
                String val = new String((byte[]) o) ;
                items.addAll(getItems(val));
            }
            return items;
        } catch (ChaiDBException ex) {
            throw new IllegalStateException(ex);
        }
        
    }

    public List<Transaction> getTransaction(Date from, Date to) {
        List<Item> items = getItems(from,to);
        List<List<Item>> transactions = new ArrayList<List<Item>>();    
        // developer -> transaction    
        Map<String,List<Item>> computingTrans = new HashMap<String,List<Item>>();
        
        for (int i = 0 ; i < items.size() ; i++) {
            Item it = items.get(i);
            addItemToTrans(computingTrans,transactions,it);    
        }
        transactions.addAll(computingTrans.values());            
        Collections.sort(transactions, new TransactionComparator());
        
        List<Transaction> realTransactions = new ArrayList<Transaction>();
        for (List<Item> items2 : transactions) {
            realTransactions.add(new Transaction(items2));
        }
        return realTransactions;
    }
    /** closes database handler 
     */
    public void closeDb() {
        try {
            testTree.close();
            db.close();
        } catch (ChaiDBException cdbe) {
            throw new IllegalStateException(cdbe);
        }
        
    }

    public void setTransactionDelay(long timeMs) {
        this.transactionDelay = timeMs;
    }
    
    private static final class TransactionComparator implements Comparator<List<Item>> {

        public int compare(List<SortedVersions.Item> o1, List<SortedVersions.Item> o2) {
            if (o1.isEmpty()) {
                return -1;
            } else if (o2.isEmpty()) {
                return 1;
            }
            long time1 = o1.get(0).date.getTime();
            long time2 = o2.get(0).date.getTime();
            return (time1 < time2 ) ? -1 : 1;
        }

    }
    private void recreate() {
        for (SourceRoot srcRoot : rep.getSourceRoots()) {
            for (JavaFile jf : srcRoot) {
               CVSMetric cvsm = jf.getCVSResultMetric();
               if (cvsm != null) {
                   Version v = cvsm.getRootVersion();
                   if (v.getState() == Version.State.DEAD) {
                       v = v.getNext();
                   }
                   String relPath = srcRoot.getRelPath();
                   String packName = jf.getPackage().getName();
                   String fileName = jf.getName();
                   try {
                       while (v != null) {
                            DateKey key = new DateKey(v.getDate());
                            Item it = new Item(relPath,packName,fileName,v.getUser(),v.getRevision(),v.getDate());
                            Set<Item> items = Collections.singleton(it);
                            Object val = testTree.lookup(key);
                            if (val != null) {
                                items = getItems(new String((byte[])val));
                                items.add(it);
                            }
                            testTree.store(key, createString(items).getBytes(),BTreeStoreMode.REPLACE);
                            v = v.getNext();
                       }
                   } catch (ChaiDBException e) {
                       throw new IllegalStateException(e);
                   }
                }
            }
        }        
    }
        Set<Item> getItems(String value)  {
            Set<Item> items = new HashSet<Item>();
            StringTokenizer tokenizer = new StringTokenizer(value,"\n");
            String vals[] = new String[6];
            while (tokenizer.hasMoreElements()) {
                int i = 0 ;
                for (; i < 6 && tokenizer.hasMoreTokens(); i++) {
                    vals[i] = tokenizer.nextToken();
                }
                if (i != 6) {
                    /// xxx something is wrong
                    break;
                }
                long time = Long.parseLong(vals[5]);
                Date date = new Date(time);
                items.add(new Item(vals[0],vals[1],vals[2],vals[3],vals[4],date));
            }
            return items;
        }
        String createString(Set<Item> items) {
            StringBuilder builder = new StringBuilder();
            for (Item item : items) {
                builder.append(item.srcRoot);
                builder.append('\n');
                builder.append(item.packName);
                builder.append('\n');
                builder.append(item.name);
                builder.append('\n');
                builder.append(item.developer);
                builder.append('\n');
                builder.append(item.version);
                builder.append('\n');
                builder.append(item.date.getTime());
                builder.append('\n');
            }
            return builder.toString();

        }

}
