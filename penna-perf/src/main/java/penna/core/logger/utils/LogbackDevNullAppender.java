package penna.core.logger.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogbackDevNullAppender extends OutputStreamAppender<ILoggingEvent> {
    FileOutputStream os;

    @Override
    public void start() {
        var file = new File("/dev/null");
        try {
            os = new FileOutputStream(file);
            setOutputStream(os);
            super.start();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.stop();
    }

}
