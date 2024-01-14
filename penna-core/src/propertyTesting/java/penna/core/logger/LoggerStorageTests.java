package penna.core.logger;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import java.util.List;

class LoggerStorageTests {

    @Provide
    Arbitrary<List<String>> loggerNames() {
        var builder = new StringBuilder();
        var string = Arbitraries
                .strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(32)
                .list()
                .ofMinSize(2)
                .ofMaxSize(6)
                .map(components -> {
                    builder.delete(0, builder.length());
                    components.forEach(item -> builder.append(".").append(item));
                    return builder.toString();
                });

        return string.list();
    }

    @Property
    boolean uniqueLoggers(@ForAll("loggerNames") @Size(min = 5, max = 1024) List<String> loggerNames) {
        LoggerStorage storage = new LoggerStorage();
        return loggerNames.stream().reduce(true,
                (acc, i) -> {
                    if (!acc) {
                        return false;
                    }

                    return storage.getOrCreate(i).name.equals(i);
                },
                Boolean::logicalAnd
        );

    }

    @Property
    boolean sameLogger(@ForAll("loggerNames") @Size(min = 5, max = 1024) List<String> loggerNames) {
        LoggerStorage storage = new LoggerStorage();
        return loggerNames.stream().reduce(true,
                (acc, i) -> {
                    if (!acc) {
                        return false;
                    }

                    var logger1 = storage.getOrCreate(i);
                    var logger2 = storage.getOrCreate(i);

                    return logger1.equals(logger2);
                },
                Boolean::logicalAnd
        );

    }

}
