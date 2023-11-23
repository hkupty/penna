package penna.core.logger;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import penna.core.logger.utils.PerfTestLoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CompleteUsecasePerformanceTest {


    @State(Scope.Thread)
    public static class TestState {

        public Random random = new Random();
        public String[] randomLoggers;
        @Param({
//                "1",
                "4",
//                "16",
//                "64",
//                "256"
        })
        int threads;

        PerfTestLoggerFactory factory;

        @Setup
        public void setUp(Blackhole bh) {
            factory = PerfTestLoggerFactory.Factory.get(PerfTestLoggerFactory.Implementation.Penna);
            factory.setup(bh);

            randomLoggers = random.ints(threads, 0, (int) Math.ceil(threads * 1.6))
                    .boxed()
                    .map(ix -> "jmh.task.child-" + ix)
                    .toArray(String[]::new);
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
    public void alwaysSameLogger(TestState state) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(state.threads);

        for (int i = 0; i < state.threads; i++) {
            Thread.ofVirtual()
                    .name("jmh-", i)
                    .start(() -> {
                        try {
                            var logger = state.getLogger("jmh.task");
                            logger.info("Message 1");
                            logger.info("Message 2");
                            logger.info("Message 3");
                            logger.info("Message 4");
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        latch.await();
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(CompleteUsecasePerformanceTest.class.getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .measurementTime(TimeValue.seconds(30))
                .warmupTime(TimeValue.seconds(20))
                .warmupIterations(3)
                .measurementIterations(3)
                .forks(1)
                .threads(1)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .addProfiler("gc")
                .addProfiler("perfnorm")
                .addProfiler("perfasm", "tooBigThreshold=2100")
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .jvmArgs("-Xmx8192m")
                .build();

        new Runner(options).run();
    }

}
