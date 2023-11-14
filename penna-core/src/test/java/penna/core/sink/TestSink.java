package penna.core.sink;

import penna.core.models.PennaLogEvent;

import java.io.IOException;
import java.util.function.Consumer;

public final class TestSink implements NonStandardSink {

    final Consumer<PennaLogEvent> consumer;

    public TestSink(Consumer<PennaLogEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        consumer.accept(logEvent);
    }
}
