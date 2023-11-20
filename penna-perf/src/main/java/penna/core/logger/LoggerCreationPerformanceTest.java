package penna.core.logger;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import penna.core.logger.utils.PerfTestLoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class LoggerCreationPerformanceTest {


    @State(Scope.Thread)
    public static class TestState {

        public Random random = new Random();
        public String[] randomWithCollisions;
        public String[] randomDistinct;
        int size = 1024;

        //        @Param
        PerfTestLoggerFactory.Implementation implementation = PerfTestLoggerFactory.Implementation.Penna;
        PerfTestLoggerFactory factory;

        @Setup
        public void setUp() {
            factory = PerfTestLoggerFactory.Factory.get(implementation);
            factory.setup();

            var collisions = random.ints(size / 2, 0, size / 4)
                    .boxed();

            var unique = random.ints(size * 2L, size / 2, size * 16)
                    .boxed()
                    .distinct();

            randomWithCollisions = Stream.concat(collisions, unique).map(ix -> "jmh.task.nr-" + ix).limit(size).toArray(String[]::new);
            randomDistinct = random.ints().distinct().limit(size).boxed().map(ix -> "jmh.task.nr-" + ix).toArray(String[]::new);
        }

        Logger getLogger(String name) {
            return factory.getLogger(name);
        }

        @TearDown
        public void tearDown() throws IOException {
            factory.close();
        }

    }

    @Benchmark
    public void alwaysNewLogger(TestState state, Blackhole bh) {
        for (int i = 0; i < state.size; i++) {
            bh.consume(state.getLogger(state.randomDistinct[i]));
        }
    }

    @Benchmark
    public void alwaysSameLogger(TestState state, Blackhole bh) {
        for (int i = 0; i < state.size; i++) {
            bh.consume(state.getLogger("jmh.task"));
        }
    }

    @Benchmark
    public void aFewDifferentLoggers(TestState state, Blackhole bh) {
        for (int i = 0; i < state.size; i++) {
            bh.consume(state.getLogger(state.randomWithCollisions[i]));
        }
    }


    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(LoggerCreationPerformanceTest.class.getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(20))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(25))
                .measurementIterations(3)
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
