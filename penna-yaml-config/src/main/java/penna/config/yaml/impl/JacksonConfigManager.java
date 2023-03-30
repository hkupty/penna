package penna.config.yaml.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import penna.api.config.Config;
import penna.api.config.ConfigManager;
import penna.api.config.Configurable;
import penna.api.models.LogField;
import org.slf4j.event.Level;
import penna.config.yaml.models.Node;
import penna.core.minilog.MiniLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class JacksonConfigManager implements ConfigManager {
    private static final LogField[] reference = new LogField[]{};
    private transient final ObjectMapper mapper;
    private transient final URL file;
    transient Configurable configurable;
    transient Node.RootNode config;

    public JacksonConfigManager(URL file) {
        this.mapper = new YAMLMapper();
        this.file = file;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private ConfigurationChange getUpdateFn(Node configNode) {
        var hasFields = (configNode.fields() != null) && (!configNode.fields().isEmpty());
        var hasLevel = configNode.level() != null;

        return ((Config base) -> base
                .replaceLevel(hasLevel ? Level.valueOf(configNode.level().toUpperCase(Locale.ENGLISH)) : base.level())
                .replaceFields(hasFields ? configNode.fields().stream().map(LogField::fromFieldName).filter(Objects::nonNull).toArray(size -> Arrays.copyOf(reference, size)) : base.fields()));
    }

    private ConfigItem getConfigItem(String logger, Node node){
        return new ConfigItem.LoggerConfigItem(logger, getUpdateFn(node));
    }

    private ConfigItem getConfigItem(Node node){
        return new ConfigItem.RootConfigItem(getUpdateFn(node));
    }

    private ConfigItem[] configItemsFromYaml(){
        ConfigItem[] root = new ConfigItem[] {
                getConfigItem(config)
        };
        ConfigItem[] child = config.loggers().keySet().stream()
                .map(key -> getConfigItem(key, config.loggers().get(key)))
                .toArray(size -> Arrays.copyOf(new ConfigItem[0], size));
        ConfigItem[] result = new ConfigItem[root.length + child.length];
        System.arraycopy(root, 0, result, 0, root.length);
        System.arraycopy(child, 0, result, root.length, child.length);

        return result;
    }

    public void read() throws IOException {
        config = mapper.readValue(file, Node.PennaConfig.class).penna();
    }

    @Override
    public void bind(Configurable configurable) {
        this.configurable = configurable;
    }

    @Override
    public void configure() {
        try {
            read();
            configurable.configure(configItemsFromYaml());
        } catch (IOException ioe) {
            MiniLogger.error("Unable to read configuration", ioe);
        }
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        configurable.configure(configItems);
    }
}

