package penna.core.internals;

import penna.core.logger.LogUnitContext;
import penna.core.models.PennaLogEvent;
import penna.core.sink.SinkManager;

import java.util.concurrent.locks.ReentrantLock;

public class ObjectPool {

    private final ReentrantLock[] locks;

    private final LogUnitContext[] objectGroup;

    private LogUnitContext leafObject(int index) {
        return new LogUnitContext(this, index, SinkManager.Instance.get(), new PennaLogEvent());
    }

    public ObjectPool() {
        int size = 16;
        objectGroup = new LogUnitContext[size];
        locks = new ReentrantLock[size];
        for (int i = 0; i < size; i++) {
            objectGroup[i] = leafObject(i);
            locks[i] = new ReentrantLock();
        }
    }

    public void release(int index) {
        locks[index].unlock();
    }

    private int acquireLock() {
        var index = 0;
        while (!locks[index].tryLock()) { index = ++index & 0xF;}
        return index;
    }

    public LogUnitContext get() {
        var index = acquireLock();
        return objectGroup[index];
    }

}
