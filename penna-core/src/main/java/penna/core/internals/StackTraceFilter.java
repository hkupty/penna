package penna.core.internals;

public sealed interface StackTraceFilter permits PassThroughFilter, StackTraceBloomFilter {

    class Shared {
        private static final PassThroughFilter passThroughFilter = new PassThroughFilter();

        public static StackTraceBloomFilter getBloomFilter() {
            return StackTraceBloomFilter.create();
        }

        public static PassThroughFilter getPassThroughFilter() {
            return passThroughFilter;
        }
    }

    void reset();

    void hash(int[] positions, StackTraceElement element);

    void mark(int... positions);

    boolean check(int... positions);
}
