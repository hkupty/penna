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

    // (Optional) support for Snakeyaml Engine
    requires static org.snakeyaml.engine.v2;

    provides penna.api.configv2.Provider with YamlConfigProvider;
}
