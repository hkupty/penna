package maple.core.sink.impl.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;

import java.io.IOException;

/**
 * This is the slimmest possible json message, with no whitespaces.
 * The only whitespace present is the line break between the log messages.
 * It is required because of the nature of the log messages; We're not writing
 * json objects inside an array, but writing logs in an infinite stream (the stdout)
 * which will likely be picked up by docker/kubernetes and shipped to a log aggregator.
 * <br />
 * Because of that, we don't need to format the log with pretty spaces or anything, but
 * just ensure we break lines between individual log messages.
 */
public class NOPPrettyPrinter implements PrettyPrinter {
    private static final String LINE_BREAK = System.getProperty("line.separator");
    private static final NOPPrettyPrinter singleton = new NOPPrettyPrinter();

    public static NOPPrettyPrinter getInstance() { return singleton; }
    private NOPPrettyPrinter() {}
    @Override
    public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(LINE_BREAK);
    }

    @Override
    public void writeStartObject(JsonGenerator gen) throws IOException {
        gen.writeRaw('{');
    }

    @Override
    public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException {
        gen.writeRaw('}');
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(',');
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(':');
}

    @Override
    public void writeStartArray(JsonGenerator gen) throws IOException {
        gen.writeRaw('[');
    }

    @Override
    public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {
        gen.writeRaw(']');
}

    @Override
    public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(',');
}

    @Override
    public void beforeArrayValues(JsonGenerator gen) throws IOException { }

    @Override
    public void beforeObjectEntries(JsonGenerator gen) throws IOException { }
}
