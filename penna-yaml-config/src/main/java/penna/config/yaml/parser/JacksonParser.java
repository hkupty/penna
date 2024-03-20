package penna.config.yaml.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.jetbrains.annotations.VisibleForTesting;
import penna.api.config.ExceptionHandling;
import penna.config.yaml.models.ConfigMap;
import penna.config.yaml.models.ConfigNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

public final class JacksonParser implements Parser {
    @VisibleForTesting
    transient final ObjectMapper mapper;

    public JacksonParser() {this.mapper = new YAMLMapper();}

    private String level(JsonNode node) {
        return node.get("level").asText();
    }

    private List<String> fields(JsonNode node) {
        var iterator = node.get("fields").iterator();
        if (iterator.hasNext()) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL), false).map(JsonNode::asText).toList();
        } else {
            return List.of();
        }
    }

    private ExceptionHandling exceptions(JsonNode node) throws IOException {
        var next = node.get("exception");

        var base = ExceptionHandling.getDefault();
        if (next.has("deduplication")) {
            base = base.replaceDeduplication(next.get("deduplication").asBoolean());
        }

        if (next.has("maxDepth")) {
            base = base.replaceMaxDepth(next.get("maxDepth").asInt());
        }

        if (next.has("traverseDepth")) {
            base = base.replaceTraverseDepth(next.get("traverseDepth").asInt());
        }

        return base;
    }

    public ConfigNode deserialize(JsonNode node) throws IOException {
        // TODO recurse into the object, produce multiple objects
        var hasLevel = node.hasNonNull("level");
        var hasFields = node.hasNonNull("fields");
        var hasException = node.hasNonNull("exception");


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

    @VisibleForTesting
    ConfigMap readConfig(JsonNode root) {
        var configNodes = new HashMap<String, ConfigNode>();
        var watch = root.get("watch").asBoolean();
        var cfg = root.get("loggers");
        var iterator = cfg.fields();
        while (iterator.hasNext()) {
            try {
                var entry = iterator.next();
                configNodes.put(entry.getKey(), deserialize(entry.getValue()));
            } catch (IOException e) {
                // TODO Handle malformed configuration
                continue;
            }
        }
        return new ConfigMap(Map.copyOf(configNodes), watch);
    }

    @Override
    public ConfigMap readAndParse(Path path) throws IOException {
        var tree = mapper.readTree(Files.newBufferedReader(path));
        return readConfig(tree);
    }
}
