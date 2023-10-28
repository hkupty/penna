package penna.core.internals;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectJsonTests {

    @Test
    void can_write_longs_to_buffer() {
        DirectJson directJson = new DirectJson();

        directJson.writeNumber(123);
        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("123,", chars);
    }


    @Test
    void can_write_strings_to_buffer() {
        DirectJson directJson = new DirectJson();

        directJson.writeString("hello");
        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("\"hello\",", chars);
    }

    @Test
    void can_write_kv_to_buffer() {
        DirectJson directJson = new DirectJson();

        directJson.openObject();
        directJson.writeStringValue("hello", "world");
        directJson.closeObject();
        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("{\"hello\":\"world\"}", chars);
    }

    @Test
    void can_write_array_to_buffer() {
        DirectJson directJson = new DirectJson();

        directJson.openArray();
        directJson.writeString("hello");
        directJson.writeString("world");
        directJson.closeArray();
        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("[\"hello\",\"world\"]", chars);
    }

    @Test
    void can_write_number_kv_to_buffer() {
        DirectJson directJson = new DirectJson();

        directJson.openObject();
        directJson.writeNumberValue("hello", 1337);
        directJson.closeObject();
        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("{\"hello\":1337}", chars);
    }

    @Test
    void can_format_strings() {
        DirectJson directJson = new DirectJson();

        directJson.openObject();
        directJson.writeStringValueFormatting("message", "hello {}", "world");
        directJson.closeObject();

        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("{\"message\":\"hello world\"}", chars);
    }

    @Test
    void ignores_escaped_format_blocks() {
        DirectJson directJson = new DirectJson();

        directJson.openObject();
        directJson.writeStringValueFormatting("message", "hello \\{}", "world");
        directJson.closeObject();

        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("{\"message\":\"hello {}\"}", chars);
    }

    @Test
    void a_previous_escape_doesnt_break_formatting() {
        DirectJson directJson = new DirectJson();

        directJson.openObject();
        directJson.writeStringValueFormatting("message", "hello \\| {}", "world");
        directJson.closeObject();

        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("{\"message\":\"hello \\\\| world\"}", chars);
    }
    @Test
    void formats_double_escaped_format_blocks() {
        DirectJson directJson = new DirectJson();

        directJson.openObject();
        directJson.writeStringValueFormatting("message", "hello http:\\\\{}", "world.com");
        directJson.closeObject();

        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("{\"message\":\"hello http:\\\\\\\\world.com\"}", chars);
    }

    @Test
    void can_write_nulls_when_formatting_string(){
        DirectJson directJson = new DirectJson();

        directJson.writeRawFormatting("String with {} placeholder", new Object[]{null});
        directJson.buffer.flip();
        var charset = StandardCharsets.UTF_8;
        var chars = charset.decode(directJson.buffer).toString();
        assertEquals("String with {} placeholder", chars);
    }
}
