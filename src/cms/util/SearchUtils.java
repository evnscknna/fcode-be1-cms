package cms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class SearchUtils {

    private SearchUtils() {
    }

    // Blank query keeps everything. haystack builds the searchable text for an item.
    public static <T> List<T> filterByText(List<T> items, String query,
                                           Function<T, String> haystack) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<T> out = new ArrayList<>();
        for (T item : items) {
            if (q.isEmpty()) {
                out.add(item);
                continue;
            }
            String hay = haystack.apply(item);
            if (hay != null && hay.toLowerCase().contains(q)) {
                out.add(item);
            }
        }
        return out;
    }

    // List must be sorted by id ascending. Returns null if not found.
    public static <T> T binarySearchById(List<T> sortedById, int id, ToIntFunction<T> idOf) {
        int lo = 0;
        int hi = sortedById.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midId = idOf.applyAsInt(sortedById.get(mid));
            if (midId == id) {
                return sortedById.get(mid);
            }
            if (midId < id) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return null;
    }
}
