package penna.core.models;

import penna.api.config.Config;
import penna.api.models.LogField;
import penna.core.internals.PassThroughFilter;
import penna.core.internals.StackTraceFilter;

public final class LogConfig {
    public LogField[] fields;
    public StackTraceFilter filter;
    public int stacktraceDepth;
    public int traverseDepth;

    public static LogConfig fromConfig(Config config) {
        var cfg = new LogConfig();
        cfg.update(config);

        return cfg;
    }


    public void update(Config config) {
        this.fields = config.fields();
        this.stacktraceDepth = config.exceptionHandling().maxDepth();
        this.traverseDepth = config.exceptionHandling().traverseDepth();
        if (!config.exceptionHandling().deduplication()) {
            this.filter = StackTraceFilter.Shared.getPassThroughFilter();
        } else if (this.filter instanceof PassThroughFilter) {
            this.filter = StackTraceFilter.Shared.getBloomFilter();
        }
    }
}

