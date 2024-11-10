package penna.core.slf4j;

import org.jetbrains.annotations.NotNull;
import org.slf4j.IMarkerFactory;
import penna.core.slf4j.marker.PennaMarker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an {@link IMarkerFactory} implementation that builds {@link PennaMarker} instances.
 */
public class PennaMarkerFactory implements IMarkerFactory {
    private final Map<@NotNull String, @NotNull PennaMarker> storage = new ConcurrentHashMap<>();

    @Override
    public PennaMarker getMarker(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name cannot be null");
        }
        return storage.computeIfAbsent(name, this::getDetachedMarker);
    }

    @Override
    public boolean exists(String name) {
        if (name == null) {
            return false;
        }

        return storage.containsKey(name);
    }

    @Override
    public boolean detachMarker(String name) {
        if (name == null) {
            return false;
        }

        return storage.remove(name) != null;
    }

    @Override
    public PennaMarker getDetachedMarker(String name) {
        return new PennaMarker(name);
    }
}
