package penna.config.yaml;

import penna.api.configv2.Manager;
import penna.api.configv2.Provider;
import penna.config.yaml.parser.Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Implementation of the {@link Provider} interface that extends the {@link Manager}
 * by adding configuration from a yaml file source.
 */
public class YamlConfigProvider implements Provider {
    private transient Path configPath;
    private transient Manager manager;
    private transient final Parser parser;

    public YamlConfigProvider() {
        this.parser = Parser.Factory.getParser();
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

    /**
     * This functions reads the configuration from the yaml file and supplies it to the manager.
     * It should only be called <b>after</b> the manager has been installed through {@link YamlConfigProvider#register(Manager)}
     *
     * @throws IOException Due to yaml file reading, an exception can be thrown.
     */
    private void refresh() throws IOException {
        var configMap = parser.readAndParse(this.configPath);

        for (var entry : configMap.config().entrySet()) {
            var next = entry.getValue();
            manager.set(entry.getKey(), next::toConfig);
        }
    }

    @Override
    public void deregister() {
    }
}
