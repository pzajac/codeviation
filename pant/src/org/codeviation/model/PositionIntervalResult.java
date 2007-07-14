/*
 * SrcVerObject.java
 *
 * Created on November 5, 2006, 10:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.model;
import java.io.Serializable;

/**
 *A metric result for PositionInterval. T needs to have unique hashcode and equals
 * implementation.
 * @author pzajac
 */
public final class PositionIntervalResult<T> implements Serializable {
    PositionInterval interval;
    T result;
    /** Creates a new instance of SrcVerObject */
    public PositionIntervalResult(PositionInterval interval,T object) {
        this.interval = interval;
        this.result = object;
    }
    @SuppressWarnings("unchecked")
    public boolean equals (Object obj) {
        if (obj instanceof PositionIntervalResult) {
            PositionIntervalResult<T> srcVer = (PositionIntervalResult<T>)obj;
            return interval.equals(srcVer.interval) && result.equals(srcVer.result);
        }
        return false;
    }
    
    public int hashCode () {
        return result.hashCode() + (interval.hashCode()   << 11);
    }
    
    public PositionInterval getInterval() {
        return interval;
    }
    /** get value
     */
    public T getObject() {
        return result;
    }    
}
