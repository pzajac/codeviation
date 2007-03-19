/*
 * Usages.java
 *
 * Created on October 29, 2006, 9:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.javac;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codeviation.model.JavaFile;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.PositionInterval;

/**
 *
 * @author pzajac
 */
public class Usages {
    private List<PositionIntervalResult<UsageItem>> items = new ArrayList<PositionIntervalResult<UsageItem>>();
    JavaFile javaFile;
    String className;
    
    public Usages(JavaFile javaFile,String className) {
        this.javaFile = javaFile;
        this.className = className;
    }
    public void persists() {
            UsagesMetric ur = javaFile.getMetric(UsagesMetric.class);
            if (ur == null) {
                ur = new UsagesMetric();
            } 
            ur.addSrcVerObjects(items,javaFile.getCVSVersion());
            
            javaFile.setMetric(ur);
    }
    
    public void addUsage(String method,String clazz,int startPos,int endPos) {
        if (javaFile.getCVSVersion() != null) {
             startPos = javaFile.unifyAbsolutePosition(startPos);
             endPos = javaFile.unifyAbsolutePosition(endPos);
             PositionInterval interval = new PositionInterval(javaFile.getPosition(startPos),
                                             javaFile.getPosition(endPos));
            items.add(new PositionIntervalResult<UsageItem>(interval,new UsageItem(method,clazz)));
        }
    }
    public void addExtends(String className,int startPos, int endPos) {
        addUsage(null,className,startPos,endPos);
    }
    public void addImplements(String className, int startPos,int endPos) {
        addUsage(null,className,startPos,endPos);
    }
    public void log() {
        Logger logger = Logger.getLogger(Usages.class.getName());
        for (PositionIntervalResult<UsageItem> i : items) {
            logger.log(Level.FINE, "log", i.getObject().getClazz() + ". " + i.getObject().getMethod() + ":" + i.getInterval().toString());
        }
        
    }
    /** @return all usages
     */
    public List<PositionIntervalResult<UsageItem>> getItems() {
        return items;
    }
}
