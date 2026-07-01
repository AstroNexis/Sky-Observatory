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

package com.skyobservatory.engine.projector;

import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.SkyCoordinate;

/**
 * Converts horizontal (alt-azimuth) sky positions into Cartesian coordinates
 * suitable for 3D rendering pipelines.
 *
 * <p>This is the public-facing entry point for the projection subsystem. It
 * delegates to {@link HorizontalToCartesianConverter} for coordinate conversion
 * and {@link VisibilityCalculator} for horizon checks.</p>
 *
 * <p>Coordinate conventions (right-handed):</p>
 * <ul>
 *   <li><b>X</b>: east-west axis. Positive = east, negative = west.</li>
 *   <li><b>Y</b>: up-down axis. Positive = up (zenith), negative = down (nadir).</li>
 *   <li><b>Z</b>: north-south axis. Positive = south, negative = north.</li>
 * </ul>
 *
 * <p>The output Cartesian coordinates are unit-length (on the unit sphere).
 * Callers that need physical distances should scale by the appropriate
 * radius value.</p>
 *
 * <p>This class is part of the engine module and is not visible to application
 * code. Applications interact with the projection pipeline through
 * {@link com.skyobservatory.api.AstroEngine#project(
 * com.skyobservatory.api.PositionResult)}.</p>
 */
public final class CoordinateProjector {

    private final HorizontalToCartesianConverter converter;
    private final VisibilityCalculator visibility;

    public CoordinateProjector() {
        this.converter = new HorizontalToCartesianConverter();
        this.visibility = new VisibilityCalculator();
    }

    /**
     * Projects the horizontal coordinates to a Cartesian unit vector and
     * returns a {@link SkyCoordinate} containing both representations.
     *
     * @param azimuthDegrees  topocentric azimuth in degrees [0, 360)
     * @param altitudeDegrees topocentric altitude in degrees [-90, 90]
     * @return a {@link SkyCoordinate} with both horizontal and Cartesian
     *         representations
     */
    public SkyCoordinate project(double azimuthDegrees, double altitudeDegrees) {
        HorizontalCoordinate horizontal = new HorizontalCoordinate(azimuthDegrees, altitudeDegrees);
        CartesianCoordinate cartesian = converter.toCartesian(horizontal).normalize();
        return new SkyCoordinate(horizontal, cartesian);
    }

    /**
     * Converts a {@link HorizontalCoordinate} to a Cartesian unit vector.
     *
     * @param horizontal the input horizontal coordinate
     * @return the unit-length Cartesian representation
     */
    public CartesianCoordinate toCartesian(HorizontalCoordinate horizontal) {
        return converter.toCartesian(horizontal).normalize();
    }

    /**
     * Converts a Cartesian unit vector back to horizontal coordinates.
     *
     * @param cartesian the Cartesian coordinate (should be unit-length)
     * @return the horizontal (alt-azimuth) representation
     */
    public HorizontalCoordinate toHorizontal(CartesianCoordinate cartesian) {
        return converter.toHorizontal(cartesian.normalize());
    }

    /**
     * Returns {@code true} if the given altitude is above the astronomical horizon.
     *
     * @param altitudeDegrees altitude in degrees
     * @return {@code true} if visible
     */
    public boolean isVisible(double altitudeDegrees) {
        return visibility.isVisible(altitudeDegrees);
    }

    /**
     * Returns {@code true} if the given horizontal coordinate is above the
     * astronomical horizon.
     *
     * @param coordinate the horizontal coordinate to evaluate
     * @return {@code true} if visible
     */
    public boolean isVisible(HorizontalCoordinate coordinate) {
        return visibility.isVisible(coordinate);
    }

    /**
     * Returns a human-readable visibility label.
     *
     * @param altitudeDegrees altitude in degrees
     * @return "above horizon" or "below horizon"
     */
    public String describeVisibility(double altitudeDegrees) {
        return visibility.describeVisibility(altitudeDegrees);
    }
}
