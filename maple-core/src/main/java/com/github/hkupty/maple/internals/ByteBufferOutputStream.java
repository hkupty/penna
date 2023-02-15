package com.github.hkupty.maple.internals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
