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
    public static class TestState {
        @Param({
                "Penna",
                "IfBasedLogger"
        })
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
    public void mixedLevels(TestState state) throws IOException {
        state.logger.atInfo().log("hello world");
        state.logger.atTrace().log("hello world");
        state.logger.atTrace().log("hello world");
        state.logger.atWarn().log("hello world");
        state.logger.atDebug().log("hello world");
        state.logger.atError().log("hello world");
    }

    @Benchmark
    public void onlyAllowed(TestState state) throws IOException {
        state.logger.atInfo().log("hello world");
        state.logger.atWarn().log("hello world");
        state.logger.atError().log("hello world");
    }

    @Benchmark
    public void onlyForbidden(TestState state) throws IOException {
        state.logger.atTrace().log("hello world");
        state.logger.atTrace().log("hello world");
        state.logger.atDebug().log("hello world");
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(LoggerPerformanceTest.class.getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(30))
                .forks(2)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .threads(1)
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .jvmArgs("-Xmx8192m")
                .build();

        new Runner(options).run();
    }

}
