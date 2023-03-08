package maple.core.internals;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public final class Clock {
    private Clock() {}


    private static final AtomicLong timestamp = new AtomicLong(0L);
    private static final Thread clockThread = ThreadCreator.newThread("maple-clock-ticker", () -> {
        while(true) {
            timestamp.set(System.currentTimeMillis());
            LockSupport.parkNanos(1_000_000);
        }
    });

    public static long getTimestamp() {
        if (!clockThread.isAlive()) {
            synchronized (clockThread) {
                if (!clockThread.isAlive()) {
                    // We preload the current value just in case we return before the thread updates
                    timestamp.updateAndGet(ignored -> System.currentTimeMillis());
                    clockThread.start();
                }
            }
        }

        return timestamp.get();
    }
}
