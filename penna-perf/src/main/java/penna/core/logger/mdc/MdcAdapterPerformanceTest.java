package penna.core.logger.mdc;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.spi.MDCAdapter;
import penna.core.logger.utils.RunnerOptions;
import penna.core.slf4j.PennaMDCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MdcAdapterPerformanceTest {

    public enum Target {
        Penna,
        Logback
    }

    @State(Scope.Thread)
    public static class MDCProxy {
        private final PennaMDCAdapter pennaMDCAdapter = new PennaMDCAdapter();
        private final LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();

        @Param({"Penna"})
        Target target;

        MDCAdapter adapter;

        @Setup
        public void setup() {
            adapter = switch (target) {
                case Penna -> pennaMDCAdapter;
                case Logback -> logbackMDCAdapter;
            };
        }

        @TearDown(Level.Invocation)
        public void teardown() {
            adapter.clear();
        }
    }

    @State(Scope.Benchmark)
    public static class MDCData {
        @Param({
                "8",
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

        public int nextKv() {
            return ixKv++;
        }

        public int nextRem() {
            return ixRem++;
        }

        public int nextGet() {
            return ixGet++;
        }

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


            for (int i = 0; i < size / 4; i++) {
                if (random.nextBoolean()) {
                    get[i] = keys[random.nextInt((i + 1) * 4)];
                } else {
                    get[i] = RandomStringUtils.randomAlphanumeric(2, 18);
                }
            }

            for (int i = 0; i < size / 8; i++) {
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

        @TearDown(Level.Invocation)
        public void teardown() {
            ixRem = 0;
            ixKv = 0;
            ixGet = 0;
        }
    }

    @Benchmark
    public void recreateMdc(Blackhole bh, MDCProxy adapter, MDCData data) throws IOException {
        MDCAdapter mdc = adapter.adapter;
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
    }

    public static void main(String[] args) throws Exception {
        var options = RunnerOptions
                .averageTime(MdcAdapterPerformanceTest.class.getName() + ".*")
                .warmupIterations(2)
                .warmupTime(TimeValue.seconds(20))
                .addProfiler("gc")
                .addProfiler("perfnorm")
                .addProfiler("perfasm")
                .addProfiler("jfr")
                .build();
        new Runner(options).run();
    }

}


/*
// With TreeMap
Benchmark                                                      (size)  (target)  Mode  Cnt       Score      Error      Units
MdcAdapterPerformanceTest.recreateMdc                               8     Penna  avgt    3       0.278 ±    0.012      us/op
MdcAdapterPerformanceTest.recreateMdc:CPI                           8     Penna  avgt            0.309             clks/insn
MdcAdapterPerformanceTest.recreateMdc:IPC                           8     Penna  avgt            3.241             insns/clk
MdcAdapterPerformanceTest.recreateMdc:L1-dcache-load-misses:u       8     Penna  avgt           14.115                  #/op
MdcAdapterPerformanceTest.recreateMdc:L1-dcache-loads:u             8     Penna  avgt         1078.988                  #/op
MdcAdapterPerformanceTest.recreateMdc:L1-dcache-stores:u            8     Penna  avgt          533.286                  #/op
MdcAdapterPerformanceTest.recreateMdc:L1-icache-load-misses:u       8     Penna  avgt            1.513                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-load-misses:u             8     Penna  avgt            0.026                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-loads:u                   8     Penna  avgt            0.270                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-store-misses:u            8     Penna  avgt            0.148                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-stores:u                  8     Penna  avgt            0.255                  #/op
MdcAdapterPerformanceTest.recreateMdc:asm                           8     Penna  avgt              NaN                   ---
MdcAdapterPerformanceTest.recreateMdc:branch-misses:u               8     Penna  avgt            0.395                  #/op
MdcAdapterPerformanceTest.recreateMdc:branches:u                    8     Penna  avgt          695.286                  #/op
MdcAdapterPerformanceTest.recreateMdc:cycles:u                      8     Penna  avgt         1222.893                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-load-misses:u            8     Penna  avgt            0.055                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-loads:u                  8     Penna  avgt         1085.475                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-store-misses:u           8     Penna  avgt            0.096                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-stores:u                 8     Penna  avgt          536.295                  #/op
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate                 8     Penna  avgt    3    2316.134 ± 1610.439     MB/sec
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate.norm            8     Penna  avgt    3     816.084 ±    2.356       B/op
MdcAdapterPerformanceTest.recreateMdc:gc.count                      8     Penna  avgt    3    1330.000                counts
MdcAdapterPerformanceTest.recreateMdc:gc.time                       8     Penna  avgt    3     534.000                    ms
MdcAdapterPerformanceTest.recreateMdc:iTLB-load-misses:u            8     Penna  avgt            0.032                  #/op
MdcAdapterPerformanceTest.recreateMdc:instructions:u                8     Penna  avgt         3962.827                  #/op
MdcAdapterPerformanceTest.recreateMdc:jfr                           8     Penna  avgt              NaN                   ---
MdcAdapterPerformanceTest.recreateMdc                             256     Penna  avgt    3      29.351 ±    5.543      us/op
MdcAdapterPerformanceTest.recreateMdc:CPI                         256     Penna  avgt            0.245             clks/insn
MdcAdapterPerformanceTest.recreateMdc:IPC                         256     Penna  avgt            4.085             insns/clk
MdcAdapterPerformanceTest.recreateMdc:L1-dcache-load-misses:u     256     Penna  avgt         2620.078                  #/op
MdcAdapterPerformanceTest.recreateMdc:L1-dcache-loads:u           256     Penna  avgt       113747.530                  #/op
MdcAdapterPerformanceTest.recreateMdc:L1-dcache-stores:u          256     Penna  avgt        62283.020                  #/op
MdcAdapterPerformanceTest.recreateMdc:L1-icache-load-misses:u     256     Penna  avgt          138.940                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-load-misses:u           256     Penna  avgt            2.658                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-loads:u                 256     Penna  avgt           29.724                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-store-misses:u          256     Penna  avgt           74.369                  #/op
MdcAdapterPerformanceTest.recreateMdc:LLC-stores:u                256     Penna  avgt           84.087                  #/op
MdcAdapterPerformanceTest.recreateMdc:asm                         256     Penna  avgt              NaN                   ---
MdcAdapterPerformanceTest.recreateMdc:branch-misses:u             256     Penna  avgt           55.235                  #/op
MdcAdapterPerformanceTest.recreateMdc:branches:u                  256     Penna  avgt        76694.591                  #/op
MdcAdapterPerformanceTest.recreateMdc:cycles:u                    256     Penna  avgt       107139.814                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-load-misses:u          256     Penna  avgt            4.118                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-loads:u                256     Penna  avgt       113875.370                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-store-misses:u         256     Penna  avgt            0.617                  #/op
MdcAdapterPerformanceTest.recreateMdc:dTLB-stores:u               256     Penna  avgt        62479.368                  #/op
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate               256     Penna  avgt    3    3132.054 ± 4338.725     MB/sec
MdcAdapterPerformanceTest.recreateMdc:gc.alloc.rate.norm          256     Penna  avgt    3  100999.667 ±  215.457       B/op
MdcAdapterPerformanceTest.recreateMdc:gc.count                    256     Penna  avgt    3    1215.000                counts
MdcAdapterPerformanceTest.recreateMdc:gc.time                     256     Penna  avgt    3     527.000                    ms
MdcAdapterPerformanceTest.recreateMdc:iTLB-load-misses:u          256     Penna  avgt            2.542                  #/op
MdcAdapterPerformanceTest.recreateMdc:instructions:u              256     Penna  avgt       437681.585                  #/op
MdcAdapterPerformanceTest.recreateMdc:jfr                         256     Penna  avgt              NaN                   ---

 */