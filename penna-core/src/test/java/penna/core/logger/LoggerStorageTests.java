package penna.core.logger;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import penna.api.config.Config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LoggerStorageTests {

    @Test
    public void getOrCreateCreatesALogger() {
        var cache = new LoggerStorage();
        var logger = cache.getOrCreate("com.for.testing");
        assertEquals(logger.name, "com.for.testing");
    }


    @Test
    public void callingGetOrCreateTwiceDoesNotCreateDuplicates() {
        var cache = new LoggerStorage();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing");

        assertSame(logger1, logger2);
    }


    @Test
    public void replaceTheWholeTreeAffectsLeafObjects() {
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
    public void replacePrefixAffectsOnlyDescendants() {
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
    public void updatePrefixAffectsOnlyDescendants() {
        var cache = new LoggerStorage();
        var defaults = Config.getDefault();
        var logger1 = cache.getOrCreate("com.for.testing");
        var logger2 = cache.getOrCreate("com.for.testing.other");
        var logger3 = cache.getOrCreate("com.for.unrelated");
        var logger4 = cache.getOrCreate("com.more.unrelated");
        var logger5 = cache.getOrCreate("io.completely.unrelated");

        assertEquals(defaults.level(), logger1.levelGuard.level());
        assertEquals(defaults.level(), logger2.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());

        cache.updateConfig("com.for.testing", config -> config.replaceLevel(Level.DEBUG));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(defaults.level(), logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());

        cache.updateConfig("com.for.unrelated", config -> config.replaceLevel(Level.WARN));

        assertEquals(Level.DEBUG, logger2.levelGuard.level());
        assertEquals(Level.DEBUG, logger1.levelGuard.level());
        assertEquals(Level.WARN, logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());
        cache.updateConfig("com.for", config -> config.replaceLevel(Level.TRACE));

        assertEquals(Level.TRACE, logger2.levelGuard.level());
        assertEquals(Level.TRACE, logger1.levelGuard.level());
        assertEquals(Level.TRACE, logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(defaults.level(), logger5.levelGuard.level());

        cache.updateConfig("io", config -> config.replaceLevel(Level.ERROR));

        assertEquals(Level.TRACE, logger2.levelGuard.level());
        assertEquals(Level.TRACE, logger1.levelGuard.level());
        assertEquals(Level.TRACE, logger3.levelGuard.level());
        assertEquals(defaults.level(), logger4.levelGuard.level());
        assertEquals(Level.ERROR, logger5.levelGuard.level());
    }

    @Test
    public void weAlwaysGetTheRightLogger() {
        var cache = new LoggerStorage();
        var loggers = List.of(
                "com.AAA.AAA", "com.AAA.AAA", "io.aaa.zzz.AAA", "io.aaa.zzz"
        );

        for (var logger : loggers) {
            assertEquals(logger, cache.getOrCreate(logger).name);
        }
    }
}
