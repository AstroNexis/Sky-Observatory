/*
 * Copyright 2026 Phuc An <pan2512811@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skyobservatory.api;

/**
 * Physical data for solar system bodies, sourced from IAU 2015 Resolution B3.
 *
 * <p>Each entry maps to a {@link CelestialObject} via NAIF ID. The
 * {@link #getRenderScale()} value matches {@link CelestialObject#getRenderRadius()}
 * so that rendering pipelines can use either source interchangeably.</p>
 *
 * <p>Saturn includes a {@link #RING_TEXTURE} constant for the ring asset.</p>
 *
 * <p>Instances are immutable.</p>
 */
public final class PlanetData {

    /** Filename of Saturn's ring texture inside {@code assets/}. */
    public static final String SATURN_RING_TEXTURE = "saturn_ring.png";

    private final int naifId;
    private final String name;
    private final String textureFile;
    private final double equatorialRadiusKm;
    private final double meanRadiusKm;
    private final double diameterKm;
    private final float renderScale;

    private PlanetData(Builder b) {
        this.naifId           = b.naifId;
        this.name             = b.name;
        this.textureFile      = b.textureFile;
        this.equatorialRadiusKm = b.equatorialRadiusKm;
        this.meanRadiusKm     = b.meanRadiusKm;
        this.diameterKm       = b.equatorialRadiusKm * 2.0;
        this.renderScale      = b.renderScale;
    }

    public int getNaifId()                          { return naifId; }
    public String getName()                         { return name; }
    public String getTextureFile()                  { return textureFile; }
    public double getEquatorialRadiusKm()           { return equatorialRadiusKm; }
    public double getMeanRadiusKm()                 { return meanRadiusKm; }
    public double getDiameterKm()                   { return diameterKm; }
    public float getRenderScale()                   { return renderScale; }

    /** Returns {@code true} when this body has a ring texture. */
    public boolean hasRings() {
        return naifId == CelestialObject.NAIF_SATURN;
    }

    // -----------------------------------------------------------------------
    // Pre-built entries for every body in the CelestialObject catalog.
    // IAU 2015 Resolution B3 nominal radii (km).
    // Render scale matches CelestialObject.renderRadius.
    // -----------------------------------------------------------------------

    public static final PlanetData SUN = new Builder(CelestialObject.NAIF_SUN, "Sun")
            .textureFile("sun.jpg")
            .equatorialRadiusKm(695700.0)
            .meanRadiusKm(695700.0)
            .renderScale(0.75f)
            .build();

    public static final PlanetData MERCURY = new Builder(CelestialObject.NAIF_MERCURY, "Mercury")
            .textureFile("mercury.jpg")
            .equatorialRadiusKm(2440.53)
            .meanRadiusKm(2439.7)
            .renderScale(0.10f)
            .build();

    public static final PlanetData VENUS = new Builder(CelestialObject.NAIF_VENUS, "Venus")
            .textureFile("venus.jpg")
            .equatorialRadiusKm(6051.8)
            .meanRadiusKm(6051.8)
            .renderScale(0.15f)
            .build();

    public static final PlanetData MOON = new Builder(CelestialObject.NAIF_MOON, "Moon")
            .textureFile("moon.jpg")
            .equatorialRadiusKm(1738.1)
            .meanRadiusKm(1737.4)
            .renderScale(0.50f)
            .build();

    public static final PlanetData MARS = new Builder(CelestialObject.NAIF_MARS, "Mars")
            .textureFile("mars.jpg")
            .equatorialRadiusKm(3396.19)
            .meanRadiusKm(3389.5)
            .renderScale(0.12f)
            .build();

    public static final PlanetData JUPITER = new Builder(CelestialObject.NAIF_JUPITER, "Jupiter")
            .textureFile("jupiter.jpg")
            .equatorialRadiusKm(71492.0)
            .meanRadiusKm(69911.0)
            .renderScale(0.20f)
            .build();

    public static final PlanetData SATURN = new Builder(CelestialObject.NAIF_SATURN, "Saturn")
            .textureFile("saturn.jpg")
            .equatorialRadiusKm(60268.0)
            .meanRadiusKm(58232.0)
            .renderScale(0.18f)
            .build();

    public static final PlanetData URANUS = new Builder(CelestialObject.NAIF_URANUS, "Uranus")
            .textureFile(null)
            .equatorialRadiusKm(25559.0)
            .meanRadiusKm(25362.0)
            .renderScale(0.12f)
            .build();

    public static final PlanetData NEPTUNE = new Builder(CelestialObject.NAIF_NEPTUNE, "Neptune")
            .textureFile(null)
            .equatorialRadiusKm(24764.0)
            .meanRadiusKm(24622.0)
            .renderScale(0.12f)
            .build();

    /** All entries indexed by NAIF ID. */
    private static final java.util.Map<Integer, PlanetData> ALL;
    static {
        java.util.Map<Integer, PlanetData> m = new java.util.LinkedHashMap<>();
        m.put(SUN.naifId,     SUN);
        m.put(MERCURY.naifId, MERCURY);
        m.put(VENUS.naifId,   VENUS);
        m.put(MOON.naifId,    MOON);
        m.put(MARS.naifId,    MARS);
        m.put(JUPITER.naifId, JUPITER);
        m.put(SATURN.naifId,  SATURN);
        m.put(URANUS.naifId,  URANUS);
        m.put(NEPTUNE.naifId, NEPTUNE);
        ALL = java.util.Collections.unmodifiableMap(m);
    }

    /**
     * Returns the {@link PlanetData} for the given NAIF ID, or {@code null}
     * if no entry exists.
     */
    public static PlanetData fromNaifId(int naifId) {
        return ALL.get(naifId);
    }

    // Builder

    public static final class Builder {
        private final int naifId;
        private final String name;
        private String textureFile;
        private double equatorialRadiusKm;
        private double meanRadiusKm;
        private float renderScale;

        public Builder(int naifId, String name) {
            this.naifId = naifId;
            this.name = name;
        }

        public Builder textureFile(String textureFile) {
            this.textureFile = textureFile;
            return this;
        }

        public Builder equatorialRadiusKm(double equatorialRadiusKm) {
            this.equatorialRadiusKm = equatorialRadiusKm;
            return this;
        }

        public Builder meanRadiusKm(double meanRadiusKm) {
            this.meanRadiusKm = meanRadiusKm;
            return this;
        }

        public Builder renderScale(float renderScale) {
            this.renderScale = renderScale;
            return this;
        }

        public PlanetData build() {
            return new PlanetData(this);
        }
    }

    @Override
    public String toString() {
        return "PlanetData{name=" + name + ", naifId=" + naifId
                + ", eqRadius=" + equatorialRadiusKm + "km"
                + ", meanRadius=" + meanRadiusKm + "km"
                + ", scale=" + renderScale + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PlanetData)) return false;
        return naifId == ((PlanetData) other).naifId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(naifId);
    }
}
