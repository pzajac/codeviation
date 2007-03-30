/*
 * VersionedResult.java
 *
 * Created on October 30, 2006, 9:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.util.Collection;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.Version;
import org.codeviation.model.PositionVersionIntervalResultContainer;

/**
 *
 * @author pzajac
 */
public abstract class VersionedMetric<T> implements Metric {
       private PositionVersionIntervalResultContainer<T> storage = new PositionVersionIntervalResultContainer<T>();   
       
       public abstract String getName(); 
       
       public void addSrcVerObject(PositionIntervalResult<T> srcVer,Version v) {
           storage.add(srcVer, v);
       }
       public void addSrcVerObjects(Collection<PositionIntervalResult<T>> srcVers,Version v) {
           for (PositionIntervalResult<T> srcVer : srcVers) {
               addSrcVerObject(srcVer,v);
           }
       }
       public PositionVersionIntervalResultContainer<T> getStorage() {
           return storage;
       }
       public abstract String getDescription();       
}
