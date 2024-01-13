package penna.core.internals;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class StringNavigator {
    public static final int DOT = 0x2E;
    public static final int DOLLAR = 0x24;
    public final String base;
    public final StringView[] views;
    public final int target;

    public record StringView(String base, int startingPoint, int length) implements CharSequence {

        @Override
        public int length() {return length;}

        @Override
        public char charAt(int index) {
            return base.charAt(startingPoint + index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return base.subSequence(startingPoint + start, startingPoint + Math.min(end, length));
        }

        @Override
        public @NotNull String toString() {
            return base.substring(startingPoint, startingPoint + length);
        }

        public int indexCompare(String against) {
            int diff = CharSequence.compare(this, against);
            return Integer.signum(diff) + 1;
        }
    }

    public StringNavigator(final String base) {
        this.base = base;
        var views = new StringView[6];
        int index = -1;
        int chunks = 0;
        while (true) {
            var previousIndex = index + 1;
            index = base.indexOf(DOT, previousIndex);
            if (chunks == views.length) {
                views = Arrays.copyOf(views, views.length * 2);
            }
            if (index > 0) {
                views[chunks++] = new StringView(base, previousIndex, index - previousIndex);
            } else {
                views[chunks++] = new StringView(base, previousIndex, base.length() - previousIndex);
                previousIndex = index + 1;
                while ((index = base.indexOf(DOLLAR, previousIndex)) > 0) {
                    if (chunks == views.length) {
                        views = Arrays.copyOf(views, views.length * 2);
                    }
                    views[chunks++] = new StringView(base, previousIndex, index - previousIndex);
                    previousIndex = index + 1;
                }
                break;
            }
        }
        this.target = chunks;
        this.views = views;
    }

    public int target() {
        return this.target;
    }

    public StringView chunk(int index) {
        return views[index];
    }

    public int indexCompare(int index, String against) {
        return views[index].indexCompare(against);
    }

}
