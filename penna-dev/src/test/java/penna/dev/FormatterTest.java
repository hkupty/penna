package penna.dev;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import penna.dev.Formatter;

class FormatterTest {

    @Test
    void no_args_means_identity() {
        Assertions.assertEquals("No args", Formatter.format("No args", new Object[]{}));
    }

    @Test
    void can_format_one_arg() {
        Assertions.assertEquals("one arg", Formatter.format("{} arg", new Object[]{"one"}));
        Assertions.assertEquals("1 arg", Formatter.format("{} arg", new Object[]{1}));
        Assertions.assertEquals("1.0 arg", Formatter.format("{} arg", new Object[]{1.0}));
        Assertions.assertEquals("null arg", Formatter.format("{} arg", new Object[]{null}));
    }

    @Test
    void excess_args_dont_break() {
        Assertions.assertEquals("one arg", Formatter.format("{} arg", new Object[]{"one", "two"}));
        Assertions.assertEquals("1 arg", Formatter.format("{} arg", new Object[]{1, 2}));
        Assertions.assertEquals("1.0 arg", Formatter.format("{} arg", new Object[]{1.0, 2.1}));
        Assertions.assertEquals("null arg", Formatter.format("{} arg", new Object[]{null, null}));
    }

    @Test
    void missing_args_dont_break() {
        Assertions.assertEquals("one arg {}", Formatter.format("{} arg {}", new Object[]{"one"}));
        Assertions.assertEquals("two args and {}", Formatter.format("{} args {} {}", new Object[]{"two", "and"}));
    }


    @Test
    void single_escaped_blocks_are_ignored() {
        Assertions.assertEquals("\\{} arg one", Formatter.format("\\{} arg {}", new Object[]{"one"}));
        Assertions.assertEquals("\\{} args \\{} two", Formatter.format("\\{} args \\{} {}", new Object[]{"two", "and"}));
    }


    @Test
    void double_escaped_blocks_are_formatted() {
        Assertions.assertEquals("\\\\one arg {}", Formatter.format("\\\\{} arg {}", new Object[]{"one"}));
        Assertions.assertEquals("\\{} args \\\\two and", Formatter.format("\\{} args \\\\{} {}", new Object[]{"two", "and"}));
    }

}