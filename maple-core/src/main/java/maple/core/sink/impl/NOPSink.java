package maple.core.sink.impl;

import maple.core.models.MapleLogEvent;
import maple.core.sink.SinkImpl;

import java.io.IOException;
import java.io.Writer;

public final class NOPSink implements SinkImpl {

    private NOPSink() {}

    private static final NOPSink singleton = new NOPSink();

    public static NOPSink getInstance() { return singleton; }
    @Override
    public void init(Writer writer) throws IOException { }

    @Override
    public void write(MapleLogEvent logEvent) throws IOException { }
}
