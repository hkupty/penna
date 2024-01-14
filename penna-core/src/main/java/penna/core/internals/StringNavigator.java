package penna.core.internals;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class StringNavigator implements Iterator<StringNavigator.StringView> {
    public static final int DOT = 0x2E;
    public static final int DOLLAR = 0x24;
    public final String base;
    public int startIndex;
    public int nextIndex;
    public boolean hasNext = true;
    public int symbol = DOT;


    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public StringView next() {
        StringView view;
        nextIndex = base.indexOf(symbol, startIndex);
        if (nextIndex == -1 && symbol == DOT) {
            symbol = DOLLAR;
            nextIndex = base.indexOf(symbol, startIndex);
        }
        if (nextIndex == -1) {
            hasNext = false;
            view = new StringView(base, startIndex, base.length() - startIndex);
        } else {
            view = new StringView(base, startIndex, nextIndex - startIndex);
            startIndex = nextIndex + 1;
        }
        return view;
    }

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
    }

    public StringView chunk(int index) {
        return null;
    }

    public int indexCompare(int index, String against) {
        return -1;
    }

}
