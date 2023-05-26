package penna.core.logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import penna.api.config.Config;
import penna.api.config.ConfigManager.ConfigItem.LoggerConfigItem;
import penna.core.logger.guard.*;
import penna.core.sink.OutputManager;
import penna.core.sink.PennaSink;
import penna.core.sink.SinkImpl;
import penna.core.sink.impl.DummySink;
import penna.core.slf4j.PennaLoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

class LoggerTests {
    record ThrowableLog(
            @JsonProperty("class") String throwable,
            String message,
            List<String> stacktrace
    ) {}

    record LogMessage(
            long timestamp,
            String level,
            String message,
            String logger,
            String thread,
            Map<String, String> mdc,
            List<String> tags,
            Map<String, Object> data,
            ThrowableLog throwable
    ) {}

    private static final ObjectMapper om = new ObjectMapper();

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
        AtomicInteger counter = new AtomicInteger(0);
        SinkImpl checker = new DummySink(mle -> counter.getAndIncrement());
        PennaLogEventBuilder.Factory.replaceSinkLocally(checker);

        PennaLogger pennaLogger = cache.getLoggerAt("test");


        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);

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

        AtomicReference<Marker> usedMarker = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            if (mle.getMarkers().size() > 0) {
                usedMarker.set(mle.getMarkers().get(0));
            }
        });

        PennaLogEventBuilder.Factory.replaceSinkLocally(checker);

        PennaLogger pennaLogger = cache.getLoggerAt("test");
        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);

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
        AtomicReference<String> message = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(mle -> {
            message.set(mle.message);
        });

        PennaLogEventBuilder.Factory.replaceSinkLocally(checker);
        PennaLogger pennaLogger = cache.getLoggerAt("test");

        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);

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
        AtomicReference<String> message = new AtomicReference<>(null);
        SinkImpl checker = new DummySink(ple -> {
            message.set(ple.message);
        });

        var exception = new Exception();

        PennaLogEventBuilder.Factory.replaceSinkLocally(checker);
        PennaLogger pennaLogger = cache.getLoggerAt("test");

        Assertions.assertEquals(InfoLevelGuard.singleton(), pennaLogger.levelGuard);

        pennaLogger.info("normal message", exception);
        Assertions.assertEquals("normal message", message.get());


//        pennaLogger.info("{} message", "formatted", exception);
//        Assertions.assertEquals("formatted message", message.get());
//
//        pennaLogger.info("with {} {} message", "two", "formatted", exception);
//        Assertions.assertEquals("with two formatted message", message.get());
//
//        pennaLogger.info("with {} {} {}", "three", "formatted", "message", exception);
//        Assertions.assertEquals("with three formatted message", message.get());
    }

    @Test
    void everything_added_to_the_log_is_present_in_the_message() throws IOException {
        Config config = Config.getDefault();
        TreeCache cache = new TreeCache(config);
        PennaLogger logger = cache.getLoggerAt("c", "est", "moi");

        File testFile = File.createTempFile("valid-message", ".json");
        var testOut = new OutputManager.ToFile(testFile);
        OutputManager.Impl.set(() -> testOut);

        MDC.put("key", "value");
        logger.atInfo()
                .addKeyValue("key", "kvp")
                .addMarker(MarkerFactory.getMarker("marker"))
                .setCause(new RuntimeException("exception"))
                .log("formatted {}", "message");
        MDC.clear();

        Assertions.assertDoesNotThrow(() -> om.readValue(testFile, LogMessage.class), () -> {
            try {
                om.readValue(testFile, LogMessage.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return ex.getMessage();
            }

            return "Unable to deserialize";
        });

        var logMessage = om.readValue(testFile, LogMessage.class);

        Assertions.assertEquals("kvp", logMessage.data().get("key"));
        Assertions.assertEquals("value", logMessage.mdc().get("key"));
        Assertions.assertEquals(1, logMessage.tags().size());
        Assertions.assertEquals("marker", logMessage.tags().get(0));
        Assertions.assertEquals("java.lang.RuntimeException", logMessage.throwable().throwable());
        Assertions.assertEquals("exception", logMessage.throwable().message());

        // Keep this line at the bottom, so we can inspect the file if the test breaks
        testFile.deleteOnExit();
        testOut.close();
    }
}
