package cms.util;

import java.util.Collection;

public final class IdGenerator {

    private int next;

    public IdGenerator(int startFrom) {
        this.next = Math.max(startFrom, 1);
    }

    public static IdGenerator seededFrom(Collection<Integer> existingIds) {
        int max = 0;
        for (int id : existingIds) {
            if (id > max) {
                max = id;
            }
        }
        return new IdGenerator(max + 1);
    }

    public int nextId() {
        return next++;
    }
}
