package penna.core.logger;

import penna.api.config.Config;
import penna.api.config.ConfigManager.ConfigItem.LoggerConfigItem;
import penna.core.logger.guard.*;
import penna.core.sink.SinkImpl;
import penna.core.sink.impl.DummySink;
import penna.core.slf4j.PennaLoggerFactory;
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
        PennaLogger pennaLogger = cache.getLoggerAt("test");
        String[] ref = new String[] {"test"};

        // null-level, no logs
        cache.updateConfig(ref, config -> config.replaceLevel(null));
        Assertions.assertEquals(NOPGuard.singleton(), pennaLogger.levelGuard);

        // Trace
        cache.updateConfig(ref, config -> config.replaceLevel(Level.TRACE));
        Assertions.assertEquals(TraceLevelGuard.singleton(), pennaLogger.levelGuard);

        // Debug
        cache.updateConfig(ref, config -> config.replaceLevel(Level.DEBUG));
        Assertions.assertEquals(DebugLevelGuard.singleton(), pennaLogger.levelGuard);

        // Info
        cache.updateConfig(ref, config -> config.replaceLevel(Level.INFO));
        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);

        // Warn
        cache.updateConfig(ref, config -> config.replaceLevel(Level.WARN));
        Assertions.assertEquals(WarnLevelGuard.singleton(), pennaLogger.levelGuard);

        // Error
        cache.updateConfig(ref, config -> config.replaceLevel(Level.ERROR));
        Assertions.assertEquals(ErrorLevelGuard.singleton(), pennaLogger.levelGuard);
    }

    @Test
    void can_write_log_messages() {
        var cache = new TreeCache(Config.getDefault());
        PennaLogger pennaLogger = cache.getLoggerAt("test");

        AtomicInteger counter = new AtomicInteger(0);
        SinkImpl checker = new DummySink(mle -> counter.getAndIncrement());

        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);

        pennaLogger.sink.set(checker);

        pennaLogger.trace("should not log");
        Assertions.assertEquals(0, counter.get());

        pennaLogger.debug("should not log");
        Assertions.assertEquals(0, counter.get());

        pennaLogger.info("something");
        Assertions.assertEquals(1, counter.get());

        pennaLogger.warn("something");
        Assertions.assertEquals(2, counter.get());

        pennaLogger.error("something");
        Assertions.assertEquals(3, counter.get());
    }


    @Test
    void markers_are_kept() {
        var cache = new TreeCache(Config.getDefault());
        PennaLogger pennaLogger = cache.getLoggerAt("test");

        AtomicReference<Marker> usedMarker = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            if (mle.getMarkers().size() > 0) {
                usedMarker.set(mle.getMarkers().get(0));
            }
        });

        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);
        pennaLogger.sink.set(checker);

        Marker ref = MarkerFactory.getMarker("ref");

        pennaLogger.info("no marker");
        Assertions.assertNull(usedMarker.get());

        pennaLogger.debug(ref, "should not log");
        Assertions.assertNull(usedMarker.get());

        pennaLogger.info(ref, "should not log");
        Assertions.assertEquals(ref, usedMarker.get());
    }

    @Test
    void messages_arrive_formatted_on_the_sink() {
        var cache = new TreeCache(Config.getDefault());
        PennaLogger pennaLogger = cache.getLoggerAt("test");

        AtomicReference<String> message = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            message.set(mle.message);
        });

        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);
        pennaLogger.sink.set(checker);

        pennaLogger.debug("should not log");
        Assertions.assertNull(message.get());

        pennaLogger.info("normal message");
        Assertions.assertEquals("normal message", message.get());


        pennaLogger.info("{} message", "formatted");
        Assertions.assertEquals("formatted message", message.get());
    }

    @Test
    void formatting_does_not_use_throwables() {
        var cache = new TreeCache(Config.getDefault());
        PennaLogger pennaLogger = cache.getLoggerAt("test");

        AtomicReference<String> message = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            message.set(mle.message);
        });

        var exception = new Exception();

        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);
        pennaLogger.sink.set(checker);

        pennaLogger.info("normal message", exception);
        Assertions.assertEquals("normal message", message.get());


        pennaLogger.info("{} message", "formatted", exception);
        Assertions.assertEquals("formatted message", message.get());

        pennaLogger.info("with {} {} message", "two", "formatted", exception);
        Assertions.assertEquals("with two formatted message", message.get());

        pennaLogger.info("with {} {} {}", "three", "formatted", "message", exception);
        Assertions.assertEquals("with three formatted message", message.get());
    }
}
