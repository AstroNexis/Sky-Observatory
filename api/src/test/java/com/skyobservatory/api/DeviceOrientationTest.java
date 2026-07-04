package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceOrientationTest {

    private static final double DELTA = 1e-12;
    private static final CartesianCoordinate FORWARD =
            new CartesianCoordinate(0.0, 0.0, -1.0);
    private static final CartesianCoordinate UP =
            new CartesianCoordinate(0.0, 1.0, 0.0);

    @Test(expected = IllegalArgumentException.class)
    public void nullForwardThrows() {
        new DeviceOrientation(null, UP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUpThrows() {
        new DeviceOrientation(FORWARD, null);
    }

    @Test
    public void storesFields() {
        DeviceOrientation d = new DeviceOrientation(FORWARD, UP);
        assertSame(FORWARD, d.getForward());
        assertSame(UP, d.getUp());
    }

    @Test
    public void equalsAndHashCode() {
        DeviceOrientation a = new DeviceOrientation(FORWARD, UP);
        DeviceOrientation b = new DeviceOrientation(FORWARD, UP);
        DeviceOrientation c = new DeviceOrientation(
                new CartesianCoordinate(1.0, 0.0, 0.0), UP);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("string", a);
    }

    @Test
    public void sameRefIsEqual() {
        DeviceOrientation d = new DeviceOrientation(FORWARD, UP);
        assertEquals(d, d);
    }

    @Test
    public void toStringContainsFields() {
        DeviceOrientation d = new DeviceOrientation(FORWARD, UP);
        String s = d.toString();
        assertTrue(s.contains("forward="));
        assertTrue(s.contains("up="));
    }
}
