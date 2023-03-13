package penna.core.internals;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteBufferWriter extends Writer {
    private final Charset charset = StandardCharsets.UTF_8;
    private final ByteBuffer buffer;
    private final FileChannel channel;

    public ByteBufferWriter(ByteBuffer buffer, FileChannel channel) {
        this.buffer = buffer;
        this.channel = channel;
    }

    @Override
    public void write(char[] chars, int offset, int length) throws IOException {
        CharBuffer charBuffer = CharBuffer.wrap(chars, offset, length);
        buffer.put(charset.encode(charBuffer));
    }

    @Override
    public void flush() throws IOException {
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
