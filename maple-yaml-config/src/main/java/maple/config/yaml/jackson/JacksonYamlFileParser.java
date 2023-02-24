package maple.config.yaml.jackson;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import maple.config.yaml.YamlConfigManager;
import maple.config.yaml.YamlDataStructure;
import maple.config.yaml.YamlFileParser;

import java.io.IOException;
import java.io.InputStream;

public class JacksonYamlFileParser implements YamlFileParser {
    private YAMLFactory factory;

    public JacksonYamlFileParser() {
        factory = new YAMLFactory();
    }

    public YamlDataStructure readChild(YAMLParser parser, YamlDataStructure base) throws IOException {
        JsonToken token; // ignore start object
        while ((token = parser.nextToken()) != null && token != JsonToken.END_ARRAY) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.START_OBJECT) { continue; }
            if (token != JsonToken.FIELD_NAME) {
                throw new RuntimeException("logger name should be a string");
            }
            var child = base.child(parser.getText());
            base.childLoggers.add(readYaml(parser, child));
        }

        return base;

    }

    public YamlDataStructure readYaml(YAMLParser parser, YamlDataStructure base) throws IOException {
        JsonToken token;
        while ((token = parser.nextToken()) != null && token != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME && "level".equals(parser.getCurrentName())) {
                if ((token = parser.nextToken()) == JsonToken.VALUE_STRING) {
                    base.level = parser.getText();
                }
            } else if (token == JsonToken.FIELD_NAME && "fields".equals(parser.getCurrentName())) {
                if ((token = parser.nextToken()) != JsonToken.START_ARRAY) {
                    throw new RuntimeException("fields should be an array");
                }
                while ((token = parser.nextToken()) == JsonToken.VALUE_STRING) {
                    base.logFields.add(parser.getText());
                }

            } else if (token == JsonToken.FIELD_NAME && "loggers".equals(parser.getCurrentName())) {
                if ((token = parser.nextToken()) != JsonToken.START_ARRAY) {
                    throw new RuntimeException("loggers should be an array");
                }
                base = readChild(parser, base);
            }
        }
        parser.nextToken();
        return base;
    }


    public YamlDataStructure readYaml(YAMLParser parser) throws IOException {
        var base = new YamlDataStructure();
        return readYaml(parser, base);
    }


    public YamlDataStructure readFile(InputStream file) {
        try {
        var parser = factory.createParser(file);
        JsonToken token ;
        while ((token = parser.nextToken()) != null) {
            if(JsonToken.FIELD_NAME.equals(token)) {
                if(!"maple".equals(parser.getCurrentName())) {
                    // Sanity check, we should have only a single top-level key here..
                    // throw error
                }
                return readYaml(parser);
            }
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
        return null;
}

    @Override
    public YamlConfigManager buildConfigManager(InputStream file) {
        var fileRead = readFile(file);
        return null;
    }
}
