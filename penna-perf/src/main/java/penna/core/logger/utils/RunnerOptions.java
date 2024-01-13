package penna.core.logger.utils;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

public class RunnerOptions {
    public static ChainedOptionsBuilder throughput(String scope) {
        return new OptionsBuilder()
                .include(scope)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(30))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .threads(1)
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .jvmArgs("-Xmx8192m");
    }

    public static ChainedOptionsBuilder averageTime(String scope) {
        return new OptionsBuilder()
                .include(scope)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(30))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .threads(1)
                .jvm("/usr/lib/jvm/java-21-jetbrains/bin/java")
                .jvmArgs("-Xmx8192m");
    }
}
