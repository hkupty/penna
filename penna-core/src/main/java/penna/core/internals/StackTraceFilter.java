package penna.core.internals;

import java.util.BitSet;

public record StackTraceFilter(BitSet bitSet, int[] cursor) {

    private static final int FILTER_SIZE = 8 * 1024;
    private static final int NUMBER_OF_HASHES = 2;

    private static final ThreadLocal<BitSet> BASE = ThreadLocal.withInitial(() -> new BitSet(FILTER_SIZE));

    public StackTraceFilter reset() {
        bitSet.clear();
        return this;
    }

    public static StackTraceFilter create() {
        var bitset = BASE.get();
        bitset.clear();

        return new StackTraceFilter(bitset, new int[NUMBER_OF_HASHES]);
    }

    public void hash(StackTraceElement element, int[] positions) {
        int size = bitSet().size();
        int prime = 1048573;

        // 32 - 13. 2^13 = 8 * 1024 = FILTER_SIZE
        // So hash2 will be definitely smaller than
        int hash1 = element.hashCode();
        int hash2 = ((hash1 * prime) & Integer.MAX_VALUE) >> 19;

        // To avoid off-by-one errors
        positions[0] = ((hash1 & Integer.MAX_VALUE)  >> 19) - 1;
        positions[1] = hash2 - 1;

    }

    public int[] hash(StackTraceElement element) {
        hash(element, this.cursor);

        return this.cursor;
    }

    public void mark(StackTraceElement stackTraceElement) {
        mark(hash(stackTraceElement));
    }

    public void mark(int[] positions) {
        for (int position : positions) {
            bitSet.set(position);
        }
    }

    public boolean check(StackTraceElement stackTraceElement) {
        return check(hash(stackTraceElement));
    }

    public boolean check(int[] positions) {
        for (int position : positions) {
            if (bitSet.get(position)) return true;
        }

        return false;
    }

}
