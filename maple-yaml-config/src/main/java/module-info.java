import maple.config.yaml.YamlConfigManager;

module maple.config.yaml {
    requires org.slf4j;
    requires transitive maple.api;

    requires static com.fasterxml.jackson.databind;
    requires static com.fasterxml.jackson.dataformat.yaml;

    provides maple.api.config.ConfigManager with YamlConfigManager;

    exports maple.config.yaml;
}
