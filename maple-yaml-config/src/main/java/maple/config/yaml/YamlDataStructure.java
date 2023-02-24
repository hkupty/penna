package maple.config.yaml;

import maple.api.config.Config;
import maple.api.config.ConfigManager;
import maple.api.models.LogField;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class YamlDataStructure {
    public String name;
    public String level;
    public List<String> logFields = new ArrayList<>();
    public List<YamlDataStructure> childLoggers = new ArrayList<>();

    public YamlDataStructure child(String partialName) {
        String finalName;
        if (name != null) {
            finalName = String.join(".", name, partialName);
        } else {
            finalName = partialName;
        }

        var childDataStructure = new YamlDataStructure();
        childDataStructure.name = finalName;
        childDataStructure.level = level;
        childDataStructure.logFields.addAll(logFields);
        return childDataStructure;
    }

    @Override
    public String toString() {
        return "YamlDataStructure{"
                + "\nname = " + name
                + "\nlevel = " + level
                + "\nlogFields = " + logFields.toString()
                + "\nchild = " + childLoggers.toString() + "}";
    }

    private Stream<ConfigManager.ConfigItem> asConfigItemStream() {
        ConfigManager.ConfigItem cfg;
        Config config = new Config(
                level != null ? Level.valueOf(level.toUpperCase()) : null,
                logFields.stream()
                        .map(str -> {
                            var buffer = new StringBuilder(str);
                            buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));

                            return buffer.toString();
                        })
                        .map(LogField::valueOf)
                        .toList().toArray(LogField[]::new));
        if (name == null) {
            cfg = new ConfigManager.ConfigItem.RootConfig(config);
        } else {
            cfg = new ConfigManager.ConfigItem.LoggerConfig(name, config);
        }

        return Stream.concat(Stream.of(cfg), childLoggers.stream().flatMap(YamlDataStructure::asConfigItemStream));
    }

    public List<ConfigManager.ConfigItem> asConfigItem() {
        return asConfigItemStream().toList();
    }
}