package maple.core.sink;

import maple.core.models.MapleLogEvent;
import maple.core.minilog.MiniLogger;
import maple.core.sink.impl.JacksonMapleSink;

import java.io.*;
import java.util.function.Supplier;

public class MapleSink {
    private transient final SinkImpl impl;

    public static class Factory {
        public static MapleSink getSink() {
            Supplier<SinkImpl> impl;
            if ((impl = tryJackson()) != null) {
                try {
                    SinkImpl sinkImpl = impl.get();
                    sinkImpl.init(new FileWriter(FileDescriptor.out));
                    return new MapleSink(sinkImpl);
                } catch (IOException e) {
                    MiniLogger.error("Unable to create jackson logger", e);
                }
            }
            return null;
        }

        private static Supplier<SinkImpl> tryJackson() {
            try {
                Class.forName("com.fasterxml.jackson.core.JsonGenerator");
                return JacksonMapleSink::new;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    private MapleSink(SinkImpl impl) throws IOException {
        this.impl = impl;
    }

    public void write(MapleLogEvent log) {
        try {
            impl.write(log);
        } catch (IOException e) {
            MiniLogger.error("Unable to log", e);
        }
    }
}
