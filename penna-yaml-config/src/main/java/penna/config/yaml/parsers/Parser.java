package penna.config.yaml.parser;

import penna.config.yaml.models.ConfigMap;

import java.io.IOException;
import java.nio.file.Path;

public interface Parser {
    ConfigMap readAndParse(Path file) throws IOException;

    class Factory {
        private static Parser tryJackson() throws ClassNotFoundException {
            Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
            return new JacksonParser();
        }

        private static Parser trySnakeyamlEngine() throws ClassNotFoundException {
            Class.forName("org.snakeyaml.engine.v2.api.Load");
            return new SnakeyamlEngineParser();
        }

        private static Parser trySnakeyaml() throws ClassNotFoundException {
            Class.forName("org.yaml.snakeyaml.Yaml");
            return new SnakeyamlParser();
        }

        public static Parser getParser() {
            try {
                return tryJackson();
            } catch (ClassNotFoundException ignored) {}

            try {
                return trySnakeyamlEngine();
            } catch (ClassNotFoundException ignored) {}

            try {
                return trySnakeyaml();
            } catch (ClassNotFoundException ignored) {}

            return null;
        }

    }

}
