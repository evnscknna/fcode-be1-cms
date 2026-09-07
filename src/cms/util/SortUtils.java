package cms.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// Merge sort by hand. Stable: equal items keep their input order.
public final class SortUtils {

    private SortUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> void mergeSort(List<T> list, Comparator<? super T> comparator) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(comparator, "comparator");
        int n = list.size();
        if (n < 2) {
            return;
        }
        T[] source = (T[]) list.toArray();
        T[] buffer = (T[]) new Object[n];
        sort(source, buffer, 0, n - 1, comparator);
        for (int i = 0; i < n; i++) {
            list.set(i, source[i]);
        }
    }

    public static <T> List<T> sorted(List<T> list, Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(list);
        mergeSort(copy, comparator);
        return copy;
    }

    private static <T> void sort(T[] a, T[] buf, int lo, int hi, Comparator<? super T> c) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort(a, buf, lo, mid, c);
        sort(a, buf, mid + 1, hi, c);
        merge(a, buf, lo, mid, hi, c);
    }

    private static <T> void merge(T[] a, T[] buf, int lo, int mid, int hi, Comparator<? super T> c) {
        for (int k = lo; k <= hi; k++) {
            buf[k] = a[k];
        }
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = buf[j++];
            } else if (j > hi) {
                a[k] = buf[i++];
            } else if (c.compare(buf[j], buf[i]) < 0) {
                a[k] = buf[j++];
            } else {
                a[k] = buf[i++]; // tie -> take left half, keeps order
            }
        }
    }
}
