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

import com.skyobservatory.api.HorizontalCoordinate;

/**
 * Determines the visibility of a sky position relative to the observer's horizon.
 *
 * <p>A position is visible when its altitude is greater than zero (above the
 * astronomical horizon). Positions at or below zero altitude are treated as
 * below the horizon.</p>
 *
 * <p>This class is package-private. Callers use
 * {@link CoordinateProjector} instead.</p>
 */
final class VisibilityCalculator {

    /**
     * Returns {@code true} if the given horizontal coordinate is above
     * the astronomical horizon.
     *
     * @param coordinate the horizontal coordinate to evaluate
     * @return {@code true} if altitude > 0
     */
    boolean isVisible(HorizontalCoordinate coordinate) {
        return coordinate.isAboveHorizon();
    }

    /**
     * Returns {@code true} if the given altitude (in degrees) is above
     * the astronomical horizon.
     *
     * @param altitudeDegrees altitude in degrees
     * @return {@code true} if altitude > 0
     */
    boolean isVisible(double altitudeDegrees) {
        return altitudeDegrees > 0.0;
    }

    /**
     * Returns a human-readable visibility label for the given altitude.
     *
     * @param altitudeDegrees altitude in degrees
     * @return "above horizon" if altitude > 0, "below horizon" otherwise
     */
    String describeVisibility(double altitudeDegrees) {
        return altitudeDegrees > 0.0 ? "above horizon" : "below horizon";
    }
}
