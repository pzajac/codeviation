/*
 * VersionsObjStorage.java
 *
 * Created on November 5, 2006, 10:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.codeviation.model.vcs.CVSMetric;

/**
 *  Versioned position interval. 
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
    
    /** Get all PositionIntervalResults which were added to version
     *  @param version computed version
     */  
    public Set<PositionIntervalResult<Y>> getAddedPIR(Version version) {
        Set<PositionIntervalResult<Y>> result = new HashSet<PositionIntervalResult<Y>>();
        for (Map.Entry<PositionIntervalResult<Y>, VersionInterval> entry : map.entrySet()) {
            VersionInterval vi = entry.getValue();
            if (vi.getFrom().equals(version)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    /** Get all PositionIntervalResult which were removed from version.
     */
    public Set<PositionIntervalResult<Y>> getRemovedPIR(Version version) {
        Set<PositionIntervalResult<Y>> result = new HashSet<PositionIntervalResult<Y>>();
        CVSMetric cvsm = version.getJavaFile().getCVSResultMetric();    
        if (cvsm != null) {
            Version v = null;
            Version next = cvsm.getRootVersion();
            do {
                v = next;
                next = v.getNext();
                if (next == null) {
                    throw new IllegalStateException("Invalid previous version  :" + version.getJavaFile() + "," + version);
                }
                
            } while ( !next.equals(version));
            
            for (Map.Entry<PositionIntervalResult<Y>, VersionInterval> entry : map.entrySet()) {
                VersionInterval vi = entry.getValue();
                if (vi.getTo().equals(v)) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;        
    }
    /** Get all PositionIntervalResult which were not changed, it's meam that previous
     *  version ant the parameter version contain the PositionIntervalResult.  
     */
    public Set<PositionIntervalResult<Y>> getUnchangedPIR(Version version) {
        Set<PositionIntervalResult<Y>> result = new HashSet<PositionIntervalResult<Y>>();
        for (Map.Entry<PositionIntervalResult<Y>, VersionInterval> entry : map.entrySet()) {
            VersionInterval vi = entry.getValue();
            if (vi.contains(version) && !vi.getFrom().equals(version)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
