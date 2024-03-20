import penna.core.slf4j.PennaServiceProvider;

module penna.core {
    // Depends explicitly on the SLF4J api
    requires org.slf4j;
    requires transitive penna.api;

    // Exposes a service provider for SLF4j
    provides org.slf4j.spi.SLF4JServiceProvider with PennaServiceProvider;
}
