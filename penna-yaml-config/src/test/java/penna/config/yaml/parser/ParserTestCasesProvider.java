package penna.config.yaml.parser;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.jupiter.cartesian.CartesianParameterArgumentsProvider;
import penna.api.config.ExceptionHandling;
import penna.config.yaml.models.ConfigMap;
import penna.config.yaml.models.ConfigNode;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ParserTestCasesProvider implements CartesianParameterArgumentsProvider<ParserTestCasesProvider.TestData> {

    private static final List<TestData> CASES = List.of(
            new TestData("""
                    ---
                    watch: false
                    loggers: {}
                    """, new ConfigMap(Map.of(), false)),
            new TestData("""
                    ---
                    watch: true
                    loggers: {}
                    """, new ConfigMap(Map.of(), true)),
            new TestData("""
                    ---
                    watch: false
                    loggers:
                        penna: { level: debug }
                    """, new ConfigMap(Map.of("penna", new ConfigNode.OnlyLevel("debug")), false)),
            new TestData("""
                    ---
                    watch: false
                    loggers: { penna: { level: debug } }
                    """, new ConfigMap(Map.of("penna", new ConfigNode.OnlyLevel("debug")), false)),
            new TestData("""
                    ---
                    watch: false
                    loggers:
                        penna:
                            level: debug
                    """, new ConfigMap(Map.of("penna", new ConfigNode.OnlyLevel("debug")), false)),
            new TestData("""
                    ---
                    watch: true
                    loggers:
                        penna:
                            level: debug
                            fields:
                                - timestamp
                                - message
                                - thread
                    """,
                    new ConfigMap(Map.of("penna", new ConfigNode.LevelAndFields("debug", List.of(
                            "timestamp",
                            "message",
                            "thread"
                    ))), true)),
            new TestData("""
                    ---
                    watch: false
                    loggers:
                        penna:
                            level: debug
                            exception:
                                deduplication: true
                                maxDepth: 128
                                traverseDepth: 3
                    """, new ConfigMap(
                    Map.of("penna",
                            new ConfigNode.LevelAndExceptions(
                                    "debug",
                                    new ExceptionHandling(128, 3, true)
                            )
                    ), false)),
            new TestData("""
                    ---
                    watch: true
                    loggers:
                        "": { level: error }
                        penna: { level: debug }
                        penna.loggers.yaml: { level: trace, fields: [message] }
                        com.other: { level: warn }
                    """, new ConfigMap(
                    Map.of("", new ConfigNode.OnlyLevel("error"),
                            "penna", new ConfigNode.OnlyLevel("debug"),
                            "penna.loggers.yaml", new ConfigNode.LevelAndFields("trace", List.of("message")),
                            "com.other", new ConfigNode.OnlyLevel("warn")
                    ), true))
    );

    @Override
    public Stream<TestData> provideArguments(ExtensionContext extensionContext, Parameter parameter) throws Exception {
        return CASES.stream();
    }

    public record TestData(String yamlDocument, ConfigMap reference) {}
}
