package penna.core.internals;


/**
 * The thread creator is a utility class that allows us to create worker threads in penna.
 * They have a few properties in common such as being daemon threads and belonging to the same {@link ThreadGroup}
 * <br />
 * @see Thread#setDaemon(boolean)
 */
public final class ThreadCreator {
    private ThreadCreator() {}

    public static Thread newThread(String name, Runnable target){
        return Thread.ofVirtual()
                .name(name)
                .unstarted(target);
    }
}
