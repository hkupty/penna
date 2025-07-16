package penna.core.logger;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import penna.core.logger.utils.PerfTestLoggerFactory;
import penna.core.logger.utils.RunnerOptions;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ThreadsPerformanceTest {

    @State(Scope.Thread)
    public static class TestBehavior {
        @Param({
                "simple",
                "large"
        })
        String behavior;
        Throwable exception = new RuntimeException("with an exception");

        public void log(Logger logger) {
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
        @Param({"8", "64"})
        int threads;

        @Param({"Penna", "Log4j", "Logback"})
        PerfTestLoggerFactory.Implementation implementation;
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
    public void testLogger(TestState state, TestBehavior tb) throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(state.threads);
        int messagesPerLogger = 512 / state.threads;
        for (int t = 0; t < state.threads; t++) {
          Thread.ofVirtual().start(() -> {
            for (int i = 0; i <= messagesPerLogger; i++) {
              tb.log(state.logger);
            }
            cdl.countDown();
          });
        }
        cdl.await();
    }

    public static void main(String[] args) throws Exception {
        var options = RunnerOptions
                .averageTime(ThreadsPerformanceTest.class.getName() + ".*")
                .addProfiler("gc")
//                .addProfiler("perfnorm")
//                .addProfiler("perfasm")
                .addProfiler("jfr")
                .build();

        new Runner(options).run();
    }

}
