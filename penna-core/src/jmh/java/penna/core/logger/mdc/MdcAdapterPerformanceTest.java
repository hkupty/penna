package penna.core.logger.mdc;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.spi.MDCAdapter;
import penna.core.slf4j.PennaMDCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MdcAdapterPerformanceTest {

    @State(Scope.Thread)
    public static class MDCProxy {
        private final PennaMDCAdapter pennaMDCAdapter = new PennaMDCAdapter();
        private final LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();

        @Param({
                "penna",
                "logback"
        })
        String target;

        MDCAdapter adapter;

        @Setup
        public void setup() {
            adapter = switch (target) {
                case "penna" -> pennaMDCAdapter;
                case "logback" -> logbackMDCAdapter;
                default -> throw new IllegalStateException("Unexpected value: " + target);
            };
        }
    }

    @State(Scope.Benchmark)
    public static class MDCData {
        @Param({
//                "8",
//                "64",
                "256"
        })
        int size;

        private Random random = new Random(1337);

        Map<String, String> replacement;
        String[] keys;
        String[] values;
        int ixKv;
        String[] remove;
        int ixRem;
        String[] get;
        int ixGet;

        public int nextKv() {return ixKv++; }
        public int nextRem() {return ixRem++; }
        public int nextGet() {return ixGet++; }

        @Setup
        public void setup() {
            keys = new String[size];
            values = new String[size];
            remove = new String[size / 8];
            get = new String[size / 4];
            for (int i = 0; i < size; i++) {
                keys[i] = RandomStringUtils.randomAlphanumeric(2, 16);
                values[i] = RandomStringUtils.randomAlphanumeric(1, 64);
            }


            for(int i = 0; i < size / 4; i++) {
                if (random.nextBoolean()) {
                    get[i] = keys[random.nextInt((i + 1) * 4)];
                } else {
                    get[i] = RandomStringUtils.randomAlphanumeric(2, 18);
                }
            }

            for(int i = 0; i < size / 8; i++) {
                if (random.nextBoolean()) {
                    remove[i] = keys[random.nextInt((i + 1) * 8)];
                } else {
                    remove[i] = RandomStringUtils.randomAlphanumeric(2, 18);
                }
            }

            replacement = new HashMap<>();
            for (int i = 0; i < size * 0.83; i++) {
                replacement.put(
                        RandomStringUtils.randomAlphanumeric(2, 16),
                        RandomStringUtils.randomAlphanumeric(1, 64)
                );
            }
        }
    }


//    @Benchmark
//    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void mdcThreadsTest(MDCProxy adapter) throws IOException {
        MDCAdapter mdc = adapter.adapter;
        mdc.put("simple", "value");
        Thread thread = new Thread(() -> {
                mdc.put("simple", "replace");
            mdc.put("other", "value");
        });
        thread.start();

        var context = mdc.getCopyOfContextMap();
        Thread second = new Thread(() -> {
            mdc.clear();
            mdc.put("simple", "thing");
        });
        second.start();
        mdc.setContextMap(context);
    }

//    @Benchmark
//    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void sizedComplete(Blackhole bh, MDCProxy adapter, MDCData data) throws IOException {
        MDCAdapter mdc = adapter.adapter;
        mdc.clear();
        int kv;

        for (int i = 0; i < (data.size / 8); i++) {
            for (int j = 0; j < 8; j++) {
                kv = data.nextKv();
                mdc.put(data.keys[kv], data.values[kv]);
            }
            bh.consume(mdc.get(data.get[data.nextGet()]));
            bh.consume(mdc.get(data.get[data.nextGet()]));

            mdc.remove(data.remove[data.nextRem()]);
        }
        bh.consume(mdc.getCopyOfContextMap());
        mdc.setContextMap(data.replacement);
        data.ixRem = 0;
        data.ixKv = 0;
        data.ixGet = 0;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void recreateMdc(Blackhole bh, MDCProxy adapter, MDCData data) throws IOException {
        MDCAdapter mdc = adapter.adapter;
        mdc.clear();
        int kv;
        var ctx = mdc.getCopyOfContextMap();

        for (int i = 0; i < (data.size / 8); i++) {
            for (int j = 0; j < 8; j++) {
                kv = data.nextKv();
                mdc.put(data.keys[kv], data.values[kv]);
            }
            var prev = ctx;
            ctx = mdc.getCopyOfContextMap();
            mdc.setContextMap(prev);
        }
        data.ixRem = 0;
        data.ixKv = 0;
        data.ixGet = 0;
    }



    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(20))
                .warmupIterations(3)
                .threads(1)
                .measurementIterations(3)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .addProfiler("gc")
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .build();

        new Runner(options).run();
    }

}


/*
// 1st run
Benchmark                                                 (size)  (target)  Mode  Cnt       Score        Error   Units
MdcAdapterPerformanceTest.recreateMdc                        256     penna  avgt    3   49929.215 ±  89803.104   ns/op
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate          256     penna  avgt    3    1941.892 ±   3610.730  MB/sec
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate.norm     256     penna  avgt    3  100992.035 ±      0.064    B/op
MdcAdapterPerformanceTest.recreateMdc:gc.count               256     penna  avgt    3     400.000               counts
MdcAdapterPerformanceTest.recreateMdc:gc.time                256     penna  avgt    3     256.000                   ms
MdcAdapterPerformanceTest.recreateMdc                        256   logback  avgt    3  210140.260 ± 283560.778   ns/op
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate          256   logback  avgt    3    1423.210 ±   1937.524  MB/sec
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate.norm     256   logback  avgt    3  312464.146 ±      0.191    B/op
MdcAdapterPerformanceTest.recreateMdc:gc.count               256   logback  avgt    3     294.000               counts
MdcAdapterPerformanceTest.recreateMdc:gc.time                256   logback  avgt    3     177.000                   ms


// 2nd run, Map.copyOf(x) + set with new HashMap(ctx)
Benchmark                                                 (size)  (target)  Mode  Cnt       Score         Error   Units
MdcAdapterPerformanceTest.recreateMdc                        256     penna  avgt    3  113175.908 ±   23078.567   ns/op
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate          256     penna  avgt    3    1342.356 ±     272.373  MB/sec
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate.norm     256     penna  avgt    3  159296.079 ±       0.016    B/op
MdcAdapterPerformanceTest.recreateMdc:gc.count               256     penna  avgt    3     280.000                counts
MdcAdapterPerformanceTest.recreateMdc:gc.time                256     penna  avgt    3     155.000                    ms
MdcAdapterPerformanceTest.recreateMdc                        256   logback  avgt    3  326954.749 ± 1190715.666   ns/op
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate          256   logback  avgt    3     933.593 ±    3048.751  MB/sec
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate.norm     256   logback  avgt    3  312464.227 ±       0.827    B/op
MdcAdapterPerformanceTest.recreateMdc:gc.count               256   logback  avgt    3     199.000                counts
MdcAdapterPerformanceTest.recreateMdc:gc.time                256   logback  avgt    3     166.000                    ms

 */