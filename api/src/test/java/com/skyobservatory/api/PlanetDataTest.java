package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlanetDataTest {

    private static final double DELTA = 1e-12;

    @Test
    public void sunHasCorrectConstants() {
        PlanetData d = PlanetData.SUN;
        assertEquals(CelestialObject.NAIF_SUN, d.getNaifId());
        assertEquals("Sun", d.getName());
        assertEquals("sun.jpg", d.getTextureFile());
        assertEquals(695700.0, d.getEquatorialRadiusKm(), DELTA);
        assertEquals(695700.0, d.getMeanRadiusKm(), DELTA);
        assertEquals(695700.0 * 2.0, d.getDiameterKm(), DELTA);
        assertEquals(0.80f, d.getRenderScale(), 0f);
        assertFalse(d.hasRings());
    }

    @Test
    public void mercuryHasTexture() {
        PlanetData d = PlanetData.MERCURY;
        assertEquals(CelestialObject.NAIF_MERCURY, d.getNaifId());
        assertEquals("mercury.jpg", d.getTextureFile());
        assertEquals(2440.53, d.getEquatorialRadiusKm(), DELTA);
        assertEquals(2439.7, d.getMeanRadiusKm(), DELTA);
    }

    @Test
    public void venusHasTexture() {
        PlanetData d = PlanetData.VENUS;
        assertEquals(CelestialObject.NAIF_VENUS, d.getNaifId());
        assertEquals("venus.jpg", d.getTextureFile());
    }

    @Test
    public void marsHasTexture() {
        PlanetData d = PlanetData.MARS;
        assertEquals(CelestialObject.NAIF_MARS, d.getNaifId());
        assertEquals("mars.jpg", d.getTextureFile());
    }

    @Test
    public void jupiterHasTexture() {
        PlanetData d = PlanetData.JUPITER;
        assertEquals(CelestialObject.NAIF_JUPITER, d.getNaifId());
        assertEquals("jupiter.jpg", d.getTextureFile());
    }

    @Test
    public void saturnHasTextureAndRings() {
        PlanetData d = PlanetData.SATURN;
        assertEquals(CelestialObject.NAIF_SATURN, d.getNaifId());
        assertEquals("saturn.jpg", d.getTextureFile());
        assertTrue(d.hasRings());
        assertEquals("saturn_ring.png", PlanetData.SATURN_RING_TEXTURE);
    }

    @Test
    public void uranusAndNeptuneHaveNoTexture() {
        assertNull(PlanetData.URANUS.getTextureFile());
        assertNull(PlanetData.NEPTUNE.getTextureFile());
        assertFalse(PlanetData.URANUS.hasRings());
        assertFalse(PlanetData.NEPTUNE.hasRings());
    }

    @Test
    public void fromNaifIdReturnsCorrectEntry() {
        assertSame(PlanetData.SUN, PlanetData.fromNaifId(CelestialObject.NAIF_SUN));
        assertSame(PlanetData.MOON, PlanetData.fromNaifId(CelestialObject.NAIF_MOON));
        assertSame(PlanetData.MERCURY, PlanetData.fromNaifId(CelestialObject.NAIF_MERCURY));
        assertSame(PlanetData.VENUS, PlanetData.fromNaifId(CelestialObject.NAIF_VENUS));
        assertSame(PlanetData.MARS, PlanetData.fromNaifId(CelestialObject.NAIF_MARS));
        assertSame(PlanetData.JUPITER, PlanetData.fromNaifId(CelestialObject.NAIF_JUPITER));
        assertSame(PlanetData.SATURN, PlanetData.fromNaifId(CelestialObject.NAIF_SATURN));
        assertSame(PlanetData.URANUS, PlanetData.fromNaifId(CelestialObject.NAIF_URANUS));
        assertSame(PlanetData.NEPTUNE, PlanetData.fromNaifId(CelestialObject.NAIF_NEPTUNE));
    }

    @Test
    public void fromNaifIdReturnsNullForUnknown() {
        assertNull(PlanetData.fromNaifId(9999999));
    }

    @Test
    public void equalsAndHashCode() {
        PlanetData a = PlanetData.SUN;
        PlanetData b = PlanetData.fromNaifId(CelestialObject.NAIF_SUN);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(PlanetData.SUN, PlanetData.MOON);
        assertNotEquals(null, a);
        assertNotEquals("string", a);
    }

    @Test
    public void builderProducesCustomEntry() {
        PlanetData d = new PlanetData.Builder(42, "Custom")
                .textureFile("custom.jpg")
                .equatorialRadiusKm(1000.0)
                .meanRadiusKm(950.0)
                .renderScale(0.5f)
                .build();

        assertEquals(42, d.getNaifId());
        assertEquals("Custom", d.getName());
        assertEquals("custom.jpg", d.getTextureFile());
        assertEquals(1000.0, d.getEquatorialRadiusKm(), DELTA);
        assertEquals(950.0, d.getMeanRadiusKm(), DELTA);
        assertEquals(2000.0, d.getDiameterKm(), DELTA);
        assertEquals(0.5f, d.getRenderScale(), 0f);
        assertFalse(d.hasRings());
    }

    @Test
    public void builderReturnsSelfForChaining() {
        PlanetData.Builder b = new PlanetData.Builder(1, "Test");
        assertSame(b, b.textureFile("a.jpg"));
        assertSame(b, b.equatorialRadiusKm(1.0));
        assertSame(b, b.meanRadiusKm(1.0));
        assertSame(b, b.renderScale(1.0f));
    }

    @Test
    public void toStringContainsFields() {
        String s = PlanetData.SUN.toString();
        assertTrue(s.contains("Sun"));
        assertTrue(s.contains("naifId=" + CelestialObject.NAIF_SUN));
        assertTrue(s.contains("eqRadius="));
        assertTrue(s.contains("meanRadius="));
        assertTrue(s.contains("scale="));
    }

    @Test
    public void moonConstants() {
        PlanetData d = PlanetData.MOON;
        assertEquals(CelestialObject.NAIF_MOON, d.getNaifId());
        assertEquals("Moon", d.getName());
        assertEquals("moon.jpg", d.getTextureFile());
        assertEquals(1738.1, d.getEquatorialRadiusKm(), DELTA);
        assertEquals(1737.4, d.getMeanRadiusKm(), DELTA);
        assertFalse(d.hasRings());
    }
}