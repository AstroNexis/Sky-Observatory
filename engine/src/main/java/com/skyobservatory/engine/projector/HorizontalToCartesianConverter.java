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

/**
 * Converts between horizontal (alt-azimuth) coordinates and Cartesian
 * coordinates in the local topocentric frame.
 *
 * <p>Coordinate convention (right-handed):</p>
 * <ul>
 *   <li><b>X</b>: east-west axis. Positive = east, negative = west.</li>
 *   <li><b>Y</b>: up-down axis. Positive = up (zenith), negative = down (nadir).</li>
 *   <li><b>Z</b>: north-south axis. Positive = south, negative = north.</li>
 * </ul>
 *
 * <p>Horizontal convention:
 * <ul>
 *   <li>Azimuth: degrees eastward from true north (0 = North, 90 = East).</li>
 *   <li>Altitude: degrees above the horizon (0 = horizon, +90 = zenith).</li>
 * </ul>
 *
 * <p>This class is package-private. Callers use
 * {@link CoordinateProjector} instead.</p>
 */
final class HorizontalToCartesianConverter {

    /**
     * Converts a {@link HorizontalCoordinate} to a unit {@link CartesianCoordinate}.
     *
     * @param horizontal the input horizontal coordinate; must not be null
     * @return a unit-length Cartesian coordinate in the topocentric frame
     */
    CartesianCoordinate toCartesian(HorizontalCoordinate horizontal) {
        double azRad = Math.toRadians(horizontal.getAzimuthDegrees());
        double altRad = Math.toRadians(horizontal.getAltitudeDegrees());

        double cosAlt = Math.cos(altRad);
        double x = cosAlt * Math.sin(azRad);
        double y = Math.sin(altRad);
        double z = -cosAlt * Math.cos(azRad);

        return new CartesianCoordinate(x, y, z);
    }

    /**
     * Converts a unit {@link CartesianCoordinate} back to a
     * {@link HorizontalCoordinate}.
     *
     * @param cartesian the Cartesian coordinate in the topocentric frame;
     *                  should be unit-length for correct results
     * @return the horizontal (alt-azimuth) representation
     */
    HorizontalCoordinate toHorizontal(CartesianCoordinate cartesian) {
        double x = cartesian.getX();
        double y = cartesian.getY();
        double z = cartesian.getZ();

        double azimuth = Math.toDegrees(Math.atan2(x, -z));
        if (azimuth < 0.0) {
            azimuth += 360.0;
        }

        double altitude = Math.toDegrees(Math.asin(clamp(y, -1.0, 1.0)));

        return new HorizontalCoordinate(azimuth, altitude);
    }

    private static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
}
