package penna.core.internals;

import java.util.BitSet;
import java.util.Objects;

public record StackTraceBloomFilter(BitSet bitSet) implements StackTraceFilter {

    private static final int FILTER_SIZE = 8 * 1024;
    private static final int PRIME = 1048573;
    public static final int NUMBER_OF_HASHES = 2;

    public static StackTraceBloomFilter create() {
        return new StackTraceBloomFilter(new BitSet(StackTraceBloomFilter.FILTER_SIZE));
    }

    @Override
    public void reset() {
        bitSet.clear();
    }

    @Override
    public void hash(int[] positions, StackTraceElement element) {
        int hash1 = Objects.hash(element.getClassName(), element.getMethodName(), element.getLineNumber(), element.getFileName());
        int hash2 = Objects.hash(PRIME, hash1);

        positions[0] = hash1 & FILTER_SIZE - 1;
        positions[1] = hash2 & FILTER_SIZE - 1;
    }

    @Override
    public void mark(int... positions) {
        for (int position : positions) {
            bitSet.set(position);
        }
    }


    @Override
    public boolean check(int... positions) {
        for (int position : positions) {
            if (bitSet.get(position)) return true;
        }

        return false;
    }

}
