package maple.core.sink;

import maple.api.models.MapleLogEvent;
import maple.core.internals.ByteBufferOutputStream;
import maple.core.minilog.MiniLogger;
import maple.core.sink.impl.JacksonMapleSink;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MapleSink {
    private transient final ByteBuffer buffer;
    private transient final FileChannel fc;
    private transient final SinkImpl impl;

    public static class Factory {
        private static final ClassLoader loader = SinkImpl.class.getClassLoader();
        private static MapleSink singleton;

        public static MapleSink getSink() {
            if (singleton == null) {
                init();
            }

            return singleton;
        }

        private static SinkImpl tryJackson() {
            try {
                Class.forName("com.fasterxml.jackson.core.JsonGenerator", false, loader);
                return new JacksonMapleSink();
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static void init() {
            SinkImpl impl;
            if (singleton == null && (impl = tryJackson()) != null) {
                try {
                    singleton = new MapleSink(impl);
                } catch (IOException e) {
                    MiniLogger.error("Unable to create jackson logger", e);
                }
            }
        }

    }

    private MapleSink(SinkImpl impl) throws IOException {
        this.impl = impl;
        fc = new FileOutputStream(FileDescriptor.out).getChannel();
        buffer = ByteBuffer.allocateDirect(1024 * 1024);
        OutputStream os = new ByteBufferOutputStream(buffer);
        impl.init(os);
    }

    public void write(MapleLogEvent log) {
        try {
            impl.write(log);
            buffer.flip();
            fc.write(buffer);
            buffer.clear();
        } catch (IOException e) {
            MiniLogger.error("Unable to log", e);
        }
    }
}
