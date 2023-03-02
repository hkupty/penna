import maple.core.slf4j.MapleServiceProvider;

module maple.core {
    uses maple.api.config.ConfigManager;
    // Depends explicitly on the SLF4J api
    requires org.slf4j;
    requires maple.api;

    // Optionally depends on the following json libraries
    requires static com.fasterxml.jackson.core;
    requires static jakarta.json;
    requires static com.google.gson;

    // Exposes a service provider for SLF4j
    provides org.slf4j.spi.SLF4JServiceProvider with MapleServiceProvider;

    // The only user-facing namespace should be config.
    exports maple.core.config;
}
