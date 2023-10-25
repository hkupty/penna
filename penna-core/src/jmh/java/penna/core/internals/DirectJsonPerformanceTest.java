package penna.core.internals;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DirectJsonPerformanceTest {

    static String longString = "Mussum Ipsum, cacilds vidis litro abertis. Em pé sem cair, deitado sem dormir, sentado sem cochilar e fazendo pose.Nullam volutpat risus nec leo commodo, ut interdum diam laoreet. Sed non consequat odio.Delegadis gente finis, bibendum egestas augue arcu ut est.Suco de cevadiss deixa as pessoas mais interessantis.";

    @State(Scope.Thread)
    public static class SimpleState {
        FileOutputStream fos;
        DirectJson dj;

        @Setup
        public void setup() {
            var devnull = new File("/dev/null");
            try {
                fos = new FileOutputStream(devnull);

                dj = new DirectJson(fos.getChannel());
                dj.checkSpace(longString.length() * 2 );
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Dev null doesn't exist!", e);
            }
        }

        @TearDown
        public void teardown() throws IOException {
            fos.close();
        }
    }



//    @Benchmark
//    @OutputTimeUnit(TimeUnit.NANOSECONDS)
//    public void writeUnsafe(SimpleState state) throws IOException {
//        state.dj.writeUnsafe(longString);
//        state.dj.flush();
//    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void writeNumber(SimpleState state) throws IOException {
        state.dj.writeNumber(1247);
        state.dj.flush();
    }

//    @Benchmark
//    @OutputTimeUnit(TimeUnit.NANOSECONDS)
//    public void writeRawFormatting(SimpleState state) throws IOException {
//        state.dj.writeRawFormatting(longString);
//        state.dj.flush();
//    }

    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.Throughput)
                .warmupTime(TimeValue.seconds(15))
                .warmupIterations(3)
                .threads(1)
                .measurementIterations(3)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .addProfiler("gc")
                .addProfiler("perfnorm")
                .addProfiler("perfasm", "tooBigThreshold=2100")
                .build();

        new Runner(options).run();
    }

}
