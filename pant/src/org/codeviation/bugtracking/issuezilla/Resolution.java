/*
 * Resolution.java
 *
 * Created on November 18, 2002, 9:41 AM
 */

package org.codeviation.bugtracking.issuezilla;

/**
 *
 * @author  pz97949
 */
public class Resolution {
    public static final String DUPLICATE_STRING = "DUPLICATE";
    public static final String FIXED_STRING = "FIXED";
    public static final String INVALID_STRING = "INVALID";
    public static final String LATER_STRING = "LATER";
    public static final String REMIND_STRING = "REMIND";
    public static final String WONTFIX_STRING = "WONTFIX";
    public static final String WORKSFORME_STRING = "WORKSFORME";
    public static final String NULL_STRING = "NULL";

    public static final Resolution duplicate = new Resolution(DUPLICATE_STRING);
    public static final Resolution fixed = new Resolution(FIXED_STRING);
    public static final Resolution invalid = new Resolution(INVALID_STRING);
    public static final Resolution later = new Resolution(LATER_STRING);
    public static final Resolution remind = new Resolution(REMIND_STRING);
    public static final Resolution wontfix = new Resolution(WONTFIX_STRING);
    public static final Resolution worksforme = new Resolution(WORKSFORME_STRING);
    public static final Resolution nullRes = new Resolution(NULL_STRING);
    
    private String value;
    /** Creates a new instance of Resolution */
    private  Resolution(String value) {
        this.value = value;
    }
    
    public static Resolution [] getAllResolutions() {
        return new Resolution[] {duplicate,fixed,invalid,later,remind,wontfix,worksforme,nullRes};
    }
    
    /**
     * find resolution for specific value. If resolution isn't found it will return NULL
     */
    public static Resolution findResolution(String resValue) {
        Resolution allRes[] = getAllResolutions();
        for (int i = 0 ; i < allRes.length ; i++ ) {
            if (allRes[i].toString().equals(resValue)) {
                
                return allRes[i];
            }
        }
        return nullRes;
    }
        
    public String toString() {
        return value;
    }
    
}
