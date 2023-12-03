package penna.core.logger.utils;

import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.OutputStream;

final class BlackholeOutputStream extends OutputStream {

    final Blackhole bh;

    BlackholeOutputStream(Blackhole bh) {
        this.bh = bh;
    }

    @Override
    public void write(int b) throws IOException {
        bh.consume(b);
    }
}
