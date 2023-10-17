package penna.core.internals;


import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public final class DirectJson implements Closeable {
    private static final int INITIAL_BUFFER_SIZE = 32 * 1024;
    private static final byte[] LINE_BREAK = System.getProperty("line.separator").getBytes(StandardCharsets.UTF_8);
    private static final byte QUOTE = '"';
    private static final byte ENTRY_SEP = ':';
    private static final byte KV_SEP = ',';
    private static final byte DOT = '.';
    private static final byte OPEN_OBJ = '{';
    private static final byte CLOSE_OBJ = '}';
    private static final byte OPEN_ARR = '[';
    private static final byte CLOSE_ARR = ']';

    private static final byte[] NEWLINE = new byte[] {
            '\\',
            'n',
    };
    private static final byte[] ESCAPE = new byte[] {
            '\\',
            '\\',
    };
    private static final byte[] LINEBREAK = new byte[] {
            '\\',
            'r',
    };
    private static final byte[] TAB = new byte[] {
            '\\',
            't',
    };
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
    static final byte DELIM_START = '{';
    static final byte DELIM_STOP = '}';

    private final FileOutputStream backingOs;
    private final FileChannel channel;
    // This is not private only for the sake of testing
    ByteBuffer buffer = ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);

    public DirectJson(FileChannel channel) {
        this.backingOs = null;
        this.channel = channel;
    }
    // From the same ticket that PMD references, https://bugs.openjdk.org/browse/JDK-8080225, it is noted that
    // in JDK 10 the problem was solved. We are targeting JDK 17+, so the problem won't affect us.
    // Plus, any other alternative is significantly slower.
    @SuppressWarnings("PMD.AvoidFileStream")
    public DirectJson() {
        this.backingOs = new FileOutputStream(FileDescriptor.out);
        this.channel = backingOs.getChannel();
    }

    public void openObject() { buffer.put(OPEN_OBJ); }
    public void openArray() { buffer.put(OPEN_ARR); }

    public void openObject(String str) {
        writeKeyString(str);
        buffer.put(OPEN_OBJ);
    }

    public void openArray(String str) {
        writeKeyString(str);
        buffer.put(OPEN_ARR);
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

    void writeUnsafe(String str) {
        for(int i = 0; i < str.length(); i++){
            buffer.put((byte) str.codePointAt(i));
        }

    }

    public void writeRaw(String str) {
        for(int i = 0; i < str.length(); i++ ){
            var chr = str.codePointAt(i);
            switch (chr) {
                case '\\' -> buffer.put(ESCAPE);
                case '\n' -> buffer.put(NEWLINE);
                case '\r' -> buffer.put(LINEBREAK);
                case '\t' -> buffer.put(TAB);
                default -> {
                    if (chr >= 0x80 && chr <= 0x10FFFF) {
                        buffer.put(String.valueOf(str.charAt(i)).getBytes());
                    } else if (chr > 0x1F) buffer.put((byte) chr);
                }
            }
        }
    }


    public void writeRawFormatting(String str, Object... arguments) {
        int cursor = 0;
        for(int i = 0; i < str.length(); i++ ){
            var chr = str.codePointAt(i);
            switch (chr) {
                case '\\' -> buffer.put(ESCAPE);
                case '\n' -> buffer.put(NEWLINE);
                case '\r' -> buffer.put(LINEBREAK);
                case '\t' -> buffer.put(TAB);
                case DELIM_START -> {
                    if (str.codePointAt(i + 1) == '}') {
                        if (str.codePointAt(i - 1) == '\\' && str.codePointAt(i - 2) != '\\') {
                            buffer.put(buffer.position() - 1, DELIM_START);
                        } else {
                            // Warning! Mismatch in arguments/template might cause exception.
                            writeRaw(arguments[cursor++].toString());
                        }
                    }
                }
                case DELIM_STOP -> {
                    if (str.codePointAt(i-1) == '{' && str.codePointAt(i-2) == '\\' && str.codePointAt(i-3) != '\\') {
                        buffer.put(DELIM_STOP);
                    }
                }
                default -> {
                    if (chr >= 0x80 && chr <= 0x10FFFF) {
                        buffer.put(String.valueOf(str.charAt(i)).getBytes());
                    } else if (chr > 0x1F) buffer.put((byte) chr);
                }
            }
        }
    }



    public void writeRaw(char chr) { buffer.put((byte) chr); }
    public void writeRaw(byte[] chr) { buffer.put(chr); }

    public void writeQuote() { buffer.put(QUOTE); }

    public void writeKeyString(String str) {
        checkSpace(str.length() + 3);
        buffer.put(QUOTE);
        writeUnsafe(str);
        buffer.put(QUOTE);
        buffer.put(ENTRY_SEP);
    }

    public void writeUnsafeString(String str) {
        checkSpace(str.length() + 3);
        buffer.put(QUOTE);
        writeUnsafe(str);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeString(String str) {
        checkSpace(str.length() + 3);
        buffer.put(QUOTE);
        writeRaw(str);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeStringFormatting(String str, Object... args) {
        checkSpace(str.length() + 3);
        buffer.put(QUOTE);
        writeRawFormatting(str, args);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeSep() { buffer.put(KV_SEP); }

    public void writeNumberRaw(final long data) {
        final int pos = buffer.position();
        final int sz = (int) Math.log10(data) + 1;
        long dataPointer = data;

        for (int i = sz - 1; i >= 0; i--) {
            byte chr = (byte) (dataPointer % 10);
            dataPointer = dataPointer / 10;
            chr += 48;
            buffer.put(pos + i, chr);
        }

        buffer.position(pos + sz);
    }

    public void writeNumber(final long data) {
        if (data < 0) {
            writeRaw('-');
        }

        final int pos = buffer.position();
        long dataPointer = Math.abs(data);
        final int sz = data == 0 ? 1 : (int) Math.log10(dataPointer) + 1;

        for (int i = sz - 1; i >= 0; i--) {
            byte chr = (byte) (dataPointer % 10);
            dataPointer = dataPointer / 10;
            chr += 48;
            buffer.put(pos + i, chr);
        }

        buffer.position(pos + sz);
        buffer.put(KV_SEP);
    }

    public void writeNumber(final double data) {
        if (data < 0) {
            writeRaw('-');
        }
        int pos = buffer.position();
        double absData = Math.abs(data);
        long whole = (long) absData;
        final int sz = (int) Math.log10(whole) + 1;

        for (int i = sz - 1; i >= 0; i--) {
            byte chr = (byte) (whole % 10);
            whole = whole / 10;
            chr += 48;
            buffer.put(pos + i, chr);
        }
        buffer.position(pos + sz);
        buffer.put(DOT);
        pos = buffer.position();
        BigDecimal fractional = BigDecimal.valueOf(absData).remainder(BigDecimal.ONE);
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

    public void writeEntrySep() { buffer.put(buffer.position() - 1, ENTRY_SEP); }

    public void writeStringValue(String key, String value) {
        writeKeyString(key);
        writeString(value);
    }

    public void writeStringValueFormatting(String key, String value, Object... args) {
        writeKeyString(key);
        writeStringFormatting(value, args);
    }

    public void writeNumberValue(String key, long value) {
        writeKeyString(key);
        writeNumber(value);
    }

    public void writeNumberValue(String key, double value) {
        writeKeyString(key);
        writeNumber(value);
    }

    public void writeBoolean(boolean value) {
        buffer.put(value ? TRUE : FALSE);
        buffer.put(KV_SEP);
    }

    public void writeNull() {
        buffer.put(NULL);
        buffer.put(KV_SEP);
    }

    void checkSpace(int size) {
        if ((buffer.position() + size * 2 ) > buffer.capacity()) {
            ByteBuffer newBuffer = ByteBuffer.allocateDirect((buffer.capacity() + size) * 2);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    public void flush() throws IOException {
        buffer.put(LINE_BREAK);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        channel.close();
        if (this.backingOs != null) backingOs.close();
    }
}