
package org.codeviation.statistics;

/**
 *
 * @author pzajac
 */
public final class RecordType {
    private final String displayName;
    private final int id;
    private final boolean supportAdditive;
    
    public RecordType(String displayName,int id,boolean supportAdditive) {
        this.displayName = displayName;
        this.id = id;
        this.supportAdditive = supportAdditive;
    }
    
    public String getDisplayName() {
        return displayName;
    }    
    
    public boolean isSupportAdditive() {
        return supportAdditive;
    }
    public int getId() {
        return id;
    }    
    public int hashCode() {
        return displayName.hashCode() + id;
    }
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof RecordType ) {
            RecordType rt = (RecordType)obj;
            if (id == rt.id && displayName.equals(rt.getDisplayName())) {
                ret = true;
            }
        }
        return ret;
    } 
    public String toString() {
        return displayName + " ; " + id + " ; "  + supportAdditive;
    }
}
