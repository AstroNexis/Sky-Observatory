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
 * Identifies a celestial object that can be observed or tracked.
 *
 * For Phase 1, this covers solar system bodies by their NAIF integer IDs,
 * which map directly to the identifiers used by SuperNOVAS internally.
 * Catalog-based objects (stars, DSOs) are reserved for a future phase.
 */
public final class CelestialObject {

    /**
     * NAIF body IDs for well-known solar system objects.
     * These match the integer values expected by the SuperNOVAS C library.
     */
    public static final int NAIF_SUN = 10;
    public static final int NAIF_MOON = 301;
    public static final int NAIF_MERCURY = 199;
    public static final int NAIF_VENUS = 299;
    public static final int NAIF_EARTH = 399;
    public static final int NAIF_MARS = 499;
    public static final int NAIF_JUPITER = 599;
    public static final int NAIF_SATURN = 699;
    public static final int NAIF_URANUS = 799;
    public static final int NAIF_NEPTUNE = 899;

    private final int naifId;
    private final String name;

    /**
     * @param naifId NAIF integer identifier for this body
     * @param name   human-readable name, used for display only
     */
    public CelestialObject(int naifId, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("CelestialObject name must not be null or empty");
        }
        this.naifId = naifId;
        this.name = name;
    }

    public int getNaifId() {
        return naifId;
    }

    public String getName() {
        return name;
    }

    // Convenience factory methods for the major solar system bodies.

    public static CelestialObject sun() {
        return new CelestialObject(NAIF_SUN, "Sun");
    }

    public static CelestialObject moon() {
        return new CelestialObject(NAIF_MOON, "Moon");
    }

    public static CelestialObject mars() {
        return new CelestialObject(NAIF_MARS, "Mars");
    }

    @Override
    public String toString() {
        return "CelestialObject{name=" + name + ", naifId=" + naifId + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CelestialObject)) return false;
        CelestialObject that = (CelestialObject) other;
        return naifId == that.naifId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(naifId);
    }
}
