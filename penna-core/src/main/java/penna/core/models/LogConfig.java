package penna.core.models;

import penna.api.config.Config;
import penna.api.models.LogField;
import penna.core.internals.PassThroughFilter;
import penna.core.internals.StackTraceBloomFilter;
import penna.core.internals.StackTraceFilter;

public record LogConfig(
        LogField[] fields,
        StackTraceFilter filter,
        int stacktraceDepth,
        int traverseDepth

) {

    public static LogConfig fromConfig(Config config) {
        return new LogConfig(
                config.fields(),
                config.exceptionHandling().deduplication() ? StackTraceBloomFilter.create() : new PassThroughFilter(),
                config.exceptionHandling().maxDepth(),
                config.exceptionHandling().traverseDepth()
        );
    }
}
