package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class EphemerisResultTest {

    private static final double DELTA = 1e-12;

    @Test
    public void emptyBuilderReturnsAllFieldsAbsent() {
        EphemerisResult r = new EphemerisResult.Builder().build();
        assertNull(r.getRiseTimeJd());
        assertNull(r.getSetTimeJd());
        assertNull(r.getVisualMagnitude());
        assertNull(r.getApparentDiameterArcmin());
        assertFalse(r.hasRiseTime());
        assertFalse(r.hasSetTime());
        assertFalse(r.hasVisualMagnitude());
        assertFalse(r.hasApparentDiameter());
    }

    @Test
    public void fullyPopulatedBuilder() {
        EphemerisResult r = new EphemerisResult.Builder()
                .riseTimeJd(2460000.5)
                .setTimeJd(2460000.8)
                .visualMagnitude(-26.74)
                .apparentDiameterArcmin(31.5)
                .build();

        assertEquals(2460000.5, r.getRiseTimeJd(), DELTA);
        assertEquals(2460000.8, r.getSetTimeJd(), DELTA);
        assertEquals(-26.74, r.getVisualMagnitude(), DELTA);
        assertEquals(31.5, r.getApparentDiameterArcmin(), DELTA);
        assertTrue(r.hasRiseTime());
        assertTrue(r.hasSetTime());
        assertTrue(r.hasVisualMagnitude());
        assertTrue(r.hasApparentDiameter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeDiameterThrows() {
        new EphemerisResult.Builder().apparentDiameterArcmin(-1.0);
    }

    @Test
    public void builderReturnsSelfForChaining() {
        EphemerisResult.Builder b = new EphemerisResult.Builder();
        assertSame(b, b.riseTimeJd(1.0));
        assertSame(b, b.setTimeJd(1.0));
        assertSame(b, b.visualMagnitude(1.0));
        assertSame(b, b.apparentDiameterArcmin(1.0));
    }

    @Test
    public void toStringContainsFields() {
        EphemerisResult r = new EphemerisResult.Builder()
                .riseTimeJd(1.0).setTimeJd(2.0)
                .visualMagnitude(-10.0).apparentDiameterArcmin(30.0)
                .build();
        String s = r.toString();
        assertTrue(s.contains("rise="));
        assertTrue(s.contains("set="));
        assertTrue(s.contains("mag="));
        assertTrue(s.contains("diam="));
    }
}
