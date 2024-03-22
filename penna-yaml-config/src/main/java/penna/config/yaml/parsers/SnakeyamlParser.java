package penna.config.yaml.parsers;

import org.yaml.snakeyaml.Yaml;
import penna.api.models.ExceptionHandling;
import penna.config.yaml.models.ConfigMap;
import penna.config.yaml.models.ConfigNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SnakeyamlParser implements Parser {

    private String level(Map<String, Object> data) {
        return (String) data.get("level");
    }

    @SuppressWarnings({"unchecked"})
    private List<String> fields(Map<String, Object> data) {
        try {
            return (List<String>) data.getOrDefault("fields", List.of());
        } catch (ClassCastException ignored) {
            return List.of();
        }
    }

    @SuppressWarnings({"unchecked"})
    private ExceptionHandling exceptions(Map<String, Object> data) {
        var base = ExceptionHandling.getDefault();
        try {
            Map<String, Object> next = (Map<String, Object>) data.get("exception");

            Boolean dedup;
            Integer maxDepth;
            Integer traverseDepth;

            if ((dedup = (Boolean) next.get("deduplication")) != null) {
                base = base.replaceDeduplication(dedup);
            }

            if ((maxDepth = (Integer) next.get("maxDepth")) != null) {
                base = base.replaceMaxDepth(maxDepth);
            }

            if ((traverseDepth = (Integer) next.get("traverseDepth")) != null) {
                base = base.replaceTraverseDepth(traverseDepth);
            }
        } catch (ClassCastException ignored) {}

        return base;
    }

    public ConfigNode deserialize(Map<String, Object> node) throws IOException {
        // TODO recurse into the object, produce multiple objects
        var hasLevel = node.containsKey("level");
        var hasFields = node.containsKey("fields");
        var hasException = node.containsKey("exception");


        if (hasLevel && hasFields && hasException) {
            return new ConfigNode.CompleteConfig(level(node), fields(node), exceptions(node));
        } else if (hasLevel && hasFields) {
            return new ConfigNode.LevelAndFields(level(node), fields(node));
        } else if (hasLevel && hasException) {
            return new ConfigNode.LevelAndExceptions(level(node), exceptions(node));
        } else if (hasFields && hasException) {
            return new ConfigNode.FieldsAndException(fields(node), exceptions(node));
        } else if (hasLevel) {
            return new ConfigNode.OnlyLevel(level(node));
        } else if (hasFields) {
            return new ConfigNode.OnlyFields(fields(node));
        } else if (hasException) {
            return new ConfigNode.OnlyExceptions(exceptions(node));
        } else {
            return null;
        }
    }


    @Override
    @SuppressWarnings({"unchecked"})
    public ConfigMap readAndParse(Path file) throws IOException {
        var yaml = new Yaml();
        // <logger> -> <properties>
        Map<String, Object> data = yaml.load(Files.newBufferedReader(file));

        Boolean watch = (Boolean) data.get("watch");
        Map<String, Map<String, Object>> loggerConfigs = (Map<String, Map<String, Object>>) data.get("loggers");
        var result = new HashMap<String, ConfigNode>();

        loggerConfigs.forEach((logger, config) -> {
            try {
                var parsed = deserialize(config);
                if (parsed != null) {
                    result.put(logger, parsed);
                }
            } catch (IOException ignored) {}
        });

        return new ConfigMap(Map.copyOf(result), watch);
    }
}
