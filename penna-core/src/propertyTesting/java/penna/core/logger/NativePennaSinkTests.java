package penna.core.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import penna.api.config.Config;
import penna.api.models.LogField;
import penna.core.models.LogConfig;
import penna.core.models.PennaLogEvent;
import penna.core.sink.OutputManager;
import penna.core.sink.PennaSink;
import penna.core.sink.SinkImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class NativePennaSinkTests {

    private static final ObjectMapper om = new ObjectMapper();
    @Provide
    Arbitrary<LogField[]> fields() {
        return Arbitraries.of(LogField.class).array(LogField[].class).uniqueElements();
    }

    @Provide
    Arbitrary<PennaLogEvent> simpleEvent() {
        Arbitrary<String> messages = Arbitraries.strings()
                .all()
                .excludeChars('"',
                        '\u0000', '\u0001', '\u0002',
                        '\u0003', '\u0004', '\u0005',
                        '\u0006', '\u0007', '\u0008',
                        '\u000B', '\u000C', '\u000F',
                        '\u0010', '\u0011', '\u0012',
                        '\u0013', '\u0014', '\u0015',
                        '\u0016', '\u001A', '\u001B',
                        '\u001C', '\u001D', '\u001E',
                        '\u001F'
                )
                .ofMaxLength(2048)
                .ofMinLength(1);
        Arbitrary<String> threads = Arbitraries
                .strings()
                .ascii()
                .excludeChars('"')
                .ofMaxLength(256)
                .ofMinLength(2);
        Arbitrary<List<Marker>> markers = Arbitraries
                .strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(MarkerFactory::getMarker)
                .list()
                .ofMinSize(0)
                .ofMaxSize(4);

        Arbitrary<String> keys = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(5);
        Arbitrary<List<KeyValuePair>> kvps = Arbitraries
                .strings()
                .all()
                .excludeChars('"')
                .flatMap(val -> keys.map(key -> new KeyValuePair(key, val)))
                .list();
        Arbitrary<Level> levels = Arbitraries.of(Level.class);

        return Builders.withBuilder(PennaLogEvent::new)
                .use(messages).in((evt, m) -> {evt.message = m; return evt;})
                .use(markers).in((evt, m) -> {evt.markers.addAll(m); return evt;})
                .use(kvps).in((evt, m) -> {evt.keyValuePairs.addAll(m); return evt;})
                .use(levels).in((evt, m) -> {evt.level = m; return evt;})
                .use(threads).in((evt, m) -> {evt.threadName = m; return evt;})
                .use(throwableArbitrary()).withProbability(0.3).in((evt, t) -> {evt.throwable = t; return evt;})
                .build();
    }

    @Provide
    Arbitrary<Throwable> throwableArbitrary() {
        Arbitrary<String> msg = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(128);

        Arbitrary<Throwable> withMessage = msg.map(RuntimeException::new);
        Arbitrary<Throwable> withoutMessage = Arbitraries.create(RuntimeException::new);

        Arbitrary<Throwable> withParentWithoutMessage = Arbitraries
                .oneOf(withMessage, withoutMessage)
                .map(RuntimeException::new);

        Arbitrary<Throwable> withParentWithMessage = Arbitraries
                .oneOf(withMessage, withoutMessage)
                .flatMap(parent -> msg.map(m -> new RuntimeException(m, parent)));

        return Arbitraries.oneOf(withMessage, withoutMessage, withParentWithMessage, withParentWithoutMessage);
    }

    @Property
    void validJsonMessage(@ForAll("fields") LogField[] fields) throws IOException {
        File testFile = File.createTempFile("valid-message", ".json");
        var out = new OutputManager.ToFile(testFile);
        OutputManager.Impl.set(() -> out);

        // HACK currently, only for tests, it is necessary to replace the sink at each iteration
        SinkImpl sink = PennaSink.getSink();
        PennaLogEventBuilder.Factory.replaceSinkLocally(sink);

        Config config = Config.getDefault().replaceFields(fields);
        TreeCache cache = new TreeCache(config);
        PennaLogger logger = cache.getLoggerAt("c", "est", "moi");


        Marker marker = MarkerFactory.getMarker("something");

        logger.atInfo()
                .addKeyValue("Something", 1)
                .addMarker(marker)
                .setCause(new RuntimeException("oh-noes!"))
                .log("My message");

        Assertions.assertDoesNotThrow(() -> om.readValue(testFile, Map.class));
        testFile.deleteOnExit();
        out.close();
    }


    @Property
    void validGenericMessage(
            @ForAll("fields") LogField[] fields,
            @ForAll("simpleEvent") PennaLogEvent event
    ) throws IOException {
        File testFile = File.createTempFile("valid-message", ".json");
        var out = new OutputManager.ToFile(testFile);
        OutputManager.Impl.set(() -> out);

        // HACK currently, only for tests, it is necessary to replace the sink at each iteration
        SinkImpl sink = PennaSink.getSink();
        PennaLogEventBuilder.Factory.replaceSinkLocally(sink);

        Config config = Config.getDefault().replaceFields(fields);
        TreeCache cache = new TreeCache(config);
        PennaLogger logger = cache.getLoggerAt("c", "est", "moi");

        event.config = LogConfig.fromConfig(Config.withFields(fields));
        event.logger = logger;

        logger.log(event);

        Assertions.assertDoesNotThrow(() -> om.readValue(testFile, Map.class));
        testFile.deleteOnExit();
        out.close();
    }


}
