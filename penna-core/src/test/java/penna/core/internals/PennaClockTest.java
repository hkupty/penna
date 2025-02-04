package penna.core.internals;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class PennaClockTest {

    @Test
    void theTimestampShouldBeValid() throws IOException {
        AtomicLong timestamp = new AtomicLong();
        var clock = Clock.fixed(Instant.EPOCH, TimeZone.getTimeZone("UTC").toZoneId());
        var pc = new PennaClock(clock);
        var buffer = ByteBuffer.allocate(13);
        buffer.put(pc.getTimestampBuffer());

        assertDoesNotThrow(() -> {
            var ts = new String(buffer.array()).trim();
            timestamp.set(Long.parseLong(ts));
        });

        assertNotNull(Instant.ofEpochMilli(timestamp.get()));

        pc.close();
    }

    @Test
    void pennaIsSafeFromUnix2038Bug() throws IOException {
        AtomicLong timestamp = new AtomicLong();
        var clock = Clock.fixed(Instant.ofEpochSecond(2148629469L), TimeZone.getTimeZone("UTC").toZoneId());
        var pc = new PennaClock(clock);
        var buffer = ByteBuffer.allocate(13);
        buffer.put(pc.getTimestampBuffer());

        assertDoesNotThrow(() -> {
            var ts = new String(buffer.array()).trim();
            timestamp.set(Long.parseLong(ts));
        });

        var ts = Instant.ofEpochMilli(timestamp.get());
        assertNotNull(ts);

        pc.close();
    }

}