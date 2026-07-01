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

package com.skyobservatory.engine.validation;

import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.ValidationException;

/**
 * Validates inputs before they reach the calculation layer.
 *
 * All methods throw {@link ValidationException}, a specific subtype of
 * {@code AstroException}, so callers can distinguish input errors from
 * calculation failures.
 */
public final class InputValidator {

    private static final double JD_MINIMUM = 2305812.5;
    private static final double JD_MAXIMUM = 2524593.5;

    public InputValidator() {}

    /**
     * Validates that an {@link Observer} has coordinates within accepted ranges.
     *
     * @throws ValidationException if any field is null or out of range
     */
    public void validateObserver(Observer observer) throws ValidationException {
        if (observer == null) {
            throw new ValidationException("Observer must not be null");
        }
        double lat = observer.getLatitudeDegrees();
        double lon = observer.getLongitudeDegrees();
        if (lat < -90.0 || lat > 90.0) {
            throw new ValidationException(
                    "Observer latitude " + lat + " is outside valid range [-90, 90]");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new ValidationException(
                    "Observer longitude " + lon + " is outside valid range [-180, 180]");
        }
    }

    /**
     * Validates that an {@link AstroTime} falls within the supported Julian Date range.
     *
     * @throws ValidationException if the time is null, non-finite, or out of range
     */
    public void validateTime(AstroTime time) throws ValidationException {
        if (time == null) {
            throw new ValidationException("AstroTime must not be null");
        }
        double jd = time.getJulianDateTT();
        if (!Double.isFinite(jd)) {
            throw new ValidationException(
                    "Julian Date must be a finite value, got: " + jd);
        }
        if (jd < JD_MINIMUM || jd > JD_MAXIMUM) {
            throw new ValidationException(
                    "Julian Date " + jd + " is outside supported range ["
                            + JD_MINIMUM + ", " + JD_MAXIMUM + "]");
        }
    }

    /**
     * Validates that a {@link CelestialObject} is non-null.
     *
     * @throws ValidationException if {@code target} is null
     */
    public void validateTarget(CelestialObject target) throws ValidationException {
        if (target == null) {
            throw new ValidationException("CelestialObject must not be null");
        }
    }
}
