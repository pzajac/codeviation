package org.codeviation.javac;

import java.util.Comparator;
import org.codeviation.model.PositionIntervalResult;

public class PIRComparator implements Comparator<PositionIntervalResult<UsageItem>> {

    public int compare(PositionIntervalResult<UsageItem> pir1, PositionIntervalResult<UsageItem> pir2) {
        int result = pir1.getInterval().compareTo(pir2.getInterval());
        if (result == 0) {
            result = pir2.hashCode() - pir1.hashCode();
        }
        return result;
    }
}
