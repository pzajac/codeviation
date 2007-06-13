
package org.codeviation.model;

import java.util.Set;
import no.uib.cipr.matrix.Vector;

/**
 * Used for metrics with mapping Version vector 
 */
public interface VersionedVector extends Metric{
    /**
     * Get vector of metrics for specified version.
     * @param vers 
     * @return vector of doubles 
     */
    public Vector getVector(Version vers);
    /**
     * Get available versions.
     * @return available versions
     */
    public Set<Version> getVersions() ;
}
