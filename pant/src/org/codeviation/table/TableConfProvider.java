package org.codeviation.table;

/**
 * It allows to register TableConf to webrowser. Register implementations to
 * META-INF/server/org.codeviation.table.TableConfProvider
 * @author pzajac
 */
public interface TableConfProvider {
    public  TableConf[] getTableConfs();    
}
