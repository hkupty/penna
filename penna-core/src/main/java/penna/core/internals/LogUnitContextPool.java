package penna.core.internals;

import org.jetbrains.annotations.VisibleForTesting;
import penna.core.logger.LogUnitContext;
import penna.core.models.PennaLogEvent;
import penna.core.sink.CoreSink;
import penna.core.sink.Sink;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * This Object Pool ensures each thread will have access to a single pooled {@link LogUnitContext},
 * without requiring {@link ThreadLocal}.
 * <br/>
 * It does so by locking each object through its {@link LogUnitContextPool#locks}.
 */
public final class LogUnitContextPool {

    /* TODO
        Check if it makes more sense performance-wise to have the lock inside LogUnitContext.
        This could allow for better CPU cache hit-rate, but at the same time it leaks the lock to
        LogUnitContext and the object becomes bigger, which could defeat the purpose.
    */

    private final Lock[] locks;

    private final LogUnitContext[] objectGroup;

    private LogUnitContext leafObject(int index) {
        return new LogUnitContext(this, index, CoreSink.getSink(), new PennaLogEvent());
    }

    public LogUnitContextPool() {
        int size = 16;
        objectGroup = new LogUnitContext[size];
        locks = new ReentrantLock[size];
        for (int i = 0; i < size; i++) {
            objectGroup[i] = leafObject(i);
            locks[i] = new ReentrantLock();
        }
    }

    @VisibleForTesting
    void refillThePool(Supplier<Sink> sinkSupplier) {
        for (int i = 0; i < objectGroup.length; i++) {
            objectGroup[i] = new LogUnitContext(this, i, sinkSupplier.get(), objectGroup[i].logEvent());
        }
    }

    public void release(int index) {
        locks[index].unlock();
    }

    private int acquireLock() {
        /* TODO
            Measure runtime as this is an interesting place to look at.
            Number of iterations and number of cycles through the ring
            might be pointing at wasted effort.
            Starting at random places in the buffer might be an easy solution
            if we end up continuously fighting contention at the beginning of the pool
            but never hitting the end of the array.
         */

        var index = Thread.currentThread().hashCode() & 0xF;
        while (!locks[index].tryLock()) {
            index = ++index & 0xF;
        }
        return index;
    }

    public LogUnitContext get() {
        var index = acquireLock();
        return objectGroup[index];
    }
}
