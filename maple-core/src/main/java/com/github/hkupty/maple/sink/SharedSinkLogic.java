package com.github.hkupty.maple.sink;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

public class SharedSinkLogic {
    private SharedSinkLogic() {}

    public static OutputStream getOutputStream() {
        return Channels.newOutputStream(Channels.newChannel(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out))));
    }
}
