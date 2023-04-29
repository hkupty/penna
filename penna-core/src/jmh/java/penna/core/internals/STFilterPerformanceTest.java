package penna.core.internals;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class STFilterPerformanceTest {

    @State(Scope.Thread)
    public static class SimpleState {
        @Param({"16", "64", "128", "512"})
        int size;

        StackTraceElement[] stacktrace;

        Set<StackTraceElement> set = new HashSet<>();
        int[] hashes = new int[StackTraceBloomFilter.NUMBER_OF_HASHES];
        StackTraceFilter filter;
        Random random;

        @Setup
        public void setup() {
            filter = StackTraceBloomFilter.create();
            random = new Random();
            stacktrace = new StackTraceElement[size];
            for (int i = 0; i < size; i++ ) {
                stacktrace[i] = new StackTraceElement(
                        STFilterPerformanceTest.SimpleState.class.getName(),
                        "setup",
                        "STFilterPerformanceTest.java",
                        i);
            }
        }

    }



    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void markAndCheck(SimpleState state){
        StackTraceElement[] stackTrace = state.stacktrace;
        int[] hashes = state.hashes;
        for (int index = 0; index < stackTrace.length; index++) {
            state.filter.hash(hashes, stackTrace[index]);
            state.filter.mark(hashes);
        }

        for (int index = 0; index < stackTrace.length; index++) {
            state.filter.hash(hashes, stackTrace[state.random.nextInt(state.size)]);
            if(state.filter.check(hashes)) break;
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void compareWithHashSet(SimpleState state){
        StackTraceElement[] stackTrace = state.stacktrace;
        for (int index = 0; index < stackTrace.length; index++) {
            state.set.add(stackTrace[index]);
        }

        for (int index = 0; index < stackTrace.length; index++) {
            if(state.set.contains(stackTrace[state.random.nextInt(state.size)])) break;
        }
    }


    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(10))
                .warmupIterations(3)
                .threads(1)
                .measurementIterations(3)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }

}
