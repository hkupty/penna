package penna.core.internals;

import java.util.BitSet;

public interface StackTraceFilter {

    StackTraceFilter reset();

    void hash(int[] positions, StackTraceElement element);

    void mark(int... positions);

    boolean check(int... positions);
}
