package penna.core.internals;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class holds the "clock" for all loggers.
 * Since {@link penna.core.models.PennaLogEvent} holds unix timestamps in millis to represent
 * the logging time, we need to keep track of time.
 * <br />
 * This class is an optimization. It would be very convenient using {@link java.time.Instant}, but that
 * costs memory. Instead, we use {@link System#currentTimeMillis()} to start and roughly every ms increment
 * by one an atomic counter. After 1024 iterations, we sync back with {@link System#currentTimeMillis()}.
 * <br />
 * For logging purposes, this should be enough. Since this is very fast, it should not be impactful in any
 * way to run it once every 1ms, but if it proves to be slow, we could update the value in chunks
 * and increase the refresh rate proportionally, reducing the frequency the underlying thread is awoken.
 */
public final class Clock {
    private Clock() {}

    /**
     * This is the amount of time, in nanos, that we will wait before incrementing.
     * Keeping this value at 1 million ensure we tick at roughly every 1ms.
     */
    private static final long REFRESH_RATE = 1_000_000;

    /**
     * This is how often, in ms, we should sync with System.currentTimeMillis()
     * It is a 2^n number as that is faster to compare than using modulo
     */
    private static final long PRECISION = 0b1111111111;

    /**
     * Lock used to ensure a single thread will be in charge of starting the clock.
     * If we get a race-condition and more than one thread tries to start the clock, we
     * return a roughly outdated value as an approximation is better than nothing.
     */
    private static final Lock startThreadLock = new ReentrantLock();

    private static final AtomicInteger counter = new AtomicInteger();
    private static final ByteBuffer asciiTimestamp = IntToAscii.createTimestampBuffer(System.currentTimeMillis());
    private static final IntToAscii itoa = new IntToAscii();

    /**
     * The thread responsible for keeping the clock up-to-date.
     * <br />
     * Because {@link System#currentTimeMillis()} is known to be slow on linux, we instead
     * increment the clock every X nanoseconds as defined by {@link Clock#REFRESH_RATE} and after every
     * Y iterations, as defined by {@link Clock#PRECISION}, we sync back with the system clock.
     */
    private static final Thread clockThread = Thread.ofVirtual().name("penna-clock-ticker").start(() -> {
        while(!Thread.currentThread().isInterrupted()) {
            if ((counter.incrementAndGet() & PRECISION) == 0x0) {
                var current = System.currentTimeMillis();
                itoa.longToAscii(current, asciiTimestamp);
            } else {
                IntToAscii.asciiIncrement(asciiTimestamp);
            }

            LockSupport.parkNanos(REFRESH_RATE);
        }
    });
    /**
     * This method returns a read only view to the timestamp buffer.
     * not started, this method will initialize it.
     * @return a roughly accurate current timestamp
     */
    public static ByteBuffer getTimestamp() {
        if (!clockThread.isAlive() && startThreadLock.tryLock()) {
            clockThread.start();
            startThreadLock.unlock();
        }

        return asciiTimestamp.asReadOnlyBuffer();
    }

}
