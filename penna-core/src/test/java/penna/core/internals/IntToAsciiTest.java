package penna.core.internals;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntToAsciiTest {

    @Test
    void longToAscii() {
        var intToAscii = new IntToAscii();
        byte[] digits = new byte[1];
        assertEquals(1, intToAscii.longToAscii(1, digits));
        assertEquals('1', digits[0]);

        assertEquals(1, intToAscii.longToAscii(2, digits));
        assertEquals('2', digits[0]);

        assertEquals(1, intToAscii.longToAscii(3, digits));
        assertEquals('3', digits[0]);

        digits = new byte[2];

        assertEquals(2, intToAscii.longToAscii(30, digits));
        assertEquals('3', digits[0]);
        assertEquals('0', digits[1]);

        digits = new byte[3];

        assertEquals(3, intToAscii.longToAscii(274, digits));
        assertEquals('2', digits[0]);
        assertEquals('7', digits[1]);
        assertEquals('4', digits[2]);

        digits = new byte[4];

        assertEquals(4, intToAscii.longToAscii(1945, digits));
        assertEquals('1', digits[0]);
        assertEquals('9', digits[1]);
        assertEquals('4', digits[2]);
        assertEquals('5', digits[3]);

        digits = new byte[5];

        assertEquals(5, intToAscii.longToAscii(27195, digits));
        assertEquals('2', digits[0]);
        assertEquals('7', digits[1]);
        assertEquals('1', digits[2]);
        assertEquals('9', digits[3]);
        assertEquals('5', digits[4]);

        digits = new byte[8];

        assertEquals(8, intToAscii.longToAscii(38761289, digits));
        assertEquals('3', digits[0]);
        assertEquals('8', digits[1]);
        assertEquals('7', digits[2]);
        assertEquals('6', digits[3]);
        assertEquals('1', digits[4]);
        assertEquals('2', digits[5]);
        assertEquals('8', digits[6]);
        assertEquals('9', digits[7]);
    }

}