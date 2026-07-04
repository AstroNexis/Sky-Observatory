package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class ViewportTest {

    private static final double DELTA = 1e-12;
    private static final CameraOrientation ORIENTATION =
            new CameraOrientation(45.0, 30.0, 0.0);

    @Test
    public void constructorStoresFields() {
        Viewport v = new Viewport(90.0, 60.0, ORIENTATION);
        assertEquals(90.0, v.getHorizontalFieldOfViewDegrees(), DELTA);
        assertEquals(60.0, v.getVerticalFieldOfViewDegrees(), DELTA);
        assertSame(ORIENTATION, v.getOrientation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullOrientationThrows() {
        new Viewport(90.0, 60.0, null);
    }

    @Test
    public void halfAngleDegrees() {
        Viewport v = new Viewport(90.0, 60.0, ORIENTATION);
        assertEquals(45.0, v.getHorizontalHalfAngleDegrees(), DELTA);
        assertEquals(30.0, v.getVerticalHalfAngleDegrees(), DELTA);
    }

    @Test
    public void equalsAndHashCode() {
        Viewport a = new Viewport(90.0, 60.0, ORIENTATION);
        Viewport b = new Viewport(90.0, 60.0, ORIENTATION);
        Viewport c = new Viewport(100.0, 60.0, ORIENTATION);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("string", a);
    }

    @Test
    public void toStringContainsFields() {
        Viewport v = new Viewport(90.0, 60.0, ORIENTATION);
        assertTrue(v.toString().contains("hFov="));
        assertTrue(v.toString().contains("vFov="));
    }
}
