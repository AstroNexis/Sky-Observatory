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

package com.skyobservatory.engine;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.engine.coordinates.CoordinateTransform;
import com.skyobservatory.engine.validation.InputValidator;

/**
 * Orchestrates a sky position calculation.
 *
 * Responsibilities: validate inputs, delegate to a {@link PositionProvider},
 * and apply output normalization. This class has no astronomical knowledge of
 * its own; it coordinates other components.
 *
 * This class is package-private. Nothing outside the engine module should
 * reference it directly.
 */
final class PositionCalculator {

    private final InputValidator validator;
    private final CoordinateTransform converter;
    private final PositionProvider provider;

    /**
     * @param validator pre-flight input validation
     * @param converter output coordinate normalization
     * @param provider  backend that performs the actual position calculation
     */
    PositionCalculator(
            InputValidator validator,
            CoordinateTransform converter,
            PositionProvider provider) {
        this.validator = validator;
        this.converter = converter;
        this.provider = provider;
    }

    /**
     * Validates inputs, calls the backend, normalizes and returns the result.
     *
     * @throws AstroException if validation fails or the backend reports an error
     */
    PositionResult calculate(
            CelestialObject target,
            Observer observer,
            AstroTime time) throws AstroException {

        validator.validateTarget(target);
        validator.validateObserver(observer);
        validator.validateTime(time);

        PositionResult raw = provider.calculatePosition(target, observer, time);

        double azimuth = converter.normalizeAzimuth(raw.getAzimuthDegrees());
        double altitude = converter.clampAltitude(raw.getAltitudeDegrees());

        // Preserve any optional fields the provider may have populated.
        PositionResult.Builder builder = new PositionResult.Builder(
                azimuth, altitude, target, observer, time);

        if (raw.hasEquatorial()) {
            builder.equatorial(raw.getEquatorial());
        }
        if (raw.hasDistance()) {
            builder.distanceAu(raw.getDistanceAu());
        }
        if (raw.hasPositionVector()) {
            builder.positionVector(raw.getPositionVector());
        }

        return builder.build();
    }
}
