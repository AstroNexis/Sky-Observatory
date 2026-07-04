package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class CartesianCoordinateTest {

    private static final double DELTA = 1e-12;

    @Test
    public void constructorStoresComponents() {
        CartesianCoordinate c = new CartesianCoordinate(1.0, 2.0, 3.0);
        assertEquals(1.0, c.getX(), DELTA);
        assertEquals(2.0, c.getY(), DELTA);
        assertEquals(3.0, c.getZ(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullVectorThrows() {
        new CartesianCoordinate((Vector3) null);
    }

    @Test
    public void magnitude() {
        CartesianCoordinate c = new CartesianCoordinate(3.0, 4.0, 0.0);
        assertEquals(5.0, c.magnitude(), DELTA);
    }

    @Test
    public void normalizeUnitVector() {
        CartesianCoordinate c = new CartesianCoordinate(3.0, 0.0, 0.0);
        CartesianCoordinate n = c.normalize();
        assertEquals(1.0, n.getX(), DELTA);
        assertEquals(0.0, n.getY(), DELTA);
        assertEquals(0.0, n.getZ(), DELTA);
    }

    @Test(expected = IllegalStateException.class)
    public void normalizeZeroThrows() {
        CartesianCoordinate.ZERO.normalize();
    }

    @Test
    public void scale() {
        CartesianCoordinate c = new CartesianCoordinate(1.0, 2.0, 3.0);
        CartesianCoordinate s = c.scale(2.0);
        assertEquals(2.0, s.getX(), DELTA);
        assertEquals(4.0, s.getY(), DELTA);
        assertEquals(6.0, s.getZ(), DELTA);
    }

    @Test
    public void dotProduct() {
        CartesianCoordinate a = new CartesianCoordinate(1.0, 0.0, 0.0);
        CartesianCoordinate b = new CartesianCoordinate(0.0, 1.0, 0.0);
        assertEquals(0.0, a.dot(b), DELTA);
        assertEquals(1.0, a.dot(a), DELTA);
    }

    @Test
    public void add() {
        CartesianCoordinate a = new CartesianCoordinate(1.0, 2.0, 3.0);
        CartesianCoordinate b = new CartesianCoordinate(4.0, 5.0, 6.0);
        CartesianCoordinate s = a.add(b);
        assertEquals(5.0, s.getX(), DELTA);
        assertEquals(7.0, s.getY(), DELTA);
        assertEquals(9.0, s.getZ(), DELTA);
    }

    @Test
    public void constants() {
        assertEquals(1.0, CartesianCoordinate.EAST.getX(), DELTA);
        assertEquals(0.0, CartesianCoordinate.EAST.getY(), DELTA);
        assertEquals(0.0, CartesianCoordinate.EAST.getZ(), DELTA);

        assertEquals(0.0, CartesianCoordinate.UP.getX(), DELTA);
        assertEquals(1.0, CartesianCoordinate.UP.getY(), DELTA);
        assertEquals(0.0, CartesianCoordinate.UP.getZ(), DELTA);

        assertEquals(0.0, CartesianCoordinate.NORTH.getX(), DELTA);
        assertEquals(0.0, CartesianCoordinate.NORTH.getY(), DELTA);
        assertEquals(-1.0, CartesianCoordinate.NORTH.getZ(), DELTA);
    }

    @Test
    public void equalsAndHashCode() {
        CartesianCoordinate a = new CartesianCoordinate(1.0, 2.0, 3.0);
        CartesianCoordinate b = new CartesianCoordinate(1.0, 2.0, 3.0);
        CartesianCoordinate c = new CartesianCoordinate(1.0, 0.0, 0.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("string", a);
    }

    @Test
    public void toVector3ReturnsSameData() {
        CartesianCoordinate c = new CartesianCoordinate(1.0, 2.0, 3.0);
        Vector3 v = c.toVector3();
        assertEquals(1.0, v.getX(), DELTA);
        assertEquals(2.0, v.getY(), DELTA);
        assertEquals(3.0, v.getZ(), DELTA);
    }

    @Test
    public void toStringContainsFields() {
        CartesianCoordinate c = new CartesianCoordinate(1.0, 2.0, 3.0);
        String s = c.toString();
        assertTrue(s.contains("x="));
        assertTrue(s.contains("y="));
        assertTrue(s.contains("z="));
    }
}
