/*
 * Transaction.java
 * 
 * Created on Jul 25, 2007, 9:19:54 PM
 * 
 */

package org.codeviation.model.vcs;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author pzajac
 */
public final class Transaction {
    private final List<SortedVersions.Item> items;
    
    Transaction (List<SortedVersions.Item> items) {
        this.items = Collections.unmodifiableList(items);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No change in transaction");
        }
    }

    public List<SortedVersions.Item> getItems() {
        return items;
    }
    
  
    /** @return all issues numbers (@see Issue). It returns empty array
     * on no issue
     */
    public int[] getIssues() {
        SortedVersions.Item item = items.get(0);
        return item.getVersion().getDefectNumbers();
    }

    public String getDeveloper() {
        SortedVersions.Item item = items.get(0);
        return item.getDeveloper();
    }

    
}
