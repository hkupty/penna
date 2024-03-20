import penna.api.config.Provider;

/**
 * Penna API is a thin set of classes and records that are common-ground between the {@code penna.core} project
 * and satellite projects or user customization.
 * <br />
 * <br /><br />
 */
module penna.api {
    uses Provider;
    requires transitive org.slf4j;
    requires transitive org.jetbrains.annotations;

    exports penna.api.models;
    exports penna.api.config;
}