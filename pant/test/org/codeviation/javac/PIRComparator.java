package org.codeviation.javac;

import java.util.Comparator;
import org.codeviation.model.PositionIntervalResult;

public class PIRComparator<T> implements Comparator<PositionIntervalResult<T>> {

    public int compare(PositionIntervalResult<T> pir1, PositionIntervalResult<T> pir2) {
        int result = pir1.getInterval().compareTo(pir2.getInterval());
        if (result == 0) {
            result = pir2.hashCode() - pir1.hashCode();
        }
        return result;
    }
}
