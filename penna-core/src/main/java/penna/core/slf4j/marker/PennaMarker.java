package penna.core.slf4j.marker;

import org.slf4j.Marker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;

/**
 * This is Penna's concrete implementation of SLF4J's {@link Marker} interface.
 * It differs from the {@link org.slf4j.helpers.BasicMarker} implementation by not allowing nested markers,
 * making it have a smaller footprint.
 * <br />
 * This is a deliberate design choice to align with SLF4J's 2.x API in which multiple markers are preferred
 * over nested markers.
 */
public record PennaMarker(ByteBuffer buffer) implements Marker {

    public static PennaMarker build(String name){
        var buffer = ByteBuffer.wrap(name.getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer();
        return new PennaMarker(buffer);
    }

    @Override
    public String getName() {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    @Override
    public void add(Marker reference) {
        throw new UnsupportedOperationException("Marker implementation does not support nested markers.");
    }

    @Override
    public boolean remove(Marker reference) {
        return false;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return false;
    }

    @Override
    public Iterator<Marker> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean contains(Marker other) {
        return false;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }
}
