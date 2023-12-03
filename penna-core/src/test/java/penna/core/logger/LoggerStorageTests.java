package penna.core.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LoggerStorageTests {

    @Test
    public void getOrCreate_creates_a_logger() {
        var cache = new LoggerStorage();
        var logger = cache.getOrCreate("com.for.testing");
        assertEquals(logger.name, "com.for.testing");
    }


    @Test
    public void calling_getOrCreate_twice_does_not_create_duplicates() {
        var cache = new LoggerStorage();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing");

        assertSame(logger1, logger2);
    }
}
