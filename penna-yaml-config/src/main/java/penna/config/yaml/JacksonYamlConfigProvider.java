package penna.config.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.jetbrains.annotations.VisibleForTesting;
import penna.api.configv2.Manager;
import penna.api.configv2.Provider;
import penna.config.yaml.jackson.NodeReader;
import penna.config.yaml.models.ConfigMap;
import penna.config.yaml.models.ConfigNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JacksonYamlConfigProvider implements Provider {
    @VisibleForTesting
    transient final ObjectMapper mapper;
    private transient Path configPath;
    private transient Manager manager;

    public JacksonYamlConfigProvider() {
        this.mapper = new YAMLMapper();
    }

    /**
     * Tries to register the yaml config provider.
     * It will fail if the file is non-existing or malformed, thus returning false.
     *
     * @param manager The {@link Manager} instance;
     * @return whether the registration was successful.
     */
    @Override
    public boolean register(Manager manager) {
        try {
            var file = Objects.requireNonNull(Thread
                            .currentThread()
                            .getContextClassLoader()
                            .getResource("penna.yaml"))
                    .toURI();

            this.configPath = Path.of(file);
            this.manager = manager;

            refresh();

            // TODO add file watcher

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @VisibleForTesting
    ConfigMap readConfig(JsonNode root) {
        var configNodes = new HashMap<String, ConfigNode>();
        var cfg = root.get("config");
        var iterator = cfg.fields();
        while (iterator.hasNext()) {
            try {
                var entry = iterator.next();
                configNodes.put(entry.getKey(), NodeReader.deserialize(entry.getValue()));
            } catch (IOException e) {
                // TODO Handle malformed configuration
                continue;
            }
        }
        return new ConfigMap(Map.copyOf(configNodes));
    }

    /**
     * This functions reads the configuration from the yaml file and supplies it to the manager.
     * It should only be called <b>after</b> the manager has been installed through {@link JacksonYamlConfigProvider#register(Manager)}
     *
     * @throws IOException Due to yaml file reading, an exception can be thrown.
     */
    private void refresh() throws IOException {
        var tree = mapper.readTree(Files.newBufferedReader(this.configPath));
        var configMap = readConfig(tree);

        for (var entry : configMap.config().entrySet()) {
            var next = entry.getValue();
            manager.set(entry.getKey(), next::toConfig);
        }
    }

    @Override
    public void deregister() {
    }
}
