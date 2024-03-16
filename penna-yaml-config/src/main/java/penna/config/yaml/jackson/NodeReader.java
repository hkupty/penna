package penna.config.yaml.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import penna.api.config.ExceptionHandling;
import penna.config.yaml.models.ConfigNode;

import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class NodeReader {
    private NodeReader() {}

    private static String level(JsonNode node) {
        return node.get("level").asText();
    }

    private static List<String> fields(JsonNode node) {
        var iterator = node.get("fields").iterator();
        if (iterator.hasNext()) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL), false).map(JsonNode::asText).toList();
        } else {
            return List.of();
        }
    }

    private static ExceptionHandling exceptions(JsonNode node) throws IOException {
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

    public static ConfigNode deserialize(JsonNode node) throws IOException {
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

}
