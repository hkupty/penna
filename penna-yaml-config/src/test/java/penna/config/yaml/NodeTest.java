package penna.config.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import penna.api.config.Config;
import penna.api.config.ExceptionHandling;
import penna.api.configv2.ConfigToLogger;
import penna.api.configv2.Manager;
import penna.api.configv2.Storage;
import penna.config.yaml.models.ConfigMap;
import penna.config.yaml.models.ConfigNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NodeTest {

    @Test
    void CanReadSimpleYaml() throws JsonProcessingException {
        var provider = new JacksonYamlConfigProvider();

        var config = """
                ---
                config:
                    penna:
                        level: debug
                """;

        var tree = provider.mapper.readTree(config);
        var result = provider.readConfig(tree);

        var reference = new ConfigMap(Map.of("penna", new ConfigNode.OnlyLevel("debug")));
        Assertions.assertEquals(reference, result);
    }

    @Test
    void CanReadMultipleFields() throws JsonProcessingException {
        var provider = new JacksonYamlConfigProvider();

        var config = """
                ---
                config:
                    penna:
                        level: debug
                        fields:
                            - timestamp
                            - message
                            - thread
                """;

        var tree = provider.mapper.readTree(config);
        var result = provider.readConfig(tree);

        var reference = new ConfigMap(Map.of("penna", new ConfigNode.LevelAndFields("debug", List.of(
                "timestamp",
                "message",
                "thread"
        ))));
        Assertions.assertEquals(reference, result);
    }


    @Test
    void CanReadMultipleFields2() throws JsonProcessingException {
        var provider = new JacksonYamlConfigProvider();
        var config = """
                ---
                config:
                    "": { level: error }
                    penna: { level: debug }
                    penna.config.yaml: { level: trace, fields: [message] }
                    com.other: { level: warn }
                """;

        var tree = provider.mapper.readTree(config);
        var result = provider.readConfig(tree);

        var reference = new ConfigMap(
                Map.of("", new ConfigNode.OnlyLevel("error"),
                        "penna", new ConfigNode.OnlyLevel("debug"),
                        "penna.config.yaml", new ConfigNode.LevelAndFields("trace", List.of("message")),
                        "com.other", new ConfigNode.OnlyLevel("warn")
                ));

        Assertions.assertEquals(reference, result);
    }

    @Test
    void CanReadExceptionConfigs() throws JsonProcessingException {
        var provider = new JacksonYamlConfigProvider();
        var config = """
                ---
                config:
                    penna:
                        level: debug
                        exception:
                            deduplication: true
                            maxDepth: 128
                            traverseDepth: 3
                """;

        var tree = provider.mapper.readTree(config);
        var result = provider.readConfig(tree);

        var reference = new ConfigMap(
                Map.of("penna",
                        new ConfigNode.LevelAndExceptions(
                                "debug",
                                new ExceptionHandling(128, 3, true)
                        )
                ));

        Assertions.assertEquals(reference, result);
    }

    class DummyStorage implements Storage {
        HashMap<String, Config> simpleStorage = new HashMap<>();

        @Override
        public void apply(ConfigToLogger... configs) {
            for (var config : configs) {
                if (config instanceof ConfigToLogger.RootLoggerConfigItem rootLoggerConfigItem) {
                    simpleStorage.put("", rootLoggerConfigItem.config());
                } else if (config instanceof ConfigToLogger.NamedLoggerConfigItem namedLoggerConfigItem) {
                    simpleStorage.put(namedLoggerConfigItem.logger(), namedLoggerConfigItem.config());
                }
            }
        }

        @Override
        public Config get(String logger) {
            return simpleStorage.get(logger);
        }
    }

    @Test
    void fullConfigTest() {
        var dummy = new DummyStorage();
        var impl = Manager.Factory.initialize(dummy);

        System.out.println(impl);
    }

}