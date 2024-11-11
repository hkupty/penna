package penna.core.internals;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectJsonTests {
    static class Helper {
        static final Charset charset = StandardCharsets.UTF_8;

        public static String write(Consumer<DirectJson> setup) {
            DirectJson directJson = new DirectJson();
            setup.accept(directJson);
            directJson.buffer.flip();

            return charset.decode(directJson.buffer).toString();
        }

    }

    @Test
    void can_write_longs_to_buffer() {
        var chars = Helper.write(directJson -> {
            directJson.writeNumber(123);
        });

        assertEquals("123,", chars);
    }


    @Test
    void can_write_strings_to_buffer() {
        var chars = Helper.write(directJson -> {
            directJson.writeString("hello");
        });
        assertEquals("\"hello\",", chars);
    }

    @Test
    void can_write_kv_to_buffer() {
        var chars = Helper.write(directJson -> {
            directJson.openObject();
            directJson.writeStringKeyValue("hello", "world");
            directJson.closeObject();
        });

        assertEquals("{\"hello\":\"world\"}", chars);
    }

    @Test
    void can_write_array_to_buffer() {
        var chars = Helper.write(directJson -> {
            directJson.openArray();
            directJson.writeString("hello");
            directJson.writeString("world");
            directJson.closeArray();
        });

        assertEquals("[\"hello\",\"world\"]", chars);
    }

    @Test
    void can_write_nulls_when_formatting_string() {
        var chars = Helper.write(directJson -> {
            directJson.writeRawFormatting("String with {} placeholder", new Object[]{null});
        });

        assertEquals("String with {} placeholder", chars);
    }
}
