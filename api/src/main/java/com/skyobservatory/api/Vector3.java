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
 * An immutable three-dimensional Cartesian vector.
 *
 * Used throughout the SDK to represent positions, directions, and velocities in
 * various coordinate frames (ICRS, GCRS, topocentric). The coordinate frame is
 * determined by context -- callers must document which frame a given {@code Vector3}
 * occupies.
 *
 * Units are also context-dependent. Positional vectors are typically in AU;
 * unit direction vectors are dimensionless with magnitude 1.
 *
 * This type is the foundational geometric primitive for the OpenGL and AR layers
 * planned in later phases.
 */
public final class Vector3 {

    /** The zero vector (0, 0, 0). */
    public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);

    private final double x;
    private final double y;
    private final double z;

    /**
     * @param x the x component
     * @param y the y component
     * @param z the z component
     * @throws IllegalArgumentException if any component is NaN or infinite
     */
    public Vector3(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException(
                    "Vector3 components must be finite. Got: (" + x + ", " + y + ", " + z + ")");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Returns the x component. */
    public double getX() { return x; }

    /** Returns the y component. */
    public double getY() { return y; }

    /** Returns the z component. */
    public double getZ() { return z; }

    /**
     * Returns the Euclidean magnitude (length) of this vector.
     *
     * @return {@code sqrt(x² + y² + z²)}
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns a new vector that is this vector scaled by the given factor.
     *
     * @param factor the scalar multiplier
     * @return scaled vector
     * @throws IllegalArgumentException if the result contains non-finite values
     */
    public Vector3 scale(double factor) {
        return new Vector3(x * factor, y * factor, z * factor);
    }

    /**
     * Returns the dot product of this vector and {@code other}.
     *
     * @param other the second operand
     * @return scalar dot product
     */
    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Returns the vector sum of this vector and {@code other}.
     *
     * @param other the vector to add
     * @return component-wise sum
     */
    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector3)) return false;
        Vector3 other = (Vector3) obj;
        return Double.compare(x, other.x) == 0
                && Double.compare(y, other.y) == 0
                && Double.compare(z, other.z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }

    @Override
    public String toString() {
        return "Vector3(" + x + ", " + y + ", " + z + ")";
    }
}
