package maple.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        int time = 10;
        int threads = 1;
        if (args.length > 0) {
            time = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            threads = Integer.parseInt(args[1]);
        }
        Logger logger = LoggerFactory.getLogger(Main.class);
        Instant target = Instant.now().plus(time, ChronoUnit.SECONDS);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
            while (Instant.now().isBefore(target)) {
                logger.atInfo().log("some message");
            }
            }).start();
        }
    }
}