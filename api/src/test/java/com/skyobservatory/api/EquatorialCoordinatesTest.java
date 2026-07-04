package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class EquatorialCoordinatesTest {

    private static final double DELTA = 1e-12;

    @Test
    public void constructorStoresFields() {
        EquatorialCoordinates c = new EquatorialCoordinates(180.0, 45.0);
        assertEquals(180.0, c.getRightAscensionDegrees(), DELTA);
        assertEquals(45.0, c.getDeclinationDegrees(), DELTA);
    }

    @Test
    public void rightAscensionHours() {
        EquatorialCoordinates c = new EquatorialCoordinates(90.0, 0.0);
        assertEquals(6.0, c.getRightAscensionHours(), DELTA);
    }

    @Test
    public void zeroRAConvertsToZeroHours() {
        EquatorialCoordinates c = new EquatorialCoordinates(0.0, 0.0);
        assertEquals(0.0, c.getRightAscensionHours(), DELTA);
    }

    @Test
    public void negativeDeclination() {
        EquatorialCoordinates c = new EquatorialCoordinates(0.0, -45.0);
        assertEquals(-45.0, c.getDeclinationDegrees(), DELTA);
    }

    @Test
    public void equalsAndHashCode() {
        EquatorialCoordinates a = new EquatorialCoordinates(180.0, 45.0);
        EquatorialCoordinates b = new EquatorialCoordinates(180.0, 45.0);
        EquatorialCoordinates c = new EquatorialCoordinates(180.0, 90.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("string", a);
    }

    @Test
    public void sameRefIsEqual() {
        EquatorialCoordinates c = new EquatorialCoordinates(0, 0);
        assertEquals(c, c);
    }

    @Test
    public void toStringContainsFields() {
        EquatorialCoordinates c = new EquatorialCoordinates(45.0, 30.0);
        String s = c.toString();
        assertTrue(s.contains("RA="));
        assertTrue(s.contains("Dec="));
    }
}
