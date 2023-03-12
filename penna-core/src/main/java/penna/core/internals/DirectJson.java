package penna.core.internals;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public final class DirectJson {

    private static final byte[] LINE_BREAK = System.getProperty("line.separator").getBytes(StandardCharsets.UTF_8);
    private static final byte QUOTE = '"';
    private static final byte ENTRY_SEP = ':';
    private static final byte KV_SEP = ',';
    private static final byte DOT = '.';
    private static final byte OPEN_OBJ = '{';
    private static final byte CLOSE_OBJ = '}';
    private static final byte OPEN_ARR = '[';
    private static final byte CLOSE_ARR = ']';
    private static final byte[] TRUE = new byte[] {
            't',
            'r',
            'u',
            'e'
    };

    private static final byte[] FALSE = new byte[] {
            'f',
            'a',
            'l',
            's',
            'e'
    };
    private static final byte[] NULL = new byte[] {
            'n',
            'u',
            'l',
            'l'
    };

    private final FileChannel channel;
    final ByteBuffer buffer = ByteBuffer.allocateDirect(32 * 1024); // 32KB should be enough for a *single* json message

    public DirectJson(FileChannel channel) {
        this.channel = channel;
    }

    public DirectJson() { this(new FileOutputStream(FileDescriptor.out).getChannel()); }

    public void openObject() {
        buffer.put(OPEN_OBJ);
    }

    public void openObject(String str) {
        writeString(str);
        writeEntrySep();
        buffer.put(OPEN_OBJ);
    }

    public void openArray() {
        buffer.put(OPEN_ARR);
    }

    public void openArray(String str) {
        writeString(str);
        writeEntrySep();
        buffer.put(OPEN_OBJ);
    }

    public void closeObject() {
        var target = buffer.position() - 1;
        if (',' == buffer.get(target)) {
            buffer.put(target, CLOSE_OBJ);
        } else {
            buffer.put(CLOSE_OBJ);
        }
    }

    public void closeArray() {
        var target = buffer.position() - 1;
        if (',' == buffer.get(target)) {
            buffer.put(target, CLOSE_ARR);
        } else {
            buffer.put(CLOSE_ARR);
        }
    }

    public void writeString(CharSequence str) {
        buffer.put(QUOTE);
        for (int i = 0; i < str.length(); i ++ ) {
            buffer.put((byte) str.charAt(i));
        }
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeSep() {
        buffer.put(KV_SEP);
    }

    public void writeNumber(long data) {
        final int pos = buffer.position();
        final int sz = (int) Math.log10(data) + 1;

        for (int i = sz - 1; i >= 0; i--) {
            char chr = (char) (data % 10);
            data = data / 10;
            chr += 48;
            buffer.put(pos + i, (byte) chr);
        }

        buffer.position(pos + sz);
        buffer.put(KV_SEP);
    }

    public void writeNumber(double data) {
        int pos = buffer.position();
        long whole = (long) data;
        final int sz = (int) Math.log10(whole) + 1;

        for (int i = sz - 1; i >= 0; i--) {
            char chr = (char) (whole % 10);
            whole = whole / 10;
            chr += 48;
            buffer.put(pos + i, (byte) chr);
        }
        buffer.position(pos + sz);
        buffer.put(DOT);
        pos = buffer.position();
        BigDecimal fractional = BigDecimal.valueOf(data).remainder(BigDecimal.ONE);
        int decs = 0;
        while (!fractional.equals(BigDecimal.ZERO)) {
            fractional = fractional.movePointRight(1);
            byte chr = (byte) (fractional.intValue() + 48);
            fractional = fractional.remainder(BigDecimal.ONE);
            decs += 1;
            buffer.put(chr);
        }

        buffer.position(pos + decs);
        buffer.put(KV_SEP);
    }

    public void writeEntrySep() {
        buffer.put(buffer.position() - 1, ENTRY_SEP);
    }

    public void writeStringValue(String key, String value) {
        writeString(key);
        writeEntrySep();
        writeString(value);
    }

    public void writeNumberValue(String key, long value) {
        writeString(key);
        writeEntrySep();
        writeNumber(value);
    }

    public void writeNumberValue(String key, double value) {
        writeString(key);
        writeEntrySep();
        writeNumber(value);
    }

    public void writeNull() {
        buffer.put(NULL);
        buffer.put(KV_SEP);
    }

    public void flush() throws IOException {
        buffer.put(LINE_BREAK);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }
}