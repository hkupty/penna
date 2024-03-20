package penna.config.yaml.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParserTests {
    private static final SnakeyamlParser snakeyamlParser = new SnakeyamlParser();
    private static final SnakeyamlEngineParser snakeyamlEngineParser = new SnakeyamlEngineParser();
    private static final JacksonParser jacksonParser = new JacksonParser();
    private Path tempFile;

    public enum Impl {
        SnakeYaml,
        SnakeYamlEngine,
        Jackson
    }

    private static Parser fromEnum(Impl impl) {
        return switch (impl) {
            case SnakeYaml -> snakeyamlParser;
            case SnakeYamlEngine -> snakeyamlEngineParser;
            case Jackson -> jacksonParser;
        };
    }

    @BeforeEach
    void prep() throws IOException {
        tempFile = Files.createTempFile("parserTest", ".yaml").toAbsolutePath();
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.delete(tempFile);
    }

    @CartesianTest
    void canParseYamlDocuments(
            @CartesianTest.Enum Impl impl,
            @ParserTestCases ParserTestCasesProvider.TestData testData
    ) throws IOException {
        var parser = fromEnum(impl);
        Files.write(tempFile, testData.yamlDocument().getBytes());
        var result = parser.readAndParse(tempFile);
        Assertions.assertEquals(testData.reference(), result);
    }
}
