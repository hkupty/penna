package penna.core.internals;

public final class PassThroughFilter implements StackTraceFilter {
    @Override
    public void reset() {

    }

    @Override
    public void hash(int[] positions, StackTraceElement element) {
    }

    @Override
    public void mark(int... positions) {
    }

    @Override
    public boolean check(int... positions) {
        return false;
    }
}
