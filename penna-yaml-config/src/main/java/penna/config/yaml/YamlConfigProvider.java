package penna.config.yaml;

import penna.api.config.Manager;
import penna.api.config.Provider;
import penna.config.yaml.parsers.Parser;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static penna.api.audit.Logger.report;
import static penna.api.audit.Logger.reportError;

/**
 * Implementation of the {@link Provider} interface that extends the {@link Manager}
 * by adding configuration from a yaml file source.
 */
public class YamlConfigProvider implements Provider {
    private transient Path configPath;
    private transient Manager manager;
    private transient final Parser parser;
    private transient final AtomicBoolean keepRunning = new AtomicBoolean(true);


    private static final List<String> TARGET_FILES = List.of(
            "penna-test.yaml",
            "penna-test.yml",
            "penna.yaml",
            "penna.yml"
    );

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
            var classLoader = Thread.currentThread().getContextClassLoader();

            var maybeFile = TARGET_FILES.stream().map(classLoader::getResource).dropWhile(Objects::isNull).findFirst();

            if (maybeFile.isEmpty()) {
                return false;
            }

            this.configPath = Path.of(maybeFile.get().toURI());
            this.manager = manager;

            var configMap = parser.readAndParse(this.configPath);
            if (configMap.watch()) {
                startWorker();
            }
            return true;
        } catch (Exception ignored) {
            report("INFO", "yaml config not registered");
            return false;
        }
    }

    private void startWorker() {
        Thread.ofVirtual().name("penna-yaml-config-file-watcher").start(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                var target = configPath.getFileName();
                configPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                WatchKey key;
                while (keepRunning.get() && (key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().equals(target)) {
                            refresh();
                        }
                    }
                    key.reset();
                }
            } catch (Exception exception) {
                reportError("WARN", "yaml config file watcher got error, closing", exception);
            }
        });
    }

    /**
     * This functions reads the configuration from the yaml file and supplies it to the manager.
     * It should only be called <b>after</b> the manager has been installed through {@link YamlConfigProvider#register(Manager)}
     *
     * @throws IOException Due to yaml file reading, an exception can be thrown.
     */
    private void refresh() throws IOException {
        var configMap = parser.readAndParse(this.configPath);

        // Setting to false effectively shuts down the watcher if it was running.
        keepRunning.set(configMap.watch());

        for (var entry : configMap.loggers().entrySet()) {
            var next = entry.getValue();
            manager.set(entry.getKey(), next::toConfig);
        }
    }

    @Override
    public void init() {
        try {
            refresh();
        } catch (Exception exception) {
            reportError("WARN", "yaml config init got error", exception);
        }
    }
}
