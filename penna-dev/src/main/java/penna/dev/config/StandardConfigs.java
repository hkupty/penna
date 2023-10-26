package penna.dev.config;

import org.slf4j.event.Level;
import penna.api.config.Config;

import static penna.api.config.ConfigManager.ConfigItem;

public final class StandardConfigs {
    private StandardConfigs() {}
    public static ConfigItem DevOptimized = new ConfigItem.RootConfigItem(config -> new Config(Level.TRACE, config.fields(), config.exceptionHandling()));
}
