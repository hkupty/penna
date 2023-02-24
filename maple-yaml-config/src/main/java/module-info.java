import maple.config.yaml.YamlConfigManager;

module maple.config.yaml {
    requires org.slf4j;
    requires maple.api;

    // For reading yamls with jackson we need both 'core' and 'dataformat.yaml'
    requires static com.fasterxml.jackson.core;
    requires static com.fasterxml.jackson.dataformat.yaml;

    provides maple.api.config.ConfigManager with YamlConfigManager;

}
