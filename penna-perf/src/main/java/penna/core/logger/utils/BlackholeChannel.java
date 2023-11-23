package penna.core.logger.utils;

import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class BlackholeChannel implements WritableByteChannel {

    final Blackhole bh;

    public BlackholeChannel(Blackhole bh) {
        this.bh = bh;
    }


    @Override
    public int write(ByteBuffer src) throws IOException {
        bh.consume(src);
        return 0;
    }

    @Override
    public boolean isOpen() {
        return bh != null;
    }

    @Override
    public void close() throws IOException {

    }
}
