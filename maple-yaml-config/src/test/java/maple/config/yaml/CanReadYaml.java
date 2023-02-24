package maple.config.yaml;

import maple.config.yaml.jackson.JacksonYamlFileParser;
import org.junit.jupiter.api.Test;

public class CanReadYaml {

    @Test
    public void canReadYaml() {
        var parser = new JacksonYamlFileParser();
        var result = parser.readFile(ResourceLocator.yamlFile());
        assert result != null;
        var asCfg = result.asConfigItem();
        System.out.println(asCfg);

    }
}
