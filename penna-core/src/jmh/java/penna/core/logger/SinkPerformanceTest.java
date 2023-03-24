package penna.core.logger;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.LoggerFactory;
import penna.api.config.Config;
import penna.core.sink.SinkImpl;
import penna.core.sink.impl.GsonPennaSink;
import penna.core.sink.impl.JacksonPennaSink;
import penna.core.sink.impl.JakartaPennaSink;
import penna.core.sink.impl.NativePennaSink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

public class SinkPerformanceTest {

    @State(Scope.Thread)
    public static class SinkState {
        private final File dumpToNull = new File("/dev/null");
        private FileOutputStream fos;
        TreeCache cache = new TreeCache(Config.getDefault());
        PennaLogger logger;
        @Param({"native", "jackson", "gson", "jakarta"})
        String sinkType;

        @Setup
        public void setup() throws IOException {
            fos = new FileOutputStream(dumpToNull);
            SinkImpl sink =  switch (sinkType) {
                case "native" -> new NativePennaSink();
                case "jackson" -> new JacksonPennaSink();
                case "gson" -> new GsonPennaSink();
                case "jakarta" -> new JakartaPennaSink();
                default -> throw new RuntimeException("wth?");
            };

            sink.init(fos.getChannel());
            logger = cache.getLoggerAt("jmh", "test", "penna");
            logger.sink.set(sink);
        }
        @TearDown
        public void tearDown() throws IOException {
            fos.close();
        }
    }

    @State(Scope.Thread)
    public static class SimpleState {
        TreeCache cache = new TreeCache(Config.getDefault());

        PennaLogger logger = cache.getLoggerAt("jmh", "test", "penna");
    }

//    @Benchmark
//    @OutputTimeUnit(TimeUnit.NANOSECONDS)
//    public void initALogger(Blackhole bh) throws IOException {
//        bh.consume(LoggerFactory.getLogger("com.pennacorp.lePennaApp.controller.TheGreatestController"));
//    }
//
//    @Benchmark
//    @OutputTimeUnit(TimeUnit.NANOSECONDS)
//    public void detectLevelIsNotEnabled(SimpleState state) throws IOException {
//        state.logger.trace("Should not log");
//    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void implementations(SinkState state) throws IOException {
        state.logger.atInfo().log("hello world");
    }


    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(15))
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
