/*
 * VersionsObjStorage.java
 *
 * Created on November 5, 2006, 10:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import org.codeviation.model.VersionInterval;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.codeviation.model.VersionInterval;

/**
 *  
 * @author pzajac
 */
public final class PositionVersionIntervalResultContainer<Y> implements Serializable {
    Map<PositionIntervalResult<Y>, VersionInterval> map = new HashMap<PositionIntervalResult<Y>, VersionInterval>();
    static Logger logger = Logger.getLogger(PositionVersionIntervalResultContainer.class.getName()); 
    /** Creates a new instance of VersionsObjStorage */
    public PositionVersionIntervalResultContainer() {
    }
    
    public VersionInterval get(PositionIntervalResult<Y> srcVer) {
        return map.get(srcVer);
    }
    public Set<PositionIntervalResult<Y>> getAllObjects() {
        return map.keySet();
    }
    
    public Set<Version> getAllVersion() {
        Set<Version> versions = new HashSet<Version>();
        for (VersionInterval vers : map.values()) {
            versions.addAll(vers.getAllVersions());
        }
        return versions;
    }
    public void add(PositionIntervalResult<Y> srcVer,Version version) {
          VersionInterval ver = map.get(srcVer);          
          if (ver == null) {
              logger.fine("newItem: " + srcVer.toString());  
              PositionInterval inter = srcVer.getInterval();
              ver = new VersionInterval(inter.getStartPosition().getVersion(),
                                inter.getEndPosition().getVersion());
              map.put(srcVer, ver);
          } else {
              ver.addVersion(version);
          }
    }
    public void add(PositionIntervalResult<Y> srcVer,VersionInterval vi) {
          VersionInterval ver = map.get(srcVer);
          if (ver == null) {
              PositionInterval inter = srcVer.getInterval();
              map.put(srcVer,vi);               
          } else {
              for (Version v :vi.getAllVersions()) {
                ver.addVersion(v);
              }
          }
    }
}
