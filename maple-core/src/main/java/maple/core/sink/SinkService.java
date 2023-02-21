package maple.core.sink;

import maple.api.models.MapleLogEvent;
import maple.core.minilog.MiniLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.LockSupport;

public class SinkService {
    private SinkService() {}

    // I know this is horrible, but we just need a simple interface that allows us to put messages in
    // without taking anything... The Umai bag of BlockingQueue...
    private static final QueueWrapper queueWrapper = new QueueWrapper();
    private static final Thread sinkRenderThread;
    private static final ArrayBlockingQueue<MapleLogEvent> logQueue;
    private static final MapleSink sink;

    private static boolean shuttingDown = false;
    private static final Object monitor = new Object();


    static {
        /*
        1536 is 0.75 * the object pool size for a single thread.
        This means we have room to operate safely without risking overwriting an object that hasn't been printed yet

        */
        logQueue = new ArrayBlockingQueue<>(1536);
        sink = MapleSink.Factory.getSink();
        var mapleThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "maple");

        sinkRenderThread = new Thread(mapleThreadGroup, () -> {
            while (true) {
                try {
                    // We fetch the value, but only remove later, so we ensure we can drain the queue without losing
                    // any messages
                    var log = logQueue.peek();

                    // If we don't have anything here, we can park the thread. This is better than polling
                    // because we don't need to wake the thread up multiple times for nothing.
                    if (log == null) {
                        LockSupport.park();
                        continue;
                    }

                    sink.write(log);

                    // We can discard the result, since it is effectively the same as the one we just processed
                    logQueue.poll();
                } catch (Exception e) {
                    MiniLogger.error("Unable to log", e);
                     Thread.currentThread().interrupt();
                }
            }
        }, "maple-sink-render");

        // Thread has to be a daemon thread in order for us to be able to close the JVM while it still runs.
        sinkRenderThread.setDaemon(true);

        // We wait a few ms to wait for the queue to drain.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (monitor) { shuttingDown = true; }
            try {
                while (!logQueue.isEmpty()) Thread.sleep(100);
            } catch (InterruptedException ie) {
                MiniLogger.error("Got error while waiting for queue to be drained", ie);
            } finally {
                sinkRenderThread.interrupt();
            }
        }));

        sinkRenderThread.start();
    }

    private static class QueueWrapper implements SinkQueue {
        @Override
        public void enqueue(MapleLogEvent mapleLogEvent) {
            try {
                // We can't let more messages in the queue when we're shutting down.
                // Sorry for that..
                if (!shuttingDown) {
                    logQueue.put(mapleLogEvent);
                    LockSupport.unpark(sinkRenderThread);
                }
            } catch (InterruptedException e) {
                MiniLogger.error("Unable to add log", e);
            }
        }
    }

    public static SinkQueue getQueue() {
        return queueWrapper;
    }
}
