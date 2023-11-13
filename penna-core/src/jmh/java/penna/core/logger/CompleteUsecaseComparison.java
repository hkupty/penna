package penna.core.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.OutputStreamAppender;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import penna.api.config.Config;
import penna.core.logger.utils.LogbackDevNullAppender;
import penna.core.sink.CoreSink;
import penna.core.sink.SinkManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Pattern;

public class CompleteUsecaseComparison {

    @State(Scope.Thread)
    public static class LogbackState {

        @Param({
                "1",
                "4",
//                "16",
//                "64",
//                "256"
        })
        int threads;

        public final Random random = new Random();
        LoggerContext context = new LoggerContext();

        public org.slf4j.Logger getLogger(String name) {
            Logger logger;
            context.setName("JMH");
            context.setMDCAdapter(new LogbackMDCAdapter());
            logger = context.getLogger(name);
            logger.setLevel(Level.INFO);
            OutputStreamAppender<ILoggingEvent> appender = new LogbackDevNullAppender();

            var encoder = new JsonEncoder();
            encoder.start();
            appender.setContext(context);
            appender.setEncoder(encoder);
            logger.addAppender(appender);

            appender.start();
            context.start();
            return logger;
        }

    }

    @State(Scope.Thread)
    public static class PennaState {
        private static final Pattern DOT_SPLIT = Pattern.compile("\\.");

        @Param({
                "1",
                "4",
//                "16",
//                "64",
//                "256"
        })
        int threads;

        public final Random random = new Random();
        private FileOutputStream fos;
        TreeCache cache = new TreeCache(Config.getDefault());

        @Setup
        public void setup() {
            var devnull = new File("/dev/null");
            try {
                fos = new FileOutputStream(devnull);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Dev null doesn't exist!", e);
            }

            SinkManager.Instance.replace(() -> new CoreSink(fos));
        }
        @TearDown
        public void tearDown() throws IOException {
            fos.close();
        }


        public org.slf4j.Logger getLogger(String name) {
            return cache.getLoggerAt(DOT_SPLIT.split(name));
        }
    }

    @Benchmark
    public long penna(PennaState state) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(state.threads);

        for (int i = 0; i < state.threads; i++) {
            int index = i;
             Thread.ofVirtual()
                    .name("jmh-penna-", i)
                    .start(() -> {
                        try {
                            var logger = state.getLogger("jmh.penna.task" + index);
                            logger.info("Message 1");
                            LockSupport.parkNanos(180 + state.random.nextInt(0, 40));
                            logger.info("Message 2");
                            LockSupport.parkNanos(180 + state.random.nextInt(0, 40));
                            logger.info("Message 3");
                            LockSupport.parkNanos(180 + state.random.nextInt(0, 40));
                            logger.info("Message 4");
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        latch.await();

        return System.currentTimeMillis();
    }


//    @Benchmark
    public long logback(LogbackState state) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(state.threads);

        for (int i = 0; i < state.threads; i++) {
            int index = i;
            Thread.ofVirtual().name("jmh-logback-" + i).start(() -> {
                try {
                    var logger = state.getLogger("jmh.logback.task" + index);
                    logger.info("Message 1");
                    LockSupport.parkNanos(180 + state.random.nextInt(0, 40));
                    logger.info("Message 2");
                    LockSupport.parkNanos(180 + state.random.nextInt(0, 40));
                    logger.info("Message 3");
                    LockSupport.parkNanos(180 + state.random.nextInt(0, 40));
                    logger.info("Message 4");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        return System.currentTimeMillis();
    }


    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(20))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(50))
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
