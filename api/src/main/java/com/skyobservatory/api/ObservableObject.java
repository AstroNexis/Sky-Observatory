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
 * An observed celestial object with its computed position and visibility state.
 *
 * <p>{@link ObservableObject} bundles the object identity, its horizontal
 * coordinates, Cartesian position, and visibility together so renderers
 * receive a self-contained entry for each object in the sky.</p>
 *
 * <p>The object type covers the full spectrum of celestial targets:
 * solar system bodies, stars, deep sky objects, and satellites. The
 * {@link #getCategory()} discriminator allows renderers to apply different
 * visual treatments per category.</p>
 *
 * <p>Instances are immutable.</p>
 */
public final class ObservableObject {

    private final CelestialObject target;
    private final SkyCoordinate position;
    private final VisibilityState visibility;
    private final ObjectCategory category;

    /**
     * Broad category of a celestial object for rendering differentiation.
     */
    public enum ObjectCategory {
        STAR,
        PLANET,
        MOON,
        SATELLITE,
        DEEP_SKY,
        CONSTELLATION,
        SOLAR_SYSTEM_BODY,
        UNKNOWN
    }

    /**
     * @param target     the celestial object identity
     * @param position   the computed sky position (horizontal and Cartesian)
     * @param visibility the visibility state relative to the horizon
     * @param category   the type category for rendering
     */
    public ObservableObject(
            CelestialObject target,
            SkyCoordinate position,
            VisibilityState visibility,
            ObjectCategory category) {
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (position == null) throw new IllegalArgumentException("position must not be null");
        if (visibility == null) throw new IllegalArgumentException("visibility must not be null");
        if (category == null) throw new IllegalArgumentException("category must not be null");
        this.target = target;
        this.position = position;
        this.visibility = visibility;
        this.category = category;
    }

    /** Returns the celestial object identity. */
    public CelestialObject getTarget() { return target; }

    /** Returns the computed sky position with both horizontal and Cartesian representations. */
    public SkyCoordinate getPosition() { return position; }

    /** Returns the visibility state relative to the horizon. */
    public VisibilityState getVisibility() { return visibility; }

    /** Returns the object category for rendering differentiation. */
    public ObjectCategory getCategory() { return category; }

    /** Convenience: returns the azimuth in degrees. */
    public double getAzimuthDegrees() { return position.getAzimuthDegrees(); }

    /** Convenience: returns the altitude in degrees. */
    public double getAltitudeDegrees() { return position.getAltitudeDegrees(); }

    /** Convenience: returns the Cartesian x component. */
    public double getX() { return position.getX(); }

    /** Convenience: returns the Cartesian y component. */
    public double getY() { return position.getY(); }

    /** Convenience: returns the Cartesian z component. */
    public double getZ() { return position.getZ(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ObservableObject)) return false;
        ObservableObject other = (ObservableObject) obj;
        return target.equals(other.target)
                && position.equals(other.position)
                && visibility == other.visibility
                && category == other.category;
    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + visibility.hashCode();
        result = 31 * result + category.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ObservableObject{target=" + target.getName()
                + ", category=" + category
                + ", visibility=" + visibility
                + ", az=" + position.getAzimuthDegrees()
                + "\u00b0, alt=" + position.getAltitudeDegrees() + "\u00b0}";
    }
}
