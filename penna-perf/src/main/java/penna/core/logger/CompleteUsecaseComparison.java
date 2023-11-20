package penna.core.logger;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import penna.core.logger.utils.PerfTestLoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CompleteUsecaseComparison {


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

        @Param
        PerfTestLoggerFactory.Implementation implementation;
        PerfTestLoggerFactory factory;

        @Setup
        public void setUp() {
            factory = PerfTestLoggerFactory.Factory.get(implementation);
            factory.setup();

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
    public void alwaysNewLogger(TestState state) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(state.threads);

        for (int i = 0; i < state.threads; i++) {
            int index = i;
            Thread.ofVirtual()
                    .name("jmh-", i)
                    .start(() -> {
                        try {
                            var logger = state.getLogger("jmh.task-" + index);
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

    @Benchmark
    public void aFewDifferentLoggers(TestState state) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(state.threads);

        for (int i = 0; i < state.threads; i++) {
            int index = i;
            Thread.ofVirtual()
                    .name("jmh-", i)
                    .start(() -> {
                        try {
                            var logger = state.getLogger(state.randomLoggers[index]);
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


    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(CompleteUsecaseComparison.class.getName() + ".*")
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
                .threads(1)
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .jvmArgs("-Xmx8192m")
                .build();

        new Runner(options).run();
    }

}
