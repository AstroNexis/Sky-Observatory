package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class SkyCoordinateTest {

    private static final double DELTA = 1e-12;
    private static final HorizontalCoordinate HORIZ =
            new HorizontalCoordinate(180.0, 45.0);
    private static final CartesianCoordinate CART =
            new CartesianCoordinate(0.0, 0.7071, -0.7071);

    @Test(expected = IllegalArgumentException.class)
    public void nullHorizontalThrows() {
        new SkyCoordinate(null, CART);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullCartesianThrows() {
        new SkyCoordinate(HORIZ, null);
    }

    @Test
    public void storesFields() {
        SkyCoordinate c = new SkyCoordinate(HORIZ, CART);
        assertSame(HORIZ, c.getHorizontal());
        assertSame(CART, c.getCartesian());
    }

    @Test
    public void convenienceMethods() {
        SkyCoordinate c = new SkyCoordinate(HORIZ, CART);
        assertEquals(180.0, c.getAzimuthDegrees(), DELTA);
        assertEquals(45.0, c.getAltitudeDegrees(), DELTA);
        assertEquals(0.0, c.getX(), DELTA);
        assertEquals(0.7071, c.getY(), DELTA);
        assertEquals(-0.7071, c.getZ(), DELTA);
    }

    @Test
    public void equalsAndHashCode() {
        SkyCoordinate a = new SkyCoordinate(HORIZ, CART);
        SkyCoordinate b = new SkyCoordinate(HORIZ, CART);
        CartesianCoordinate other = new CartesianCoordinate(0.0, 1.0, 0.0);
        SkyCoordinate c = new SkyCoordinate(HORIZ, other);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    public void sameRefIsEqual() {
        SkyCoordinate c = new SkyCoordinate(HORIZ, CART);
        assertEquals(c, c);
    }

    @Test
    public void toStringContainsFields() {
        SkyCoordinate c = new SkyCoordinate(HORIZ, CART);
        String s = c.toString();
        assertTrue(s.contains("az="));
        assertTrue(s.contains("alt="));
        assertTrue(s.contains("x="));
        assertTrue(s.contains("y="));
        assertTrue(s.contains("z="));
    }
}
