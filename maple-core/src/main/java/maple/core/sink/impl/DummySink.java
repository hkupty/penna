package maple.core.sink.impl;

import maple.core.models.MapleLogEvent;
import maple.core.sink.SinkImpl;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

public final class DummySink implements SinkImpl {

    Consumer<MapleLogEvent> consumer;

    public DummySink(Consumer<MapleLogEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void init(Writer writer) throws IOException { }

    @Override
    public void write(MapleLogEvent logEvent) throws IOException {
        consumer.accept(logEvent);
    }
}
