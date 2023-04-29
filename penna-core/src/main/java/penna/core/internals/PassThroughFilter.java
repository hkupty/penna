package penna.core.internals;

public class PassThroughFilter implements StackTraceFilter {
    @Override
    public StackTraceFilter reset() {
        return this;
    }

    @Override
    public void hash(int[] positions, StackTraceElement element) { }

    @Override
    public void mark(int... positions) { }

    @Override
    public boolean check(int... positions) {
        return false;
    }
}
