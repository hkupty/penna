package penna.core.internals;



import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public final class DirectJson implements Closeable {
    private static final int INITIAL_BUFFER_SIZE = 32 * 1024;
    private int highWatermark = (int) Math.ceil(INITIAL_BUFFER_SIZE * 0.8);
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
    @Deprecated() // Pass in a file channel instead
    public DirectJson() {
        this.backingOs = new FileOutputStream(FileDescriptor.out);
        this.channel = backingOs.getChannel();
    }

    // --[ Write stuff to the buffer ]-- //
    public void writeRawFormatting(final String str, final Object... arguments) {
        int cursor = 0;
        for(int i = 0; i < str.length(); i++){
            var chr = str.codePointAt(i);
            switch (chr) {
                case '\\' -> buffer.put(ESCAPE);
                case '\n' -> buffer.put(NEWLINE);
                case '\r' -> buffer.put(LINEBREAK);
                case '\t' -> buffer.put(TAB);
                case DELIM_START -> {
                    if (str.codePointAt(i + 1) == '}' &&
                            (str.codePointAt(i - 1) != '\\' || str.codePointAt(i - 2) == '\\')
                    ) {
                        // Warning! Mismatch in arguments/template might cause exception.
                        var argument = arguments[cursor++].toString();
                        checkSpace(argument.length());
                        writeRaw(argument);

                    } else {
                        int offset = str.codePointAt(i - 1) == '\\' ? 1 : 0;
                        buffer.put(buffer.position() - offset, DELIM_START);
                    }
                }
                case DELIM_STOP -> {
                    if (str.codePointAt(i - 1)  != '{' ||
                        (str.codePointAt(i - 2) == '\\' && str.codePointAt(i - 3) != '\\')) {
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
    public void writeRaw(final String str) {
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

    public void writeRaw(final char chr) { buffer.put((byte) chr); }
    public void writeRaw(final byte[] chars) { buffer.put(chars); }

    public void openObject() { buffer.put(OPEN_OBJ); }
    public void openArray() { buffer.put(OPEN_ARR); }

    public void openObject(String str) {
        writeKey(str);
        buffer.put(OPEN_OBJ);
    }

    public void openObject(final byte[] str) {
        writeKey(str);
        buffer.put(OPEN_OBJ);
    }

    public void openArray(String str) {
        writeKey(str);
        buffer.put(OPEN_ARR);
    }

    public void openArray(final byte[] str) {
        writeKey(str);
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

    public void writeUnsafe(final String str) {
        for(int i = 0; i < str.length(); i++){
            buffer.put((byte) str.codePointAt(i));
        }
    }

    public void writeQuote() { buffer.put(QUOTE); }

    // --[ 2nd level; based on the level above ]-- //

    public void writeStringFromBytes(final byte[] chars) {
        buffer.put(QUOTE);
        writeRaw(chars);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);

    }

    public void writeKey(String str) {
        buffer.put(QUOTE);
        writeUnsafe(str);
        buffer.put(QUOTE);
        buffer.put(ENTRY_SEP);
    }

    public void writeKey(final byte[] chrs) {
        buffer.put(QUOTE);
        writeRaw(chrs);
        buffer.put(QUOTE);
        buffer.put(ENTRY_SEP);
    }

    public void writeUnsafeString(final String str) {
        buffer.put(QUOTE);
        writeUnsafe(str);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeString(final String str) {
        // TODO ensure check space happens on the correct places, but not repeatedly
        checkSpace(str.length() + 3);
        buffer.put(QUOTE);
        writeRaw(str);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeStringFormatting(final String str, final Object... args) {
        buffer.put(QUOTE);
        writeRawFormatting(str, args);
        buffer.put(QUOTE);
        buffer.put(KV_SEP);
    }

    public void writeSep() { buffer.put(KV_SEP); }

    public void writeNumberRaw(final long data) {
        IntToAscii.longToAscii(data, buffer);
    }

    public void writeNumber(final long data) {
        if (data < 0) {
            writeRaw('-');
        }

        writeNumberRaw(data);
        buffer.put(KV_SEP);
    }

    public void writeNumber(final double data) {
        double number = data;
        if (data < 0) {
            writeRaw('-');
            number = Math.abs(data);
        }
        writeNumberRaw((long) number);
        buffer.put(DOT);
        var pos = buffer.position();
        BigDecimal fractional = BigDecimal.valueOf(number).remainder(BigDecimal.ONE);
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

    public void writeStringValue(final String key, final String value) {
        checkSpace(key.length() + value.length() + 5);
        writeKey(key);
        writeString(value);
    }

    public void writeStringValueFormatting(String key, String value, Object... args) {
        checkSpace(key.length() + value.length() + 5);
        writeKey(key);
        writeStringFormatting(value, args);
    }

    public void writeNumberValue(String key, long value) {
        checkSpace(key.length() + 3);
        writeKey(key);
        writeNumber(value);
    }

    public void writeNumberValue(String key, double value) {
        writeKey(key);
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

    public void checkSpace(int size) {
        if ((buffer.position() + size) > highWatermark) {
            var newCapacity = (buffer.capacity() + size) * 2;
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity);
            highWatermark = (int) Math.ceil(newCapacity * 0.8);
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