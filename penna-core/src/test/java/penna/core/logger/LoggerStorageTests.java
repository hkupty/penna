package penna.core.logger;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import penna.api.config.Config;

import java.util.List;

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


    @Test
    public void update_the_whole_tree_affects_leaf_objects() {
        var cache = new LoggerStorage();
        var defaults = Config.getDefault();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");

        assertEquals(defaults.level(), logger1.levelGuard.level());
        assertEquals(defaults.level(), logger2.levelGuard.level());

        cache.replaceConfig(Config.withFields(Level.DEBUG));

        assertEquals(Level.DEBUG, cache.getOrCreate("com.for.testing").levelGuard.level());
        assertEquals(Level.DEBUG, cache.getOrCreate("com.for.testing.other").levelGuard.level());
    }

    @Test
    public void update_prefix_doesnt_change_all_only_descendants() {
        var cache = new LoggerStorage();
        var defaults = Config.getDefault();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");
        var logger3 = cache.getOrCreate("com.for.unrelated");

        assertEquals(defaults.level(), logger1.levelGuard.level());
        assertEquals(defaults.level(), logger2.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());

        cache.replaceConfig("com.for.testing", Config.withFields(Level.DEBUG));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());
        cache.replaceConfig("com.for.unrelated", Config.withFields(Level.WARN));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(Level.WARN, logger3.levelGuard.level());
    }


    @Test
    public void we_always_get_the_right_logger() {
        var cache = new LoggerStorage();
        var loggers = List.of(
                "com.AAA.AAA", "com.AAA.AAA", "com.AAA.AAA", "io.aaa.zzz.AAA", "io.aaa.zzz"
        );

        for (var logger : loggers) {
            assertEquals(cache.getOrCreate(logger).name, logger);
        }
    }
}
