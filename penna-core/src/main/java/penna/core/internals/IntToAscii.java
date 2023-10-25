package penna.core.internals;

import java.nio.ByteBuffer;

public class IntToAscii {


    private static final byte[] innerBuffer = new byte[20];


    static int longToAscii(long num, byte[] target){
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

    static void longToAscii(long num, ByteBuffer buffer){
        int sz = longToAscii(num, innerBuffer);
        for (int i = innerBuffer.length - sz; i < innerBuffer.length; i++){
            buffer.put(innerBuffer[i]);
            innerBuffer[i] = 0x0;
        }
    }

}
