package penna.core.internals;

import penna.core.logger.guard.LevelGuard;
import penna.core.sink.Sink;

import java.util.function.Supplier;

public class TestContextPoolManager {
    private TestContextPoolManager() {}
    public static void replace(Supplier<Sink> replacementSupplier) {
        LevelGuard.Shared.logUnits.refillThePool(replacementSupplier);
    }
}
