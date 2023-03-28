package penna.config.yaml.models;

import java.util.List;
import java.util.Map;

public sealed interface Node {
    String level();
    List<String> fields();

    record YamlConfigNode(
            String level,
            List<String> fields
    ) implements Node {}

   record RootNode(
            String level,
            List<String> fields,
            Map<String, YamlConfigNode> loggers
    ) implements Node {}

    record PennaConfig(
            RootNode penna
    ){}
}
