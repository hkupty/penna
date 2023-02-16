package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;

public final class ThrowableDataFrame extends DataFrame<Throwable>{

    private final StringBuilder stJoiner = new StringBuilder();
    private static final char[] lineEnd = new char[]{'\n'};

    public ThrowableDataFrame(String key, Throwable value) {
        super(key, value);
    }

    @Override
    public void write(Sink.SinkWriter writer) {
        write(writer, key, value);
    }

    private void write(Sink.SinkWriter writer, String levelKey, Throwable value) {
        String message;
        StackTraceElement[] st;
        Throwable cause;

        writer.startObject(levelKey);
        if((message = value.getMessage()) != null) {
            writer.writeString("message", message);
        }
        if ((st = value.getStackTrace()) != null && st.length > 0) {
            // Reinitialize stJoiner;
            stJoiner.delete(0, stJoiner.length());
            for (int i = 0; i < st.length; i++) {
                stJoiner.append(st[i].toString());
                stJoiner.append(lineEnd);
            }
            writer.writeString("stacktrace", stJoiner.toString());
        }
        if((cause = value.getCause()) != null) {
            write(writer, "cause", cause);
        }

        writer.endObject();
    }
}
