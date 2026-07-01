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
 * Extension point for output coordinate normalization strategies.
 *
 * The default implementation is {@link CoordinateConverter}, which applies
 * standard range normalization (azimuth to [0,360), altitude clamped to [-90,90]).
 * Alternative strategies -- for example, a no-op passthrough for raw output
 * inspection in tests, or a frame-specific transform in later phases -- can be
 * provided by implementing this interface.
 *
 * This interface is intentionally minimal. It covers only the normalization
 * operations that {@link com.skyobservatory.engine.PositionCalculator}
 * applies to raw output. Full coordinate frame transforms (ICRS->GCRS, etc.)
 * will be separate concerns in Phase 3.
 */
public interface CoordinateTransform {

    /**
     * Normalizes an azimuth angle to the range [0, 360).
     *
     * @param degrees the raw azimuth in degrees
     * @return equivalent angle in [0, 360)
     */
    double normalizeAzimuth(double degrees);

    /**
     * Clamps an altitude angle to the physical range [-90, 90].
     *
     * @param degrees the raw altitude in degrees
     * @return clamped value in [-90, 90]
     */
    double clampAltitude(double degrees);
}
