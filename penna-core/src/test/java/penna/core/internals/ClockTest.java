package penna.core.internals;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class ClockTest {

    IntToAscii itoa = new IntToAscii();

    @Test
    void testClockIncrement() {
        ByteBuffer clock = ByteBuffer.allocate(13);
        itoa.longToAscii(1731320236053L ,clock);
        clock.rewind();
        var view = clock.asReadOnlyBuffer();
        var str1 = UTF_8.decode(view).toString();
        IntToAscii.asciiIncrement(clock);
        view.rewind();
        var str2 = UTF_8.decode(view).toString();
        assertNotEquals(str1, str2);
        assertEquals(13, str1.length());
    }


}