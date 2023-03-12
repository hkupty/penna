import penna.core.slf4j.PennaServiceProvider;

module penna.core {
    uses penna.api.config.ConfigManager;
    // Depends explicitly on the SLF4J api
    requires org.slf4j;
    requires transitive penna.api;

    // Optionally depends on the following json libraries
    requires static com.fasterxml.jackson.core;
    requires static jakarta.json;
    requires static com.google.gson;

    // Exposes a service provider for SLF4j
    provides org.slf4j.spi.SLF4JServiceProvider with PennaServiceProvider;

    // The only user-facing namespace should be config.
    exports penna.core.config;
}
