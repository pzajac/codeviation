/*
 * Priority.java
 *
 * Created on November 18, 2002, 9:41 AM
 */

package org.codeviation.bugtracking.issuezilla;

/**
 * Issue priority
 * @author  pz97949
 */
public enum Priority {    
    P1("P1",0),
    P2("P2",1),
    P3("P3",2),
    P4("P4",3),
    P5("P5",4);
    String val;
    int priority;
    Priority(String val,int priority) {
        this.val = val;
        this.priority = priority;
    }

    /** Creates a new instance of Priority */
    
    public String toString() {
        return val;
    }
    
    /** Get priority for priority level
     */
    public static Priority get(int priority) {
        for (Priority p : Priority.values()) {
            if (p.getPriority() == priority) {
                return p;
            }
        }
        return null;
    }
    /**
     * 
     * @return priority level
     */
    public int getPriority() {
        return  priority;
    }
//    public static Priority findPriority(String priorityValue) {
//        Priority prs[] = getAllPriorities();
//        for (int i = 0 ; i < prs.length ; i++ ) {
//            if (prs[i].toString().equals (priorityValue)) {
//                return prs[i];
//            }
//        }
//        return null;
//    }
        
}
