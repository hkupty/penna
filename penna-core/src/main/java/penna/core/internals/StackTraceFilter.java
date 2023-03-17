package penna.core.internals;

import java.util.BitSet;

public record StackTraceFilter(BitSet bitSet) {

    private static final int FILTER_SIZE = 8 * 1024;
    private static final int NUMBER_OF_HASHES = 6;

    private static final ThreadLocal<BitSet> BASE = ThreadLocal.withInitial(() -> new BitSet(FILTER_SIZE));

    public static StackTraceFilter create() {
        var bitset = BASE.get();
        bitset.clear();

        return new StackTraceFilter(bitset);
    }

    private int[] hash(StackTraceElement element) {
        int[] positions = new int[NUMBER_OF_HASHES];
        int prime = 1048573; // a large prime number

        int hash1 = element.hashCode();
        int hash2 = prime - (hash1 % prime);

        for (int i = 0; i < NUMBER_OF_HASHES; i++) {
            positions[i] = Math.abs((hash1 + i * hash2) % bitSet().size());
        }
        return positions;
    }


    public void mark(StackTraceElement stackTraceElement) {
        for (int position : hash(stackTraceElement)) {
            bitSet.set(position, true);
        }
    }

    public boolean check(StackTraceElement stackTraceElement) {
        boolean isSet = true;
        for (int position : hash(stackTraceElement)) {
            isSet &= bitSet.get(position);
            if (!isSet) break;
        }

        return isSet;
    }

}
