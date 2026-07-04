package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class CameraOrientationTest {

    private static final double DELTA = 1e-12;

    @Test
    public void constructorStoresFields() {
        CameraOrientation o = new CameraOrientation(45.0, 30.0, 10.0);
        assertEquals(45.0, o.getYawDegrees(), DELTA);
        assertEquals(30.0, o.getPitchDegrees(), DELTA);
        assertEquals(10.0, o.getRollDegrees(), DELTA);
    }

    @Test
    public void equalsAndHashCode() {
        CameraOrientation a = new CameraOrientation(45.0, 30.0, 10.0);
        CameraOrientation b = new CameraOrientation(45.0, 30.0, 10.0);
        CameraOrientation c = new CameraOrientation(90.0, 30.0, 10.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("string", a);
    }

    @Test
    public void toStringContainsFields() {
        CameraOrientation o = new CameraOrientation(45.0, 30.0, 10.0);
        String s = o.toString();
        assertTrue(s.contains("yaw="));
        assertTrue(s.contains("pitch="));
        assertTrue(s.contains("roll="));
    }

    @Test
    public void sameRefIsEqual() {
        CameraOrientation o = new CameraOrientation(0, 0, 0);
        assertEquals(o, o);
    }
}
