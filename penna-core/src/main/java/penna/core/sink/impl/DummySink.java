package penna.core.sink.impl;

import penna.core.models.PennaLogEvent;
import penna.core.sink.SinkImpl;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

public final class DummySink implements SinkImpl {

    Consumer<PennaLogEvent> consumer;

    public DummySink(Consumer<PennaLogEvent> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void init(FileChannel channel) throws IOException {
        // There's no usage for writer in this implementation
        // as this class exists for testing purposes, and we're not
        // writing it to STDOUT anyway, at least not through the writer instance.
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        consumer.accept(logEvent);
    }
}
