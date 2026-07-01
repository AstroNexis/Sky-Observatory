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

/**
 * Computation contract for a single sky position calculation.
 *
 * This interface is the boundary between the engine orchestration layer and
 * any concrete calculation backend (native library, pure-Java fallback, mock).
 * {@link com.skyobservatory.engine.PositionCalculator} depends on this
 * interface rather than a concrete implementation.
 *
 * <h3>Planned implementations</h3>
 * <ul>
 *   <li>{@code NativeAstroCalculator} -- delegates to the SuperNOVAS JNI bridge (current)</li>
 *   <li>{@code SuperNovasPositionProvider} -- renamed/refined in Phase 2</li>
 *   <li>Test doubles -- anonymous subclasses or mocks in unit tests</li>
 * </ul>
 *
 * Inputs are pre-validated by the engine layer before this method is called.
 * Implementations may assume non-null arguments and values within documented ranges.
 */
interface PositionProvider {

    /**
     * Computes the apparent topocentric position of a solar system body.
     *
     * @param target   the body to locate; not null, NAIF id is valid
     * @param observer the observer location; not null, lat/lon within range
     * @param time     the observation time in TT; not null, JD within supported range
     * @return a {@link PositionResult} containing at minimum azimuth and altitude
     * @throws AstroException if the backend cannot produce a result
     */
    PositionResult calculatePosition(CelestialObject target, Observer observer, AstroTime time)
            throws AstroException;
}
