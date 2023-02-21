package maple.core.internals;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThreadLocalPool<T> {
    private final ThreadLocal<Pool<T>> pool;

    private ThreadLocalPool(T[] reference, int size, Supplier<T> init, Consumer<T> reset){
        Supplier<Pool<T>> poolCreator = () ->  new Pool<T>(reference, size, init, reset);
        pool = ThreadLocal.withInitial(poolCreator);
    }
    public static <T> ThreadLocalPool<T> smallPool(T[] reference, Supplier<T> init, Consumer<T> reset){
        return new ThreadLocalPool<>(reference, 16, init, reset);
    }

    public static <T> ThreadLocalPool<T> mediumPool(T[] reference, Supplier<T> init, Consumer<T> reset){
        return new ThreadLocalPool<>(reference, 128, init, reset);
    }

    public static <T> ThreadLocalPool<T> largePool(T[] reference, Supplier<T> init, Consumer<T> reset){
        return new ThreadLocalPool<>(reference, 2048, init, reset);
    }

    public T next() {
        return pool.get().next();
    }
}
