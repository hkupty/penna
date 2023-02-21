package maple.core.internals;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic reusable Pool implementation using a circular buffer.
 * This is a very simple implementation of an object pool that rely on their simple lifecycle:
 *  - Object A is requested;
 *  - Data is set to A;
 *  - A is processed (and print to stdout);
 *  - that processing ends, with A being ignored.
 *  <br />
 *  Because the pool still holds a reference to A, it will be kept in memory and reused
 *  in the future.
 *  Also, since this is a generic implementation, one must provide the ways to initialize and clean up
 *  the object for proper use.
 *  <br />
 *  Note that this implementation is <strong>not thread safe</strong>.
 *  For a thread-safe implementation, use {@link ThreadLocalPool}, which wraps this class in a
 *  {@link ThreadLocal}.
 * @param <T> The object type which will be stored in a pool;
 */
public class Pool<T> {
    T[] objects;
    int cursor;

    Consumer<T> reset;

    Pool(T[] reference, int size, Supplier<T> init, Consumer<T> reset){
        objects = Arrays.copyOf(reference, size);
        for (int i = 0; i < size; i++) {
            objects[i] = init.get();
        }
        cursor = 0;
        this.reset = reset;
    }

    public static <T> Pool<T> smallPool(T[] reference, Supplier<T> init, Consumer<T> reset){
        return new Pool<>(reference, 16, init, reset);
    }

    public static <T> Pool<T> mediumPool(T[] reference, Supplier<T> init, Consumer<T> reset){
        return new Pool<>(reference, 128, init, reset);
    }

    public static <T> Pool<T> largePool(T[] reference, Supplier<T> init, Consumer<T> reset){
        return new Pool<>(reference, 1024, init, reset);
    }

    public T next() {
        T item = objects[cursor];
        reset.accept(item);

        /*
        While modulo seems a natural choice, it is important to remind ourselves
        that we're aiming for sparing the resources of the library users.
        Therefore, we can mask the bits of the cursor to achieve the same results:

         cursor + 1   7  00000000 00000111
               mask  15  00000000 00001111
             cursor   7  00000000 00000111

         cursor + 1  16  00000000 00010000
               mask  15  00000000 00001111
             cursor   0  00000000 00000000

        This, however, is only possible because the pool sizes are powers of 2 (16, 128 and 1024)
        */
        cursor = (cursor + 1) & (objects.length - 1);
        return item;
    }
}
