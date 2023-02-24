package maple.core.internals;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharBufferWriter extends Writer {
    private final FileChannel out;
    private final CharBuffer buffer;

    public CharBufferWriter() {
        out = new FileOutputStream(FileDescriptor.out).getChannel();
        buffer = CharBuffer.allocate(32 * 1024);
    }

    @Override
    public void write(char[] chars, int offset, int length) throws IOException {
        buffer.put(chars, offset, length);
    }

    @Override
    public void flush() throws IOException {
        buffer.flip();
        ByteBuffer bytes = StandardCharsets.UTF_8.encode(buffer);
        out.write(bytes);
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
