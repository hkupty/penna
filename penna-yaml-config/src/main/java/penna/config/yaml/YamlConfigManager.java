package penna.config.yaml;

import penna.api.config.ConfigManager;
import penna.api.config.Configurable;
import penna.config.yaml.impl.JacksonConfigManager;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class YamlConfigManager implements ConfigManager {
    ConfigManager impl;

    private static ConfigManager tryJackson(URL configFile) throws ClassNotFoundException {
        Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
        return new JacksonConfigManager(configFile);
    }

    public YamlConfigManager(){
        try {
            var url = Objects.requireNonNull(getClass().getClassLoader().getResource("penna.yaml"));
            impl = tryJackson(url);
        } catch (NullPointerException | ClassNotFoundException ignored) {
            impl = new ConfigManager() {
                private Configurable configurable;

                @Override
                public void bind(Configurable configurable) {
                    this.configurable = configurable;
                }

                @Override
                public void configure() { }

                @Override
                public void updateConfigs(ConfigItem... configItems) { configurable.configure(configItems); }
            };
        }
    }


    @Override
    public void bind(Configurable configurable) {
        impl.bind(configurable);
    }

    @Override
    public void configure() {
        impl.configure();
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        impl.updateConfigs(configItems);
    }
}
