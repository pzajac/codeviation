package org.codeviation.statistics;

import java.util.Comparator;
import org.codeviation.model.Version;

/**
 * Compares Dates of Versions.
 * @author pzajac
 */
public class VersionDateComparator implements Comparator<Version>{
    public int compare(Version v1, Version v2) {
        return v1.getDate().compareTo(v2.getDate());
    }
}
