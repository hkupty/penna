package maple.core.internals;

import maple.core.models.MapleLogEvent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class LogEventRingBuffer {
    private final MapleLogEvent[] buffer;
    private int writer = 0;
    private int reader = 0;
    private final ReentrantLock lock;
    private final Condition bufferFree;

    public LogEventRingBuffer(int size) {
        buffer = new MapleLogEvent[size];
        lock = new ReentrantLock();
        bufferFree = lock.newCondition();
    }

    private int next(int cursor) {
        return (cursor + 1) % buffer.length;
    }

    public MapleLogEvent peek(){
        return buffer[reader];
    }

    public void commit() {
        try {
            lock.lock();
            buffer[reader] = null;
            reader = next(reader);
            bufferFree.signal();
        } finally {
            lock.unlock();
        }
    }

    public void put(MapleLogEvent mapleLogEvent) throws InterruptedException {
        try {
            lock.lock();
            while (next(writer) == reader) bufferFree.await();
            buffer[writer] = mapleLogEvent;
            writer = next(writer);
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        return writer - reader == 0;
    }
}