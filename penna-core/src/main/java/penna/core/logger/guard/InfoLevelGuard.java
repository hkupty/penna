package penna.core.logger.guard;

public final class InfoLevelGuard implements LevelGuard {
    private InfoLevelGuard() {
    }

    private static final LevelGuard instance = new InfoLevelGuard();

    public static LevelGuard singleton() {
        return instance;
    }

}
