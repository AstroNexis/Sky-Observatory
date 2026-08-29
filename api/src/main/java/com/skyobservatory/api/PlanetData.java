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
 * <p>Deprecated. All fields are now available directly on {@link CelestialObject}.
 * Use {@link CelestialObject#getEquatorialRadiusKm()}, {@link CelestialObject#getMeanRadiusKm()},
 * {@link CelestialObject#getDiameterKm()}, {@link CelestialObject#getRenderRadius()}, and
 * {@link CelestialObject#hasRings()} instead.</p>
 *
 * @deprecated Use {@link CelestialObject} directly. This class is retained for
 *             backward compatibility and delegates to the catalog entries.
 */
@Deprecated
public final class PlanetData {

    /** Filename of Saturn's ring texture inside {@code assets/}. */
    @Deprecated public static final String SATURN_RING_TEXTURE = CelestialObject.SATURN_RING_TEXTURE;

    private final CelestialObject celestial;

    private PlanetData(CelestialObject celestial) {
        this.celestial = celestial;
    }

    @Deprecated public int getNaifId()                          { return celestial.getNaifId(); }
    @Deprecated public String getName()                         { return celestial.getName(); }
    @Deprecated public String getTextureFile()                  { return celestial.getAssetName(); }
    @Deprecated public double getEquatorialRadiusKm()           { return celestial.getEquatorialRadiusKm(); }
    @Deprecated public double getMeanRadiusKm()                 { return celestial.getMeanRadiusKm(); }
    @Deprecated public double getDiameterKm()                   { return celestial.getDiameterKm(); }
    @Deprecated public float getRenderScale()                   { return celestial.getRenderRadius(); }
    @Deprecated public boolean hasRings()                       { return celestial.hasRings(); }

    // Pre-built entries (delegating to CelestialObject.CATALOG)

    @Deprecated public static final PlanetData SUN     = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_SUN));
    @Deprecated public static final PlanetData MERCURY = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_MERCURY));
    @Deprecated public static final PlanetData VENUS   = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_VENUS));
    @Deprecated public static final PlanetData MOON    = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_MOON));
    @Deprecated public static final PlanetData MARS    = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_MARS));
    @Deprecated public static final PlanetData JUPITER = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_JUPITER));
    @Deprecated public static final PlanetData SATURN  = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_SATURN));
    @Deprecated public static final PlanetData URANUS  = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_URANUS));
    @Deprecated public static final PlanetData NEPTUNE = new PlanetData(CelestialObject.CATALOG.get(CelestialObject.NAIF_NEPTUNE));

    private static final java.util.Map<Integer, PlanetData> ALL;
    static {
        java.util.Map<Integer, PlanetData> m = new java.util.LinkedHashMap<>();
        for (CelestialObject co : CelestialObject.CATALOG.values()) {
            m.put(co.getNaifId(), new PlanetData(co));
        }
        ALL = java.util.Collections.unmodifiableMap(m);
    }

    @Deprecated
    public static PlanetData fromNaifId(int naifId) {
        return ALL.get(naifId);
    }

    @Deprecated
    public static final class Builder {
        private final int naifId;
        private final String name;

        @Deprecated public Builder(int naifId, String name) {
            this.naifId = naifId;
            this.name = name;
        }

        @Deprecated public Builder textureFile(String textureFile) { return this; }
        @Deprecated public Builder equatorialRadiusKm(double equatorialRadiusKm) { return this; }
        @Deprecated public Builder meanRadiusKm(double meanRadiusKm) { return this; }
        @Deprecated public Builder renderScale(float renderScale) { return this; }

        @Deprecated
        public PlanetData build() {
            CelestialObject co = CelestialObject.fromNaifId(naifId);
            if (co != null) return new PlanetData(co);
            // Return a minimal fallback for custom entries
            return new PlanetData(new CelestialObject(naifId, name));
        }
    }

    @Override
    @Deprecated
    public String toString() {
        return "PlanetData{name=" + celestial.getName() + ", naifId=" + celestial.getNaifId() + "}";
    }
}