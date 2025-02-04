package penna.core.internals;

import org.jetbrains.annotations.VisibleForTesting;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

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
public final class PennaClock implements Closeable {

    private final AtomicInteger counter = new AtomicInteger();
    private final ByteBuffer asciiTimestamp;
    private final IntToAscii itoa = new IntToAscii();
    private final Clock clock;
    private final Thread clockThread;

    @VisibleForTesting
    public PennaClock(Clock clock) {
        this.clock = clock;
        asciiTimestamp = IntToAscii.createTimestampBuffer(clock.millis());
        this.clockThread = Thread.ofVirtual().name("penna-clock-ticker").start(() -> {
            /*
             * This is how often, in ms, we should sync with System.currentTimeMillis()
             * It is a 2^n number as that is faster to compare than using modulo
             */
            long PRECISION = 0b1111111111;

            /*
             * This is the amount of time, in nanos, that we will wait before incrementing.
             * Keeping this value at 1 million ensure we tick at roughly every 1ms.
             */
            long REFRESH_RATE = 1_000_000;

            while (!Thread.currentThread().isInterrupted()) {
                if ((counter.incrementAndGet() & PRECISION) == 0x0) {
                    this.syncToClock();
                } else {
                    IntToAscii.asciiIncrement(asciiTimestamp);
                }
                LockSupport.parkNanos(REFRESH_RATE);
            }
        });
    }

    public PennaClock() {
        this(Clock.systemDefaultZone());
    }

    private void syncToClock() {
        itoa.longToAscii(this.clock.millis(), asciiTimestamp);
    }

    /**
     * This method returns a read only view to the timestamp buffer.
     * @return a roughly accurate current timestamp
     */
    public ByteBuffer getTimestampBuffer() {
        return asciiTimestamp.asReadOnlyBuffer();
    }

    @Override
    public void close() throws IOException {
        clockThread.interrupt();
    }
}
