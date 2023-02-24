package maple.config.yaml;

import java.io.InputStream;

public final class ResourceLocator {
    private static ClassLoader loader = ResourceLocator.class.getClassLoader();

    public static InputStream yamlFile() {
        // TODO try more files also
        return loader.getResourceAsStream("maple.yaml");
    }
}
