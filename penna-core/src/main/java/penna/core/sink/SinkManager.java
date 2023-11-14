package penna.core.sink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SinkManager {
    private Supplier<Sink> factory = CoreSink::new;
    private final List<ProxySink> proxies = new ArrayList<>();

    public static final SinkManager Instance = new SinkManager();

    private SinkManager() {}

    public void replace(Supplier<Sink> factory) {
        this.factory = factory;

        proxies.forEach(proxy -> proxy.replaceImpl(factory.get()));
    }

    public ProxySink get() {
        var proxy = new ProxySink(factory.get());
        proxies.add(proxy);
        return proxy;
    }
}
