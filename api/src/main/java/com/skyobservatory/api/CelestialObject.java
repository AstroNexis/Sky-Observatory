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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Identifies a celestial object that can be observed or tracked.
 *
 * <p>For Phase 1, this covers solar system bodies by their NAIF integer IDs,
 * which map directly to the identifiers used by SuperNOVAS internally.
 * Catalog-based objects (stars, DSOs) are reserved for a future phase.</p>
 *
 * <p>The built-in {@link #CATALOG} is the single source of truth for every
 * body the app knows about. To add a new object, add one entry to that map.
 * Nothing else needs to change.</p>
 */
public final class CelestialObject {

    /** Filename of Saturn's ring texture inside {@code assets/}. */
    public static final String SATURN_RING_TEXTURE = "saturn_ring.png";

    // NAIF body IDs for well-known solar system objects.
    public static final int NAIF_SUN     = 10;
    public static final int NAIF_MOON    = 301;
    public static final int NAIF_MERCURY = 199;
    public static final int NAIF_VENUS   = 299;
    public static final int NAIF_EARTH   = 399;
    public static final int NAIF_MARS    = 499;
    public static final int NAIF_JUPITER = 599;
    public static final int NAIF_SATURN  = 699;
    public static final int NAIF_URANUS  = 799;
    public static final int NAIF_NEPTUNE = 899;

    /**
     * All celestial objects the app can render, keyed by NAIF ID.
     *
     * This is the only place to add or remove a tracked body. Order is
     * preserved (insertion order); {@link #defaultTargets()} returns the
     * subset that is enabled by default.
     */
    public static final Map<Integer, CelestialObject> CATALOG;
    static {
        Map<Integer, CelestialObject> m = new LinkedHashMap<>();
        //                naifId         name        assetName    category                          radius  defaultOn  equRadiusKm  meanRadiusKm  rings
        m.put(NAIF_SUN,     new CelestialObject(NAIF_SUN,     "Sun",     "sun.jpg",     ObservableObject.ObjectCategory.SOLAR_SYSTEM_BODY, 0.80f, true,  695700.0,  695700.0,  false));
        m.put(NAIF_MOON,    new CelestialObject(NAIF_MOON,    "Moon",    "moon.jpg",    ObservableObject.ObjectCategory.MOON,              0.25f, true,  1738.1,   1737.4,    false));
        m.put(NAIF_MERCURY, new CelestialObject(NAIF_MERCURY, "Mercury", "mercury.jpg", ObservableObject.ObjectCategory.PLANET,            0.10f, true,  2440.53,  2439.7,    false));
        m.put(NAIF_VENUS,   new CelestialObject(NAIF_VENUS,   "Venus",   "venus.jpg",   ObservableObject.ObjectCategory.PLANET,            0.15f, true,  6051.8,   6051.8,    false));
        m.put(NAIF_MARS,    new CelestialObject(NAIF_MARS,    "Mars",    "mars.jpg",    ObservableObject.ObjectCategory.PLANET,            0.12f, true,  3396.19,  3389.5,    false));
        m.put(NAIF_JUPITER, new CelestialObject(NAIF_JUPITER, "Jupiter", "jupiter.jpg", ObservableObject.ObjectCategory.PLANET,            0.20f, true,  71492.0,  69911.0,   false));
        m.put(NAIF_SATURN,  new CelestialObject(NAIF_SATURN,  "Saturn",  "saturn.jpg",  ObservableObject.ObjectCategory.PLANET,            0.18f, true,  60268.0,  58232.0,   true));
        m.put(NAIF_URANUS,  new CelestialObject(NAIF_URANUS,  "Uranus",  null,          ObservableObject.ObjectCategory.PLANET,            0.12f, true,  25559.0,  25362.0,   false));
        m.put(NAIF_NEPTUNE, new CelestialObject(NAIF_NEPTUNE, "Neptune", null,          ObservableObject.ObjectCategory.PLANET,            0.12f, true,  24764.0,  24622.0,   false));
        CATALOG = Collections.unmodifiableMap(m);
    }

    /**
     * Returns the subset of {@link #CATALOG} that is tracked by default.
     *
     * The renderer and activity use this instead of hand-coding a list.
     */
    public static List<CelestialObject> defaultTargets() {
        List<CelestialObject> result = new ArrayList<>();
        for (CelestialObject obj : CATALOG.values()) {
            if (obj.enabledByDefault) result.add(obj);
        }
        return result;
    }

    /**
     * Returns the {@link CelestialObject} for the given NAIF ID, or
     * {@code null} if it is not in the catalog.
     */
    public static CelestialObject fromNaifId(int naifId) {
        return CATALOG.get(naifId);
    }

    // Instance fields

    private final int naifId;
    private final String name;
    /** Asset file name inside {@code assets/}, or {@code null} if none. */
    private final String assetName;
    private final ObservableObject.ObjectCategory category;
    /** Render sphere radius in world units. */
    private final float renderRadius;
    private final boolean enabledByDefault;
    /** Equatorial radius in km (IAU 2015 Resolution B3). */
    private final double equatorialRadiusKm;
    /** Mean radius in km (IAU 2015 Resolution B3). */
    private final double meanRadiusKm;
    /** Whether this body has a ring texture (e.g. Saturn). */
    private final boolean hasRings;

    public CelestialObject(
            int naifId,
            String name,
            String assetName,
            ObservableObject.ObjectCategory category,
            float renderRadius,
            boolean enabledByDefault,
            double equatorialRadiusKm,
            double meanRadiusKm,
            boolean hasRings) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("CelestialObject name must not be null or empty");
        }
        this.naifId           = naifId;
        this.name             = name;
        this.assetName        = assetName;
        this.category         = category;
        this.renderRadius     = renderRadius;
        this.enabledByDefault = enabledByDefault;
        this.equatorialRadiusKm = equatorialRadiusKm;
        this.meanRadiusKm     = meanRadiusKm;
        this.hasRings         = hasRings;
    }

    public CelestialObject(int naifId, String name) {
        this(naifId, name, null, ObservableObject.ObjectCategory.UNKNOWN, 0.15f, false, 0.0, 0.0, false);
    }

    public int getNaifId()                               { return naifId; }
    public String getName()                              { return name; }
    /** Asset file name inside {@code assets/}, or {@code null} if none. */
    public String getAssetName()                         { return assetName; }
    public ObservableObject.ObjectCategory getCategory() { return category; }
    public float getRenderRadius()                       { return renderRadius; }
    public boolean isEnabledByDefault()                  { return enabledByDefault; }
    /** Equatorial radius in km (IAU 2015 Resolution B3). */
    public double getEquatorialRadiusKm()                { return equatorialRadiusKm; }
    /** Mean radius in km (IAU 2015 Resolution B3). */
    public double getMeanRadiusKm()                      { return meanRadiusKm; }
    /** Diameter in km (2 x equatorial radius). */
    public double getDiameterKm()                        { return equatorialRadiusKm * 2.0; }
    /** Returns {@code true} when this body has a ring texture (e.g. Saturn). */
    public boolean hasRings()                            { return hasRings; }

    // Legacy convenience factories kept for API compatibility.

    public static CelestialObject sun()  { return CATALOG.get(NAIF_SUN); }
    public static CelestialObject moon() { return CATALOG.get(NAIF_MOON); }
    public static CelestialObject mars() { return CATALOG.get(NAIF_MARS); }

    @Override
    public String toString() {
        return "CelestialObject{name=" + name + ", naifId=" + naifId + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CelestialObject)) return false;
        return naifId == ((CelestialObject) other).naifId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(naifId);
    }
}
