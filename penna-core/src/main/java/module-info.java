import penna.core.slf4j.PennaServiceProvider;

module penna.core {
    uses penna.core.sink.NonStandardSink;
    // Depends explicitly on the SLF4J api
    requires org.slf4j;
    requires transitive penna.api;

    // Exposes a service provider for SLF4j
    provides org.slf4j.spi.SLF4JServiceProvider with PennaServiceProvider;


    // Penna subprojects also have access to minilog
    // when/if breaking apart from slf4j, expose the full logger
    exports penna.core.minilog to penna.config.yaml;
    exports penna.core.sink to penna.dev;
    exports penna.core.slf4j to penna.dev;
    exports penna.core.models to penna.dev;
}
