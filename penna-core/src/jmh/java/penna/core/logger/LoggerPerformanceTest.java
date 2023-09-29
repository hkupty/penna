package penna.core.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import penna.api.config.Config;
import penna.core.logger.utils.LogbackDevNullAppender;
import penna.core.sink.OutputManager;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LoggerPerformanceTest {

    @State(Scope.Thread)
    public static class TestBehavior {
        @Param({
                "simple",
                "modest",
                "moderate",
                "large"
        })
        String behavior;
        Throwable exception = new RuntimeException("with an exception");

        public void log(org.slf4j.Logger logger) {
            switch (behavior) {
                case "modest" -> {
                    logger.atInfo()
                            .addMarker(MarkerFactory.getMarker("For the win!"))
                            .log("Some event");
                }
                case "moderate" -> {
                    MDC.put("SomeKey", "some value");
                    logger
                            .atInfo()
                            .addMarker(MarkerFactory.getMarker("For the win!"))
                            .addArgument(UUID::randomUUID)
                            .log("Some event: {}");
                    MDC.remove("SomeKey");
                }
                case "large" -> {
                    MDC.put("SomeKey", "some value");
                    logger
                            .atInfo()
                            .addMarker(MarkerFactory.getMarker("For the win!"))
                            .addArgument(UUID::randomUUID)
                            .log("Some event: {}", exception);
                    MDC.remove("SomeKey");
                }
                default -> {
                    logger.atInfo().log("hello world");

                }
            }
        }
    }

    @State(Scope.Thread)
    public static class LogbackState {
        LoggerContext context = new LoggerContext();
        Logger logger;
        @Param({
                "contrib",
//                "logstash"
        })
        String encoder;

        private Encoder<ILoggingEvent> getContribEncoder() {
            var encoder = new JsonEncoder();
            encoder.start();
            return encoder;
        }

        private Encoder<ILoggingEvent> getLogstashEncoder() {
            var encoder = new LogstashEncoder();
            encoder.setContext(context);
            encoder.start();
            return encoder;
        }

        @Setup
        public void setup() {
            context.setName("JMH");
            logger = context.getLogger("jmh.test.logback");
            logger.setLevel(Level.INFO);
            OutputStreamAppender<ILoggingEvent> appender = new LogbackDevNullAppender();


            var encoder = switch (this.encoder) {
                case "contrib" -> getContribEncoder();
                case "logstash" -> getLogstashEncoder();
                default -> throw new RuntimeException("Unable to match logger");
            };

            appender.setContext(context);
            appender.setEncoder(encoder);
            logger.addAppender(appender);

            appender.start();
            context.start();
        }

    }

    @State(Scope.Thread)
    public static class PennaState {
        private OutputManager dumpToNull;
        TreeCache cache = new TreeCache(Config.getDefault());
        PennaLogger logger;

        @Setup
        public void setup() throws IOException {
            dumpToNull = new OutputManager.ToFile(new File("/dev/null"));
            OutputManager.Impl.set(() -> dumpToNull);
            logger = cache.getLoggerAt("jmh", "test", "penna");
        }
        @TearDown
        public void tearDown() throws IOException {
            dumpToNull.close();
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void penna(PennaState state, TestBehavior tb) throws IOException {
        tb.log(state.logger);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void logback(LogbackState state, TestBehavior tb) throws IOException {
        tb.log(state.logger);
    }


    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(20))
                .warmupIterations(3)
                .threads(1)
                .measurementIterations(3)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .addProfiler("gc")
                .build();

        new Runner(options).run();
    }

}
