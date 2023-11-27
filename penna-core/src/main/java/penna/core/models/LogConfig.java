package penna.core.models;

import penna.api.config.Config;
import penna.api.models.LogField;
import penna.core.internals.StackTraceFilter;

public final class LogConfig {
    public LogField[] fields;
    public StackTraceFilter filter;
    public int stacktraceDepth;
    public int traverseDepth;

    private boolean deduplicate;

    public static LogConfig fromConfig(Config config) {
        var cfg = new LogConfig();
        cfg.deduplicate = config.exceptionHandling().deduplication();
        if (cfg.deduplicate) {
            cfg.filter = StackTraceFilter.Shared.getBloomFilter();
        }
        cfg.update(config);

        return cfg;
    }


    public void update(Config config) {
        this.fields = config.fields();
        this.stacktraceDepth = config.exceptionHandling().maxDepth();
        this.traverseDepth = config.exceptionHandling().traverseDepth();
        if (config.exceptionHandling().deduplication() != this.deduplicate) {
            if (!config.exceptionHandling().deduplication()) {
                this.filter = StackTraceFilter.Shared.getPassThroughFilter();
            } else {
                this.filter = StackTraceFilter.Shared.getBloomFilter();
            }
            this.deduplicate = config.exceptionHandling().deduplication();
        }
    }
}

