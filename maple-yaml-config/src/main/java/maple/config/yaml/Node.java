package maple.config.yaml;

import java.util.List;
import java.util.Map;

sealed interface Node {
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

    record MapleConfig(
            RootNode maple
    ){}
}
