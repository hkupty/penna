package maple.throughput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        long count;
        int threads = 1;
        if (args.length > 0) {
            count = Long.parseLong(args[0]);
        } else {
            count = 100_000L;
        }
        if (args.length > 1) {
            threads = Integer.parseInt(args[1]);
        }
        Logger logger = LoggerFactory.getLogger(Main.class);

        for (int i = 0; i < threads; i++) {
            int finalI = i;
            new Thread(() -> {
                for(long l = 0; l < count; l++) {
                    logger.atInfo()
                            .addKeyValue("thread", finalI)
                            .addKeyValue("count", l)
                            .log("some message");
                }
            }).start();
        }
    }
}