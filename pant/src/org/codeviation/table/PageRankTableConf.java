
package org.codeviation.table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.codeviation.model.JavaFile;
import org.codeviation.model.Package;
import org.codeviation.model.SourceRoot;
import org.codeviation.tasks.PageRankMetric;

/**
 *
 * @author pzajac
 */
public final class PageRankTableConf implements TableConf,TableConfProvider{
    /** name -> tag -> value
     */ 
    private Map<String,Map<String,Double>> values = new TreeMap<String,Map<String,Double>>();
    
    private Set<String> tags = new HashSet<String>();
    
    public void init() {
        values.clear();
        tags.clear();
    }

    public void addJavaFile(JavaFile jf) {
        PageRankMetric prm = jf.getMetric(PageRankMetric.class);
        for (String tag : prm.getTags()) {
            addValue(jf.getName(),tag,prm.get(tag));
        }        
    }
    public void addSourceRoot(SourceRoot srcRoot) {
        for (Package pack : srcRoot.getPackages()) {
            double val = 0.0;
            String packName = pack.getName();
            for (JavaFile jf : pack.getJavaFiles()) {
                PageRankMetric prm = jf.getMetric(PageRankMetric.class);
                for (String tag : prm.getTags()) {
                    addValue(packName,tag,prm.get(tag));
                }
            }
        }
    }
    public void addPackage(Package pack) {
        for (JavaFile jf : pack.getJavaFiles()) {
            PageRankMetric prm = jf.getMetric(PageRankMetric.class);
            for (String tag : prm.getTags()) {
                addValue(jf.getName(),tag,prm.get(tag));
            }
        }
    }
    
    public void addValue(String name,String tag,double value) {
        Map<String,Double> tagToValue = values.get(name);
        if (tagToValue == null) {
            tagToValue = new TreeMap<String,Double>();
            values.put(name,tagToValue);
        }
        Double valObj = tagToValue.get(tag);
        double val = 0;
        if (valObj == null) {
            tags.add(tag);
        } else {
            val = valObj.doubleValue();
        }
        tagToValue.put(tag,value + val);
    }
    
    public Table getTable() {
        DefaultTable table = new DefaultTable(values.size(),tags.size(),"PageRank");
        table.setDescription("PageRank history");
        table.setTitle("PageRank history");
        
        String rowHeader[] = new String[values.size()];
        values.keySet().toArray(rowHeader);
        Arrays.sort(rowHeader);
        
        String columnHeader[] = new String[tags.size()];
        tags.toArray(columnHeader);
        Arrays.sort(columnHeader);
        
        table.setRowsHeader(rowHeader);
        table.setColumnsHeader(columnHeader);
        
        for (int r = 0 ; r < rowHeader.length ; r++) {
            Map<String,Double> tagToValue = values.get(rowHeader[r]);
            if (tagToValue != null) {
                for (int c = 0 ; c < columnHeader.length ; c++) {
                    Double val = tagToValue.get(columnHeader[c]);
                    if (val != null) {
                        table.setData(r,c,val);
                    }
                }
            }
        }
        return table;
    }

    public void clear() {
        values.clear();
        tags.clear();
    }

    public TableConf[] getTableConfs() {
        return new TableConf[] {new PageRankTableConf()};
    }
    
    public String toString() {
        return "PageRank";
    }
}
