package penna.throughput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        int time;
        int threads = 1;
        if (args.length > 0) {
            time = Integer.parseInt(args[0]);
        } else {
            time = 10;
        }
        if (args.length > 1) {
            threads = Integer.parseInt(args[1]);
        }
        Logger logger =  LoggerFactory.getLogger(Main.class);
        List<AtomicBoolean> buffer = IntStream
                .range(0, threads)
                .mapToObj(_id -> new AtomicBoolean(true)).toList();

        Thread.ofVirtual()
                .start(() -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(time));
            for (var gate : buffer) {
                gate.set(false);
            }
        });


        for (int i = 0; i < threads; i++) {
            final int finalI = i;
            Thread.ofPlatform()
                    .daemon(false)
                    .name("penna-" + i).start(() -> {
                while (buffer.get(finalI).get()) {
                    logger.info("hello");
                }
            });
        }

    }
}
