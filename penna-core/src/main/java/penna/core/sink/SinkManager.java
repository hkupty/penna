package penna.core.sink;

import penna.core.logger.PennaLogEventBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SinkManager {
    private Supplier<Sink> factory = CoreSink::new;

    private final Map<PennaLogEventBuilder, ProxySink> binding = new HashMap<>();

    public static final SinkManager Instance = new SinkManager();

    private SinkManager() {}

    public void replace(Supplier<Sink> factory) {
        this.factory = factory;
        for (ProxySink proxySink : binding.values()) {
            proxySink.replaceImpl(factory.get());
        }
    }

    public ProxySink get(PennaLogEventBuilder builder) {
        ProxySink proxySink;
        if ((proxySink = binding.get(builder)) == null) {
            proxySink = new ProxySink(factory.get());
            binding.put(builder, proxySink);
        }

        return proxySink;
    }
}
