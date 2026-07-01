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
 * Indicates whether a celestial object is currently visible above the
 * observer's astronomical horizon.
 */
public enum VisibilityState {
    VISIBLE,
    BELOW_HORIZON;

    /**
     * Returns {@link #VISIBLE} when the given altitude is above zero,
     * {@link #BELOW_HORIZON} otherwise.
     *
     * @param altitudeDegrees altitude in degrees
     * @return the corresponding visibility state
     */
    public static VisibilityState fromAltitude(double altitudeDegrees) {
        return altitudeDegrees > 0.0 ? VISIBLE : BELOW_HORIZON;
    }
}
