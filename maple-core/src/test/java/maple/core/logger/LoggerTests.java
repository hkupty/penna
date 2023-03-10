package maple.core.logger;

import maple.api.config.Config;
import maple.api.config.ConfigManager.ConfigItem.LoggerConfigItem;
import maple.core.logger.guard.*;
import maple.core.sink.SinkImpl;
import maple.core.sink.impl.DummySink;
import maple.core.slf4j.MapleLoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

class LoggerTests {

    @Test
    void log_levels_are_respected() {
        var cache = new TreeCache(Config.getDefault());
        MapleLogger mapleLogger = cache.getLoggerAt("test");
        String[] ref = new String[] {"test"};

        // null-level, no logs
        cache.updateConfig(ref, config -> config.replaceLevel(null));
        Assertions.assertEquals(NOPGuard.singleton(), mapleLogger.levelGuard);

        // Trace
        cache.updateConfig(ref, config -> config.replaceLevel(Level.TRACE));
        Assertions.assertEquals(TraceLevelGuard.singleton(), mapleLogger.levelGuard);

        // Debug
        cache.updateConfig(ref, config -> config.replaceLevel(Level.DEBUG));
        Assertions.assertEquals(DebugLevelGuard.singleton(), mapleLogger.levelGuard);

        // Info
        cache.updateConfig(ref, config -> config.replaceLevel(Level.INFO));
        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);

        // Warn
        cache.updateConfig(ref, config -> config.replaceLevel(Level.WARN));
        Assertions.assertEquals(WarnLevelGuard.singleton(), mapleLogger.levelGuard);

        // Error
        cache.updateConfig(ref, config -> config.replaceLevel(Level.ERROR));
        Assertions.assertEquals(ErrorLevelGuard.singleton(), mapleLogger.levelGuard);
    }

    @Test
    void can_write_log_messages() {
        var cache = new TreeCache(Config.getDefault());
        MapleLogger mapleLogger = cache.getLoggerAt("test");

        AtomicInteger counter = new AtomicInteger(0);
        SinkImpl checker = new DummySink(mle -> counter.getAndIncrement());

        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);

        mapleLogger.sink.set(checker);

        mapleLogger.trace("should not log");
        Assertions.assertEquals(0, counter.get());

        mapleLogger.debug("should not log");
        Assertions.assertEquals(0, counter.get());

        mapleLogger.info("something");
        Assertions.assertEquals(1, counter.get());

        mapleLogger.warn("something");
        Assertions.assertEquals(2, counter.get());

        mapleLogger.error("something");
        Assertions.assertEquals(3, counter.get());
    }


    @Test
    void markers_are_kept() {
        var cache = new TreeCache(Config.getDefault());
        MapleLogger mapleLogger = cache.getLoggerAt("test");

        AtomicReference<Marker> usedMarker = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            if (mle.getMarkers().size() > 0) {
                usedMarker.set(mle.getMarkers().get(0));
            }
        });

        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);
        mapleLogger.sink.set(checker);

        Marker ref = MarkerFactory.getMarker("ref");

        mapleLogger.info("no marker");
        Assertions.assertNull(usedMarker.get());

        mapleLogger.debug(ref, "should not log");
        Assertions.assertNull(usedMarker.get());

        mapleLogger.info(ref, "should not log");
        Assertions.assertEquals(ref, usedMarker.get());
    }

    @Test
    void messages_arrive_formatted_on_the_sink() {
        var cache = new TreeCache(Config.getDefault());
        MapleLogger mapleLogger = cache.getLoggerAt("test");

        AtomicReference<String> message = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            message.set(mle.message);
        });

        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);
        mapleLogger.sink.set(checker);

        mapleLogger.debug("should not log");
        Assertions.assertNull(message.get());

        mapleLogger.info("normal message");
        Assertions.assertEquals("normal message", message.get());


        mapleLogger.info("{} message", "formatted");
        Assertions.assertEquals("formatted message", message.get());
    }

    @Test
    void formatting_does_not_use_throwables() {
        var cache = new TreeCache(Config.getDefault());
        MapleLogger mapleLogger = cache.getLoggerAt("test");

        AtomicReference<String> message = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            message.set(mle.message);
        });

        var exception = new Exception();

        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);
        mapleLogger.sink.set(checker);

        mapleLogger.info("normal message", exception);
        Assertions.assertEquals("normal message", message.get());


        mapleLogger.info("{} message", "formatted", exception);
        Assertions.assertEquals("formatted message", message.get());

        mapleLogger.info("with {} {} message", "two", "formatted", exception);
        Assertions.assertEquals("with two formatted message", message.get());

        mapleLogger.info("with {} {} {}", "three", "formatted", "message", exception);
        Assertions.assertEquals("with three formatted message", message.get());
    }
}
