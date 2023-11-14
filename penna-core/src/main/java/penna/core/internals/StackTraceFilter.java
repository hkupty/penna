package penna.core.internals;

public sealed interface StackTraceFilter permits PassThroughFilter, StackTraceBloomFilter {

    void reset();

    void hash(int[] positions, StackTraceElement element);

    void mark(int... positions);

    boolean check(int... positions);
}
