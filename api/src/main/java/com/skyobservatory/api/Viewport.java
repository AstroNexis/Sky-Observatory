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
 * An immutable model of a camera viewport in the topocentric frame.
 *
 * <p>The viewport defines the viewing frustum: which portion of the sky is
 * visible through the camera. Together with the {@link CameraOrientation}
 * it forms the complete camera state needed for frustum culling.</p>
 *
 * <p>Field of view values are in degrees and refer to the camera's visible
 * angular extent. The horizontal FOV is typically wider than the vertical FOV
 * for landscape-oriented displays.</p>
 *
 * Instances are immutable.
 */
public final class Viewport {

    private final double horizontalFieldOfViewDegrees;
    private final double verticalFieldOfViewDegrees;
    private final CameraOrientation orientation;

    /**
     * @param horizontalFieldOfViewDegrees horizontal field of view in degrees (0, 180)
     * @param verticalFieldOfViewDegrees   vertical field of view in degrees (0, 180)
     * @param orientation                  the camera orientation; must not be null
     */
    public Viewport(
            double horizontalFieldOfViewDegrees,
            double verticalFieldOfViewDegrees,
            CameraOrientation orientation) {
        if (orientation == null) throw new IllegalArgumentException("orientation must not be null");
        this.horizontalFieldOfViewDegrees = horizontalFieldOfViewDegrees;
        this.verticalFieldOfViewDegrees = verticalFieldOfViewDegrees;
        this.orientation = orientation;
    }

    /** Returns the horizontal field of view in degrees. */
    public double getHorizontalFieldOfViewDegrees() { return horizontalFieldOfViewDegrees; }

    /** Returns the vertical field of view in degrees. */
    public double getVerticalFieldOfViewDegrees() { return verticalFieldOfViewDegrees; }

    /** Returns the camera orientation. */
    public CameraOrientation getOrientation() { return orientation; }

    /**
     * Returns half the horizontal field of view in degrees.
     * Useful for frustum plane calculations.
     */
    public double getHorizontalHalfAngleDegrees() {
        return horizontalFieldOfViewDegrees / 2.0;
    }

    /**
     * Returns half the vertical field of view in degrees.
     * Useful for frustum plane calculations.
     */
    public double getVerticalHalfAngleDegrees() {
        return verticalFieldOfViewDegrees / 2.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Viewport)) return false;
        Viewport other = (Viewport) obj;
        return Double.compare(horizontalFieldOfViewDegrees, other.horizontalFieldOfViewDegrees) == 0
                && Double.compare(verticalFieldOfViewDegrees, other.verticalFieldOfViewDegrees) == 0
                && orientation.equals(other.orientation);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(horizontalFieldOfViewDegrees);
        result = 31 * result + Double.hashCode(verticalFieldOfViewDegrees);
        result = 31 * result + orientation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Viewport{hFov=" + horizontalFieldOfViewDegrees
                + "\u00b0, vFov=" + verticalFieldOfViewDegrees
                + "\u00b0, orientation=" + orientation + "}";
    }
}
