import penna.config.yaml.YamlConfigManager;

module penna.config.yaml {
    requires org.slf4j;
    requires transitive penna.api;

    requires static com.fasterxml.jackson.databind;
    requires static com.fasterxml.jackson.dataformat.yaml;
    requires penna.core;

    provides penna.api.config.ConfigManager with YamlConfigManager;

    exports penna.config.yaml;
}
