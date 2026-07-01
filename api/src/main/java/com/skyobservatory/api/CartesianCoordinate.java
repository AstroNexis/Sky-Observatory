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
 * An immutable Cartesian coordinate in the local topocentric frame suitable for
 * 3D rendering pipelines.
 *
 * <p>Coordinate system conventions (right-handed):</p>
 * <ul>
 *   <li><b>X</b>: east-west axis. Positive = east, negative = west.</li>
 *   <li><b>Y</b>: up-down axis. Positive = up (zenith), negative = down (nadir).</li>
 *   <li><b>Z</b>: north-south axis. Positive = south, negative = north.</li>
 * </ul>
 *
 * This frame is derived from the standard East-North-Up (ENU) system with
 * re-ordered axes (Y = up, Z = south) to match the convention used by common
 * rendering frameworks. The {@link #getNorthVector()} and {@link #getEastVector()}
 * methods provide cardinal direction vectors for manual transformations.
 *
 * <p>Coordinates are dimensionless. When used as a direction vector the length
 * is typically 1.0 (unit sphere). Callers working with physical distances should
 * scale by the appropriate radius or distance value.</p>
 *
 * <p>This type is renderer agnostic and compatible with OpenGL ES, Vulkan,
 * Filament, Sceneform, Unity, and custom rendering pipelines.</p>
 */
public final class CartesianCoordinate {

    private final Vector3 vector;

    /** The unit vector pointing east (+X). */
    public static final CartesianCoordinate EAST =
            new CartesianCoordinate(new Vector3(1.0, 0.0, 0.0));

    /** The unit vector pointing west (-X). */
    public static final CartesianCoordinate WEST =
            new CartesianCoordinate(new Vector3(-1.0, 0.0, 0.0));

    /** The unit vector pointing up / zenith (+Y). */
    public static final CartesianCoordinate UP =
            new CartesianCoordinate(new Vector3(0.0, 1.0, 0.0));

    /** The unit vector pointing down / nadir (-Y). */
    public static final CartesianCoordinate DOWN =
            new CartesianCoordinate(new Vector3(0.0, -1.0, 0.0));

    /** The unit vector pointing south (+Z). */
    public static final CartesianCoordinate SOUTH =
            new CartesianCoordinate(new Vector3(0.0, 0.0, 1.0));

    /** The unit vector pointing north (-Z). */
    public static final CartesianCoordinate NORTH =
            new CartesianCoordinate(new Vector3(0.0, 0.0, -1.0));

    /** The origin (center of the unit sphere). */
    public static final CartesianCoordinate ZERO =
            new CartesianCoordinate(Vector3.ZERO);

    /**
     * @param x the east-west component (positive = east)
     * @param y the up-down component (positive = up)
     * @param z the north-south component (positive = south)
     * @throws IllegalArgumentException if any component is NaN or infinite
     */
    public CartesianCoordinate(double x, double y, double z) {
        this(new Vector3(x, y, z));
    }

    /**
     * Wraps an existing {@link Vector3} as a CartesianCoordinate.
     *
     * @param vector the underlying vector; must not be null
     */
    public CartesianCoordinate(Vector3 vector) {
        if (vector == null) throw new IllegalArgumentException("vector must not be null");
        this.vector = vector;
    }

    /** Returns the east-west component (positive = east). */
    public double getX() { return vector.getX(); }

    /** Returns the up-down component (positive = up). */
    public double getY() { return vector.getY(); }

    /** Returns the north-south component (positive = south). */
    public double getZ() { return vector.getZ(); }

    /** Returns the underlying {@link Vector3}. */
    public Vector3 toVector3() { return vector; }

    /**
     * Returns the Euclidean magnitude (length) of this coordinate.
     *
     * @return {@code sqrt(x² + y² + z²)}
     */
    public double magnitude() { return vector.magnitude(); }

    /**
     * Returns a normalized (unit-length) copy of this coordinate.
     *
     * @return unit vector pointing in the same direction
     * @throws IllegalStateException if this coordinate has zero length
     */
    public CartesianCoordinate normalize() {
        double m = magnitude();
        if (m == 0.0) throw new IllegalStateException("Cannot normalize a zero-length coordinate");
        return scale(1.0 / m);
    }

    /**
     * Returns a new coordinate scaled by the given factor.
     *
     * @param factor the scalar multiplier
     * @return scaled coordinate
     */
    public CartesianCoordinate scale(double factor) {
        return new CartesianCoordinate(vector.scale(factor));
    }

    /**
     * Returns the dot product with another coordinate.
     *
     * @param other the second operand
     * @return scalar dot product
     */
    public double dot(CartesianCoordinate other) {
        return vector.dot(other.vector);
    }

    /**
     * Returns the vector sum of this coordinate and another.
     *
     * @param other the coordinate to add
     * @return component-wise sum
     */
    public CartesianCoordinate add(CartesianCoordinate other) {
        return new CartesianCoordinate(vector.add(other.vector));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CartesianCoordinate)) return false;
        return vector.equals(((CartesianCoordinate) obj).vector);
    }

    @Override
    public int hashCode() {
        return vector.hashCode();
    }

    @Override
    public String toString() {
        return "CartesianCoordinate{x=" + vector.getX()
                + ", y=" + vector.getY()
                + ", z=" + vector.getZ() + "}";
    }
}
