package penna.core.logger.guard;

import org.slf4j.event.Level;

public final class InfoLevelGuard implements LevelGuard {
    private InfoLevelGuard() {
    }

    private static final LevelGuard instance = new InfoLevelGuard();

    public static LevelGuard singleton() {
        return instance;
    }

    @Override
    public Level level() {
        return Level.INFO;
    }
}
