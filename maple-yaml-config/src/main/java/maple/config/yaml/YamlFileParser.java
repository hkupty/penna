package maple.config.yaml;

import java.io.InputStream;

public interface YamlFileParser {

    YamlConfigManager buildConfigManager(InputStream file);
}
