import penna.api.configv2.Provider;

/**
 * Penna API is a thin set of classes and records that are common-ground between the {@code penna.core} project
 * and satellite projects or user customization.
 * <br />
 * For example, in order to customize log level, a user might need to pass in a {@link penna.api.config.Config} through
 * the {@link penna.api.config.ConfigManager} interface.
 * <br /><br />
 * Advanced users might also implement a {@link penna.api.config.ConfigManager} themselves, so they have better runtime
 * control over log level and fields, for example, fine-tuning the amount of logged information based on metrics or usage
 * volume. For that, one can have a monitor thread collecting instrumentation data interacting directly with the
 * {@link penna.api.config.Configurable} instance.
 */
module penna.api {
    uses Provider;
    requires transitive org.slf4j;
    requires transitive org.jetbrains.annotations;

    exports penna.api.models;
    exports penna.api.config;
    exports penna.api.configv2;
}