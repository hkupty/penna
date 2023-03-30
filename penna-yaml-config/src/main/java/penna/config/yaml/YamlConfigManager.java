package penna.config.yaml;

import penna.api.config.ConfigManager;
import penna.api.config.Configurable;
import penna.config.yaml.impl.JacksonConfigManager;

import java.net.URL;
import java.util.Objects;


public class YamlConfigManager implements ConfigManager {
    transient ConfigManager impl;

    private static ConfigManager tryJackson(URL configFile) throws ClassNotFoundException {
        Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
        return new JacksonConfigManager(configFile);
    }

    public YamlConfigManager(){
        try {
            var url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("penna.yaml"));
            impl = tryJackson(url);
        } catch (ClassNotFoundException ignored) {
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
