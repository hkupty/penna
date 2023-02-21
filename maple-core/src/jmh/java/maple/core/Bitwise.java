package maple.core;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

public class Bitwise {
//    @State(Scope.Thread)
//    public static class BenchmarkState {
//        @Param({
//                "16",
//                "128",
//                "1024"
//        })
//        int target;
//    }
//
//    @Fork(value = 1, warmups = 1)
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MICROSECONDS)
//    @Benchmark
//    public void bitwise(BenchmarkState state, Blackhole bh){
//        int counter = 0;
//        for (int i = 0; i < 2_000; i++) {
//            counter = (counter + 1) & (state.target - 1);
//        }
//        bh.consume(counter);
//    }
//
//    @Fork(value = 1, warmups = 1)
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MICROSECONDS)
//    @Benchmark
//    public void modulo(BenchmarkState state, Blackhole bh){
//        int counter = 0;
//        for (int i = 0; i < 2_000; i++) {
//            counter = (counter + 1) % state.target;
//        }
//        bh.consume(counter);
//    }
//
}
