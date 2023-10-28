package penna.dev.config;

import org.slf4j.event.Level;
import penna.api.config.ConfigManager.ConfigItem;
import penna.api.config.ConfigManager.ConfigItem.LoggerConfigItem;
import penna.api.config.ConfigManager.ConfigItem.RootConfigItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class PennaRuntimeConfigManager {
    private static final PennaRuntimeConfigManager instance = new PennaRuntimeConfigManager();
    private final List<WeakReference<DevConfigManager>> clients = new ArrayList<>();

    private PennaRuntimeConfigManager() {}

    public static PennaRuntimeConfigManager getInstance() {
        return instance;
    }

    static void register(DevConfigManager configManager) {
        instance.clients.add(new WeakReference<>(configManager));
    }

    private void update(ConfigItem... items) {
        clients.removeIf(ref -> ref.refersTo(null));
        clients.forEach(client ->{
            DevConfigManager manager = client.get();
            if(manager != null) {
                manager.updateConfigs(items);
            }
        });
    }

    void setLevel(Level level, String... loggers) {
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


    public void setTrace(String... loggers) { setLevel(Level.TRACE, loggers); }
    public void setDebug(String... loggers) { setLevel(Level.DEBUG, loggers); }
    public void setInfo(String... loggers) { setLevel(Level.INFO, loggers); }
    public void setWarn(String... loggers) { setLevel(Level.WARN, loggers); }
    public void setError(String... loggers) { setLevel(Level.ERROR, loggers); }

    public void reset() {
        update(StandardConfigs.DevOptimized);
    }

}
