package maple.core.sink;

import maple.core.models.MapleLogEvent;
import maple.core.minilog.MiniLogger;
import maple.core.sink.impl.JacksonMapleSink;

import java.io.*;
import java.util.function.Supplier;

public final class MapleSink {
    private transient final SinkImpl impl;

    public static final class Factory {

        private Factory() {}

        // From the same ticket that PMD references, https://bugs.openjdk.org/browse/JDK-8080225, it is noted that
        // in JDK 10 the problem was solved. We are targeting JDK 17+, so the problem won't affect us.
        // Plus, any other alternative is significantly slower.
        @SuppressWarnings("PMD.AvoidFileStream")
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
