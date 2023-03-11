package penna.core.internals;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public final class Clock {
    private Clock() {}

    // This is the amount of time, in nanos, that we will wait before incrementing.
    // Keeping this at 1_000_000 ensure we tick at roughly 1ms;
    private static final long refreshRate = 1_000_000;

    // The precision is how far behind, in microseconds, we afford to be before we sync to System.currentTimeMillis
    private static final long precision = 1_000;
    private static final Lock startThreadLock = new ReentrantLock();

    private static final AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());
    private static final Thread clockThread = ThreadCreator.newThread("maple-clock-ticker", () -> {
        while(true) {
            var ts = timestamp.incrementAndGet();

            // System.currentTimeMillis is known to be slow on linux.
            // We can increment manually and sync at every second maybe?
            if (ts % precision == 0) {
                timestamp.set(System.currentTimeMillis());
            }
            LockSupport.parkNanos(refreshRate);
        }
    });

    public static long getTimestamp() {
        if (!clockThread.isAlive() && startThreadLock.tryLock()) {
            // We preload the current value just in case we return before the thread updates
            timestamp.updateAndGet(ignored -> System.currentTimeMillis());
            clockThread.start();
            startThreadLock.unlock();
        }

        return timestamp.get();
    }
}
