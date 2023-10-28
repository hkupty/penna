package penna.dev.config;

import org.slf4j.event.Level;
import penna.api.config.ConfigManager;
import penna.api.config.ConfigManager.ConfigItem;
import penna.api.config.ConfigManager.ConfigItem.LoggerConfigItem;
import penna.api.config.ConfigManager.ConfigItem.RootConfigItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PennaRuntimeConfigManager {
    private static final PennaRuntimeConfigManager instance = new PennaRuntimeConfigManager();
    // TODO Check if it really needs to be a list
    private final List<WeakReference<DevConfigManager>> clients = new ArrayList<>();

    private PennaRuntimeConfigManager() {}

    static void register(DevConfigManager configManager) {
        instance.clients.add(new WeakReference<>(configManager));
    }

    private void update(ConfigItem... items) {
        clients.removeIf(ref -> ref.refersTo(null));
        clients.forEach(client ->{
            DevConfigManager manager;
            if((manager = client.get()) != null) {
                manager.updateConfigs(items);
            }
        });
    }

    void level(Level level, String... loggers) {
        if (loggers.length == 0) {
            update(new RootConfigItem(cfg -> cfg.replaceLevel(level)));
            return;
        }

        var configItems = new ConfigItem[loggers.length];

        for(int i = 0; i < loggers.length; i++) {
            configItems[i] = new LoggerConfigItem(loggers[i], cfg -> cfg.replaceLevel(level));
        }


        update(configItems);
    }


    public static void setLevel(Level level, String... loggers) { instance.level(level, loggers); }
    public static void setTrace(String... loggers) { setLevel(Level.TRACE, loggers); }
    public static void setDebug(String... loggers) { setLevel(Level.DEBUG, loggers); }
    public static void setInfo(String... loggers) { setLevel(Level.INFO, loggers); }
    public static void setWarn(String... loggers) { setLevel(Level.WARN, loggers); }
    public static void setError(String... loggers) { setLevel(Level.ERROR, loggers); }

    public static void reset() {
        instance.update(StandardConfigs.DevOptimized);
    }

}
