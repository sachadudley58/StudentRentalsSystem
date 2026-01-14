package studentrentals.util;

import java.time.LocalDate;

public final class DateRangeUtil {
    private DateRangeUtil() {}

    // Overlap check using half-open intervals: [start, end)
    // Overlap exists if: start1 < end2 && start2 < end1
    public static boolean overlaps(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
