package maple.core.logger;

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

public class LoggerTests {

    @Test
    public void log_levels_are_respected() {
        var factory = MapleLoggerFactory.getInstance();
        if (!(factory.getLogger("test") instanceof MapleLogger mapleLogger)) {
            Assertions.fail("MapleLoggerFactory is not returning an MapleLogger");
            return;
        }

        // null-level, no logs
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(null)));
        Assertions.assertEquals(NOPGuard.singleton(), mapleLogger.levelGuard);

        // Trace
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.TRACE)));
        Assertions.assertEquals(TraceLevelGuard.singleton(), mapleLogger.levelGuard);

        // Debug
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.DEBUG)));
        Assertions.assertEquals(DebugLevelGuard.singleton(), mapleLogger.levelGuard);

        // Info
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.INFO)));
        Assertions.assertEquals(InfoLevelGuard.singleton(), mapleLogger.levelGuard);

        // Warn
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.WARN)));
        Assertions.assertEquals(WarnLevelGuard.singleton(), mapleLogger.levelGuard);

        // Error
        factory.configure(new LoggerConfigItem("test", config -> config.replaceLevel(Level.ERROR)));
        Assertions.assertEquals(ErrorLevelGuard.singleton(), mapleLogger.levelGuard);
    }

    @Test
    public void can_write_log_messages() {
        var factory = MapleLoggerFactory.getInstance();
        if (!(factory.getLogger("test") instanceof MapleLogger mapleLogger)) {
            Assertions.fail("MapleLoggerFactory is not returning an MapleLogger");
            return;
        }
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
    public void markers_are_kept() {
        var factory = MapleLoggerFactory.getInstance();
        if (!(factory.getLogger("test") instanceof MapleLogger mapleLogger)) {
            Assertions.fail("MapleLoggerFactory is not returning an MapleLogger");
            return;
        }

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
}
