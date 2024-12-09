package penna.core.internals;

import java.nio.ByteBuffer;

/**
 * Utility helper to transform numbers to their equivalent ascii values.
 * <br/>
 * Utilizes "fast" math (relies on JVM optimizations though) to efficiently extract each digit and convert it to
 * ascii by applying `| 0x30` to it.
 */
public final class IntToAscii {

    private final byte[] innerBuffer = new byte[20];

    static ByteBuffer createTimestampBuffer(long number) {
        byte[] bytearray = new byte[13];
        longToAscii(number, bytearray);

        return ByteBuffer.wrap(bytearray);
    }

    static void asciiIncrement(ByteBuffer buffer) {
        byte current;
        var cursor = buffer.limit() - 1;
        boolean overflow;
        do {
            current = (byte) (buffer.get(cursor) + 1);
            overflow = (current == 0b00111010);
            if (overflow) {
                current = 0b00110000;
            }
            buffer.put(cursor--, current);
        } while(overflow && cursor >= 0);
        buffer.rewind();
    }

    /**
     * writes {@param num} to the byte[], one digit at a time
     *
     * @param num    the number to be encoded in ascii
     * @param target the byte array to write to
     * @return the number of bytes written
     */
    static int longToAscii(long num, byte[] target) {
        var quot = num;
        int i = target.length - 1;
        for (; i >= 0; i--) {
            var temp = quot;
            quot = quot / 10;
            var rem = temp - (quot * 10);
            target[i] = (byte) (rem | 0x30);
            if (quot == 0) break;
        }

        return target.length - i;
    }

    /**
     * Writes {@param num} to the supplied {@link ByteBuffer}.
     *
     * @param num    the number to be encoded
     * @param buffer the buffer where the digits should be written to
     */
    void longToAscii(long num, ByteBuffer buffer) {
        // Check if it can be optimized by doing it in a single pass
        int sz = longToAscii(num, innerBuffer);
        buffer.put(innerBuffer, innerBuffer.length - sz, sz);
    }
}
