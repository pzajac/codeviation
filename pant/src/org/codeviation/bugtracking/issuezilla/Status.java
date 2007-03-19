/*
 * IssueStatus.java
 *
 * Created on November 18, 2002, 9:41 AM
 */

package org.codeviation.bugtracking.issuezilla;

/**
 * issue status 
 * @author  pz97949
 */
public class Status {
    public static final String CLOSED_STRING = "CLOSED";
    public static final String NEW_STRING = "NEW";
    public static final String REOPEN_STRING = "REOPENED";
    public static final String RESOLVED_STRING = "RESOLVED";
    public static final String STARTED_STRING = "STARTED";
    public static final String VERIFIED_STRING = "VERIFIED";
    public static final String NOT_USED_STRING = "";
    
    public static final Status closed = new Status(CLOSED_STRING);
    public static final Status newSt = new Status(NEW_STRING);
    public static final Status reopen = new Status(REOPEN_STRING);
    public static final Status resolved = new Status(RESOLVED_STRING);
    public static final Status started = new Status(STARTED_STRING);
    public static final Status verified = new Status(VERIFIED_STRING);
    public static final Status notUsed = new Status(NOT_USED_STRING);
    
    
    private String value;
/** Creates a new instance of IssueStatus */    
    private Status(String value) {
        this.value = value;
        
    }
    
    public static  Status[] getAllStatus() {
        return new Status [] {
            closed, newSt, reopen, started,resolved,verified,notUsed
        };
    }
            
    /** get status for specific status value. If status is not found it will return null
     */
    public static Status findStatus(String statusValue) {
        final Status allStatus[] = getAllStatus();
        for (int i = 0 ; i < allStatus.length ; i++ ) {
            if (allStatus[i].toString().equals(statusValue)) {
                return allStatus[i];
            }
        }
        return null;
    }
    
    public String toString () {
        return value;
    }
}
