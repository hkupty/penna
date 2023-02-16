package com.github.hkupty.maple.internals;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This is a simple layer on top of a ByteBuffer that has to be
 * handled separately. Do not use it directly because it will *not*
 * control the byte buffer appropriately.
 */
public class ByteBufferOutputStream extends OutputStream {

    private final ByteBuffer buffer;

    public ByteBufferOutputStream(ByteBuffer buffer) { this.buffer = buffer; }


    @Override
    public void write(int i) {
        buffer.put((byte) i);
    }

    @Override
    public void write(byte[] bytes) {
        buffer.put(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int len) {
        buffer.put(bytes, offset, len);
    }
}
