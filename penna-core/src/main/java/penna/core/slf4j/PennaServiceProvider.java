package penna.core.slf4j;

import penna.api.config.ConfigManager;
import penna.core.config.ConfigManagerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public final class PennaServiceProvider implements SLF4JServiceProvider {

    /**
     * Declare the version of the SLF4J API this implementation is compiled
     * against. The value of this field is modified with each major release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    @SuppressWarnings("PMD")
    public static String REQUESTED_API_VERSION = "2.0.99"; // !final

    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
        ConfigManager manager = ConfigManagerFactory.getConfigManager();
        PennaLoggerFactory pennaLoggerFactory = PennaLoggerFactory.getInstance();
        manager.bind(pennaLoggerFactory);
        manager.configure();

        this.loggerFactory = pennaLoggerFactory;
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new PennaMDCAdapter();
    }
}
