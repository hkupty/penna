package penna.config.yaml.models;

import java.util.List;
import java.util.Map;

public sealed interface Node {
    String level();
    List<String> fields();

    Exceptions exceptions();

    sealed interface Exceptions {

        boolean deduplicate();
        int maxDepth();

        int traverseDepth();

        record ExceptionsConfig(
                boolean deduplicate,
                int maxDepth,
                int traverseDepth
        ) implements Exceptions {}
    }

    record YamlConfigNode(
            String level,
            List<String> fields,

            Exceptions exceptions

    ) implements Node {}

   record RootNode(
            String level,
            List<String> fields,
            Exceptions exceptions,
            Map<String, YamlConfigNode> loggers
    ) implements Node {}

    record PennaConfig(
            RootNode penna
    ){}
}
