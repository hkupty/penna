/**
 * Maple API is a thin set of classes and records that are common-ground between the {@code maple.core} project
 * and satellite projects or user customization.
 * <br />
 * For example, in order to customize log level, a user might need to pass in a {@link maple.api.config.Config} through
 * the {@link maple.api.config.ConfigManager} interface.
 * <br /><br />
 * Advanced users might also implement a {@link maple.api.config.ConfigManager} themselves, so they have better runtime
 * control over log level and fields, for example, fine-tuning the amount of logged information based on metrics or usage
 * volume. For that, one can have a monitor thread collecting instrumentation data interacting directly with the
 * {@link maple.api.config.Configurable} instance.
 */
module maple.api {
    requires org.slf4j;

    exports maple.api.models;
    exports maple.api.config;
}