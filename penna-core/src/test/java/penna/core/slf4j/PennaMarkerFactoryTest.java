package penna.core.slf4j;

import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import penna.core.slf4j.marker.PennaMarker;

import static org.junit.jupiter.api.Assertions.*;

class PennaMarkerFactoryTest {

    @Test
    void markerFactoryReturnsAValidMarker(){
        var markerFactory = new PennaMarkerFactory();
        var marker = markerFactory.getMarker("test");

        assertNotNull(marker);
        assertInstanceOf(Marker.class, marker);
        assertInstanceOf(PennaMarker.class, marker);
    }

    @Test
    void markerFactoryReturnsSameInstanceForSameKey(){
        var markerFactory = new PennaMarkerFactory();
        var marker = markerFactory.getMarker("test");
        var marker2 = markerFactory.getMarker("test");

        assertSame(marker, marker2);
    }

    @Test
    void twoDifferentMarkersAreDifferent(){
        var markerFactory = new PennaMarkerFactory();
        var marker = markerFactory.getMarker("test");
        var marker2 = markerFactory.getMarker("test2");

        assertNotSame(marker, marker2);
        assertNotEquals(marker, marker2);
    }

    @Test
    void returnedMarkerProducesValidStringOutput(){
        var markerFactory = new PennaMarkerFactory();
        var marker = markerFactory.getMarker("test");

        assertEquals("test", marker.getName());
        assertEquals("test", new String(marker.buffer().array()));
    }
}