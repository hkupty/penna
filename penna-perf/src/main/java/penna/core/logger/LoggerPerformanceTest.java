package penna.core.logger;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import penna.core.logger.utils.PerfTestLoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoggerPerformanceTest {

    @State(Scope.Thread)
    public static class TestBehavior {
        @Param({
                "simple",
//                "modest",
//                "moderate",
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
                            .addArgument("static-value")
                            .log("Some event: {}");
                    MDC.remove("SomeKey");
                }
                case "large" -> {
                    MDC.put("SomeKey", "some value");
                    logger
                            .atInfo()
                            .addMarker(MarkerFactory.getMarker("For the win!"))
                            .addArgument("static-value")
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
    public static class TestState {
        PerfTestLoggerFactory.Implementation implementation = PerfTestLoggerFactory.Implementation.Penna;
        PerfTestLoggerFactory factory;
        Logger logger;

        @Setup
        public void setUp(Blackhole bh) {
            factory = PerfTestLoggerFactory.Factory.get(implementation);
            factory.setup(bh);
            logger = factory.getLogger("jmh." + implementation.name() + ".loggerTest");
        }

        @TearDown
        public void tearDown() throws IOException {
            factory.close();
        }

    }


    @Benchmark
    public void testLogger(TestState state, TestBehavior tb) throws IOException {
        tb.log(state.logger);
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(LoggerPerformanceTest.class.getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(40))
                .warmupIterations(3)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(50))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .addProfiler("gc")
                .addProfiler("perfnorm")
                .addProfiler("perfasm", "tooBigThreshold=2100")
                .threads(1)
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .jvmArgs("-Xmx8192m")
                .build();

        new Runner(options).run();
    }

}
