package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class HorizontalCoordinateTest {

    private static final double DELTA = 1e-9;

    @Test
    public void constructorStoresValues() {
        HorizontalCoordinate c = new HorizontalCoordinate(180.0, 45.0);
        assertEquals(180.0, c.getAzimuthDegrees(), DELTA);
        assertEquals(45.0, c.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void isAboveHorizonTrueWhenAltitudePositive() {
        HorizontalCoordinate c = new HorizontalCoordinate(90.0, 10.0);
        assertTrue(c.isAboveHorizon());
    }

    @Test
    public void isAboveHorizonFalseWhenAltitudeZero() {
        HorizontalCoordinate c = new HorizontalCoordinate(90.0, 0.0);
        assertFalse(c.isAboveHorizon());
    }

    @Test
    public void isAboveHorizonFalseWhenAltitudeNegative() {
        HorizontalCoordinate c = new HorizontalCoordinate(90.0, -5.0);
        assertFalse(c.isAboveHorizon());
    }

    @Test
    public void equalsByValue() {
        HorizontalCoordinate a = new HorizontalCoordinate(180.0, 45.0);
        HorizontalCoordinate b = new HorizontalCoordinate(180.0, 45.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void notEqualsDifferentAzimuth() {
        HorizontalCoordinate a = new HorizontalCoordinate(180.0, 45.0);
        HorizontalCoordinate b = new HorizontalCoordinate(90.0, 45.0);
        assertNotEquals(a, b);
    }

    @Test
    public void notEqualsDifferentAltitude() {
        HorizontalCoordinate a = new HorizontalCoordinate(180.0, 45.0);
        HorizontalCoordinate b = new HorizontalCoordinate(180.0, 30.0);
        assertNotEquals(a, b);
    }

    @Test
    public void notEqualsNull() {
        HorizontalCoordinate c = new HorizontalCoordinate(0.0, 0.0);
        assertNotEquals(null, c);
    }

    @Test
    public void toStringContainsValues() {
        HorizontalCoordinate c = new HorizontalCoordinate(90.0, 45.0);
        String s = c.toString();
        assertTrue(s.contains("90.0"));
        assertTrue(s.contains("45.0"));
    }
}