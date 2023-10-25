package penna.dev.sink;

import com.diogonunes.jcolor.Attribute;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;
import penna.core.models.PennaLogEvent;
import penna.core.sink.NonStandardSink;
import penna.core.sink.Sink;
import penna.core.slf4j.PennaMDCAdapter;
import penna.dev.models.EnhancedLogEvent;

import java.io.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static com.diogonunes.jcolor.Ansi.colorize;

public class DevRuntimeSink implements NonStandardSink, Closeable {
    FileOutputStream fos = new FileOutputStream(FileDescriptor.out);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    PrintStream ps = new PrintStream(fos, false);
    PennaMDCAdapter mdcAdapter;

    private void init() {
        if (mdcAdapter != null) return;

        if (MDC.getMDCAdapter() instanceof PennaMDCAdapter adapter) {
            mdcAdapter = adapter;
        }
    }

    private static Attribute getColor(CharSequence key) {
        return Attribute.TEXT_COLOR((Math.abs(key.hashCode()) % 155) + 40);
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        init();
        var enhanced = EnhancedLogEvent.create(logEvent);
        StringBuilder logData = new StringBuilder();
        // Headers

        logData.append(colorize(String.format("%6s", logEvent.level.toString()), Attribute.BOLD(), enhanced.accentColor()));
        logData.append(" ");
        logData.append(colorize(formatter.format(enhanced.timestamp()), Attribute.BOLD()));
        logData.append(" ");
        logData.append(colorize(new String(logEvent.threadName), Attribute.BOLD()));
        var logger = new StringBuilder(new String(logEvent.logger));
        if (logger.length() > 25) {
            var cursor = 0;
            while (cursor != -1) {
                var off = logger.indexOf(".", cursor);
                if (off == -1) break;
                logger.delete(cursor + 1, off);
                cursor = logger.indexOf(".", cursor) + 1;
                if (logger.length() <= 25) break;
            }
        }
        logData.append(" ".repeat(Math.max(34 - logEvent.threadName.length - logger.length(), 1)));
        logData.append(colorize(logger.toString(), Attribute.BOLD(), getColor(new String(logEvent.logger))));
        logData.append(colorize(" |", Attribute.BOLD()));

        // Metadata
        if(mdcAdapter.isNotEmpty()) {
            logData.append(colorize(" mdc{", Attribute.ITALIC()));

            mdcAdapter.forEach((key, value) -> {
                logData.append(colorize(key, Attribute.BOLD(), getColor(key)));
                logData.append("=");
                logData.append(colorize(value, Attribute.BOLD()));
                logData.append(" ");
            });
            logData.deleteCharAt(logData.length() - 1);
            logData.append(colorize("}", Attribute.ITALIC()));
        }

        logData.append(" ");
        if(!logEvent.markers.isEmpty()) {
            logEvent.markers.forEach(key -> {
                var kvColor = getColor(key.getName());
                logData.append(colorize("#", Attribute.BOLD(), kvColor));
                logData.append(colorize(key.getName(), Attribute.BOLD(), kvColor));
                logData.append(" ");
            });
        }
        logData.append(MessageFormatter.basicArrayFormat(logEvent.message, logEvent.arguments));

        if(!logEvent.keyValuePairs.isEmpty()) {
            logData.append(colorize(" kvs{", Attribute.ITALIC()));

            logEvent.keyValuePairs.forEach((kvp) -> {
                logData.append(colorize(kvp.key, Attribute.BOLD(), getColor(kvp.key)));
                logData.append("=");
                logData.append(colorize(kvp.value.toString(), Attribute.BOLD()));
                logData.append(" ");
            });
            logData.deleteCharAt(logData.length() - 1);
            logData.append(colorize("}", Attribute.ITALIC()));
        }


        if (logEvent.throwable != null) {
            var throwable = logEvent.throwable;
            logData.append("\n");
            logData.append(colorize(" ❌ ", Attribute.BOLD(), Attribute.BRIGHT_RED_TEXT()));
            logData.append(colorize(throwable.getClass().getName(), Attribute.YELLOW_TEXT()));
            logData.append(colorize(": ", Attribute.BOLD(), Attribute.YELLOW_TEXT()));
            logData.append(colorize(throwable.getMessage(), Attribute.YELLOW_TEXT()));
            logData.append("\n");
            for(StackTraceElement ste : throwable.getStackTrace()) {
                logData.append("\t");
                logData.append(ste.toString());
                logData.append("\n");
            }
            logData.deleteCharAt(logData.length() - 1);
        }

        ps.println(logData);
        ps.flush();
    }

    @Override
    public Supplier<Sink> sibling() {
        return DevRuntimeSink::new;
    }

    @Override
    public void close() throws IOException {
        fos.close();
        ps.close();
    }
}
