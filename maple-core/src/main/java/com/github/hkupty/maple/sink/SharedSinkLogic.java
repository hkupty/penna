package com.github.hkupty.maple.sink;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class SharedSinkLogic {
    private SharedSinkLogic() {}

    private static final FileChannel fc = new FileOutputStream(FileDescriptor.out).getChannel();

    public static OutputStream getOutputStream() {
        return Channels.newOutputStream(fc);
    }

    public static FileChannel getFileChannel() {
        return fc;
    }

}
