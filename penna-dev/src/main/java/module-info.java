
module penna.dev {
    uses penna.api.config.ConfigManager;
    requires penna.core;
    requires org.slf4j;
    requires JColor;
    provides penna.core.sink.NonStandardSink with penna.dev.sink.DevRuntimeSink;
}