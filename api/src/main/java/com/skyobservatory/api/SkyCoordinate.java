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
 * An immutable container pairing the horizontal and Cartesian representations
 * of a sky position.
 *
 * <p>{@link SkyCoordinate} unifies the two representations so that rendering
 * pipelines and visibility logic can share a single result object without
 * converting back and forth.</p>
 *
 * <p>Both representations always refer to the same sky position.
 * Instances are immutable.</p>
 */
public final class SkyCoordinate {

    private final HorizontalCoordinate horizontal;
    private final CartesianCoordinate cartesian;

    /**
     * @param horizontal the horizontal (alt-azimuth) representation
     * @param cartesian  the Cartesian representation in the topocentric frame
     */
    public SkyCoordinate(HorizontalCoordinate horizontal, CartesianCoordinate cartesian) {
        if (horizontal == null) throw new IllegalArgumentException("horizontal must not be null");
        if (cartesian == null) throw new IllegalArgumentException("cartesian must not be null");
        this.horizontal = horizontal;
        this.cartesian = cartesian;
    }

    /** Returns the horizontal (alt-azimuth) representation. */
    public HorizontalCoordinate getHorizontal() { return horizontal; }

    /** Returns the Cartesian representation in the topocentric frame. */
    public CartesianCoordinate getCartesian() { return cartesian; }

    /** Convenience: returns the azimuth in degrees. */
    public double getAzimuthDegrees() { return horizontal.getAzimuthDegrees(); }

    /** Convenience: returns the altitude in degrees. */
    public double getAltitudeDegrees() { return horizontal.getAltitudeDegrees(); }

    /** Convenience: returns the east-west component. */
    public double getX() { return cartesian.getX(); }

    /** Convenience: returns the up-down component. */
    public double getY() { return cartesian.getY(); }

    /** Convenience: returns the north-south component. */
    public double getZ() { return cartesian.getZ(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SkyCoordinate)) return false;
        SkyCoordinate other = (SkyCoordinate) obj;
        return horizontal.equals(other.horizontal) && cartesian.equals(other.cartesian);
    }

    @Override
    public int hashCode() {
        int result = horizontal.hashCode();
        result = 31 * result + cartesian.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SkyCoordinate{az=" + horizontal.getAzimuthDegrees()
                + "\u00b0, alt=" + horizontal.getAltitudeDegrees()
                + "\u00b0, x=" + cartesian.getX()
                + ", y=" + cartesian.getY()
                + ", z=" + cartesian.getZ() + "}";
    }
}
