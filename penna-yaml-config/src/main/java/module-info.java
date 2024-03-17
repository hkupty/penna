import penna.config.yaml.YamlConfigManager;
import penna.config.yaml.YamlConfigProvider;

module penna.config.yaml {
    requires org.slf4j;
    requires transitive penna.api;
    requires penna.core;

    // (Optional) support for Jackson
    requires static com.fasterxml.jackson.databind;
    requires static com.fasterxml.jackson.dataformat.yaml;

    // (Optional) support for Snakeyaml
    requires static org.yaml.snakeyaml;
    requires static org.snakeyaml.engine.v2;

    provides penna.api.config.ConfigManager with YamlConfigManager;
    provides penna.api.configv2.Provider with YamlConfigProvider;
}
