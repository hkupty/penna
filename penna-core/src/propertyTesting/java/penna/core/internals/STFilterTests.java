package penna.core.internals;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;
import net.jqwik.api.statistics.Histogram;
import net.jqwik.api.statistics.Statistics;
import net.jqwik.api.statistics.StatisticsReport;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class STFilterTests {

    private double chiSquaredCriticalValue(int degreesOfFreedom, double significanceLevel) {
        // Use the Apache Commons Math library to compute the critical value
        ChiSquaredDistribution dist = new ChiSquaredDistribution(degreesOfFreedom);
        return dist.inverseCumulativeProbability(1.0 - significanceLevel);
    }

    @Provide
    public static Arbitrary<StackTraceElement> stackTraceElementArbitrary() {
        Arbitrary<String> classNameGen = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> methodNameGen = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> fileNameGen = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).map(s -> s + ".java");
        Arbitrary<Integer> lineNumberGen = Arbitraries.integers().between(1, 1000);

        return Combinators.combine(classNameGen, methodNameGen, fileNameGen, lineNumberGen)
                .as(Tuple::of)
                .filter(t -> !t.get3().isEmpty()) // filter out empty file names
                .map(t -> new StackTraceElement(t.get1(), t.get2(), t.get3(), t.get4()));
    }


    @Property(tries = 2500)
    // @StatisticsReport(format = Histogram.class)
    void lessThan1PercentCollisionRate(
            @ForAll @Size(min = 128, max = 1024) List<@From("stackTraceElementArbitrary") @UniqueElements StackTraceElement> stackTraces
    ) {
        StackTraceFilter filter = StackTraceFilter.create();
        Map<String, Integer> bucketCounts = new HashMap<>();
        int[] hashes = new int[StackTraceFilter.NUMBER_OF_HASHES];

        for (int i = 0; i < stackTraces.size(); i ++ ) {
            filter.hash(stackTraces.get(i), hashes);
            int fst = Math.min(hashes[0], hashes[1]);
            int snd = Math.max(hashes[0], hashes[1]);
            var key = fst + ":" + snd;
            bucketCounts.compute(key, (k, v) -> (v == null ? 0 : v) + 1);
            Statistics.label("collision").collect(filter.check(hashes));
            filter.mark(hashes);
        }

        int numBuckets = bucketCounts.size();
        double expectedFrequency = (double) numBuckets / stackTraces.size();

        double chiSquared = 0.0;
        for (int count : bucketCounts.values()) {
            double diff = count - expectedFrequency;
            chiSquared += diff * diff / expectedFrequency;
        }

        // Compute the degrees of freedom for the test
        int degreesOfFreedom = numBuckets - 1;

        double significanceLevel = 0.01;

        // Check if the chi-squared statistic exceeds the critical value
        double criticalValue = chiSquaredCriticalValue(degreesOfFreedom, significanceLevel);


        Assertions.assertTrue(chiSquared < criticalValue);

    }
}
