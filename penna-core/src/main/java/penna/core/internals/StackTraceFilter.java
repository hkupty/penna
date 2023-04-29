package penna.core.internals;

public interface StackTraceFilter {

    StackTraceFilter reset();

    void hash(int[] positions, StackTraceElement element);

    void mark(int... positions);

    boolean check(int... positions);
}
