import maple.core.slf4j.MapleServiceProvider;

module maple.core {
    // Depends explicitly on the SLF4J api
    requires org.slf4j;
    requires maple.api;

    // Optionally depends on the following json libraries
    requires static com.fasterxml.jackson.core;
    requires static jakarta.json;

    // Exposes a service provider for SLF4j
    provides org.slf4j.spi.SLF4JServiceProvider with MapleServiceProvider;

            // Only allows users to "see" maple through the *.slf4j package
    exports maple.core.slf4j;
}
