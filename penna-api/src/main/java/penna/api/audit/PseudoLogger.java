package penna.api.audit;

/**
 * This is the internal logging mechanism, so we can still log important messages if something fails on initialization.
 */
public interface PseudoLogger {
    /**
     * This method should be used to notify an event;
     * @param level How critical this event is;
     * @param event The event as a short string;
     */
    void report(String level, String event);


    /**
     * This method should be used to notify a failure or an exception, handled or not;
     * @param level How critical is the situation
     * @param event The event as a short string;
     * @param throwable The error that happened
     */
    void reportError(String level, String event, Throwable throwable);
}
