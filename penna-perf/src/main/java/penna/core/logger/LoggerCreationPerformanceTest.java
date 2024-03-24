package penna.core.logger;

import com.google.common.collect.Streams;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.slf4j.Logger;
import penna.core.logger.utils.PerfTestLoggerFactory;
import penna.core.logger.utils.RunnerOptions;

import java.io.IOException;
import java.util.Random;

public class LoggerCreationPerformanceTest {


    @State(Scope.Thread)
    public static class TestState {

        public Random random = new Random();
        public String[] randomDistinct;

        public static final int SIZE = 1024;
        public static final int UNIQUE = 640;
        public static final String[] topLevel = new String[]{
                "com",
                "org",
                "io",
        };

        public static final String[] midlevel = new String[]{
                "app",
                "test",
                "jmh",
                "other",
                "dummy",
                "special",
                "last"
        };

        public static final String[] botLevel = new String[]{
                "controller",
                "model",
                "service",
                "adapter",
                "util",
                "port",
                "view",
                "presenter"
        };

        @Param()
        PerfTestLoggerFactory.Implementation implementation;
        PerfTestLoggerFactory factory;

        @Setup
        public void setUp(Blackhole bh) {
            factory = PerfTestLoggerFactory.Factory.get(implementation);
            factory.setup(bh);

            var tops = random.ints(0, topLevel.length).mapToObj(ix -> topLevel[ix]);
            var mids = random.ints(0, midlevel.length).mapToObj(ix -> midlevel[ix]);
            var bots = random.ints(0, botLevel.length).mapToObj(ix -> botLevel[ix]);

            var nss = Streams.zip(
                    Streams.zip(tops, mids, (t, m) -> t + "." + m),
                    bots, (p, b) -> p + "." + b);


            randomDistinct = nss.map(x -> RandomStringUtils.randomAlphabetic(2, 16)).distinct().limit(SIZE).toArray(String[]::new);

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
        var factory = PerfTestLoggerFactory.Factory.get(state.implementation);
        factory.setup(bh);
        for (int i = 0; i < TestState.SIZE; i++) {
            bh.consume(factory.getLogger(state.randomDistinct[i]));
        }
    }

    @Benchmark
    public void aFewDifferentLoggers(TestState state, Blackhole bh) {
        var factory = PerfTestLoggerFactory.Factory.get(state.implementation);
        factory.setup(bh);
        for (int i = 0; i < TestState.SIZE; i++) {
            bh.consume(factory.getLogger(state.randomDistinct[i % TestState.UNIQUE]));
        }
    }


    public static void main(String[] args) throws Exception {
        var options = RunnerOptions
                .averageTime(LoggerCreationPerformanceTest.class.getName() + ".*")
//                .warmupIterations(2)
//                .warmupTime(TimeValue.seconds(20))
                .addProfiler("gc")
                .addProfiler("perfnorm")
                .addProfiler("perfasm")
                .addProfiler("jfr")
                .build();
        new Runner(options).run();

    }

}
