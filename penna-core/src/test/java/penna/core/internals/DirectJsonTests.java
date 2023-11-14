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
            directJson.writeStringValue("hello", "world");
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
    void can_write_number_kv_to_buffer() {
        var chars = Helper.write(directJson -> {
            directJson.openObject();
            directJson.writeNumberValue("hello", 1337);
            directJson.closeObject();
        });

        assertEquals("{\"hello\":1337}", chars);
    }

    @Test
    void can_format_strings() {
        var chars = Helper.write(directJson -> {
            directJson.openObject();
            directJson.writeStringValueFormatting("message", "hello {}", "world");
            directJson.closeObject();
        });

        assertEquals("{\"message\":\"hello world\"}", chars);
    }

    @Test
    void ignores_escaped_format_blocks() {
        var chars = Helper.write(directJson -> {
            directJson.openObject();
            directJson.writeStringValueFormatting("message", "hello \\{}", "world");
            directJson.closeObject();
        });

        assertEquals("{\"message\":\"hello {}\"}", chars);
    }

    @Test
    void a_previous_escape_doesnt_break_formatting() {
        var chars = Helper.write(directJson -> {
            directJson.openObject();
            directJson.writeStringValueFormatting("message", "hello \\| {}", "world");
            directJson.closeObject();
        });

        assertEquals("{\"message\":\"hello \\\\| world\"}", chars);
    }

    @Test
    void formats_double_escaped_format_blocks() {
        var chars = Helper.write(directJson -> {
            directJson.openObject();
            directJson.writeStringValueFormatting("message", "hello http:\\\\{}", "world.com");
            directJson.closeObject();
        });

        assertEquals("{\"message\":\"hello http:\\\\\\\\world.com\"}", chars);
    }

    @Test
    void can_write_nulls_when_formatting_string() {
        var chars = Helper.write(directJson -> {
            directJson.writeRawFormatting("String with {} placeholder", new Object[]{null});
        });

        assertEquals("String with {} placeholder", chars);
    }
}
