package penna.core.slf4j;

import org.jetbrains.annotations.NotNull;
import org.slf4j.IMarkerFactory;
import penna.core.slf4j.marker.PennaMarker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This is an {@link IMarkerFactory} implementation that builds {@link PennaMarker} instances.
 */
public class PennaMarkerFactory implements IMarkerFactory {
    private final Map<@NotNull String, @NotNull PennaMarker> storage = new ConcurrentHashMap<>();
    private final Function<@NotNull String, @NotNull PennaMarker> compute = this::getDetachedMarker;

    @Override
    public @NotNull PennaMarker getMarker(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name cannot be null");
        }
        PennaMarker marker;

        if ((marker = storage.get(name)) == null) {
            // `computeIfAbsent` can be unnecessarily expensive if we have the marker already
            marker = storage.computeIfAbsent(name, compute);
        }

        return marker;
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
