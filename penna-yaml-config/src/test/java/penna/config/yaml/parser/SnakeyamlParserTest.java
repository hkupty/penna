package penna.config.yaml.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import penna.api.config.ExceptionHandling;
import penna.config.yaml.models.ConfigMap;
import penna.config.yaml.models.ConfigNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class SnakeyamlParserTest {

    @Test
    void CanReadExceptionConfigs() throws IOException {
        var parser = new SnakeyamlParser();
        var config = """
                ---
                watch: false
                config:
                    penna:
                        level: debug
                        exception:
                            deduplication: true
                            maxDepth: 128
                            traverseDepth: 3
                """;
        var tmp = Files.createTempFile("testfile", ".yaml");
        Path fpath = tmp.toAbsolutePath();
        Files.write(fpath, config.getBytes());
        var result = parser.readAndParse(fpath);


        var reference = new ConfigMap(
                Map.of("penna",
                        new ConfigNode.LevelAndExceptions(
                                "debug",
                                new ExceptionHandling(128, 3, true)
                        )
                ), false);

        Assertions.assertEquals(reference, result);
    }

}