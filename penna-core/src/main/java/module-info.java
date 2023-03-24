import penna.core.slf4j.PennaServiceProvider;

module penna.core {
    uses penna.api.config.ConfigManager;
    // Depends explicitly on the SLF4J api
    requires org.slf4j;
    requires transitive penna.api;

    // Exposes a service provider for SLF4j
    provides org.slf4j.spi.SLF4JServiceProvider with PennaServiceProvider;

    // The only user-facing namespace should be config.
    exports penna.core.config;
}
