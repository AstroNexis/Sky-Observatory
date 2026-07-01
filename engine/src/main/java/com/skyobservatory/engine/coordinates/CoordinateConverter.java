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

package com.skyobservatory.engine.coordinates;

/**
 * Standard output coordinate normalization.
 *
 * Implements {@link CoordinateTransform} with the default rules:
 * azimuth normalized to [0, 360) and altitude clamped to [-90, 90].
 *
 * Also provides degree/radian conversion utilities used internally
 * by the engine and future coordinate frame transforms.
 *
 * Phase 1 only implements the normalization needed for horizontal output.
 * Full frame-of-reference transforms (ICRS->GCRS->TIRS->topocentric) are
 * extension points for Phase 3.
 */
public final class CoordinateConverter implements CoordinateTransform {

    public CoordinateConverter() {}

    /**
     * Converts degrees to radians.
     *
     * @param degrees angle in degrees
     * @return equivalent angle in radians
     */
    public double degreesToRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    /**
     * Converts radians to degrees.
     *
     * @param radians angle in radians
     * @return equivalent angle in degrees
     */
    public double radiansToDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double normalizeAzimuth(double degrees) {
        double normalized = degrees % 360.0;
        if (normalized < 0.0) {
            normalized += 360.0;
        }
        return normalized;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double clampAltitude(double degrees) {
        return Math.max(-90.0, Math.min(90.0, degrees));
    }
}
