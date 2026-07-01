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

import java.util.List;

/**
 * The primary entry point for SDK consumers.
 *
 * Applications obtain a position by calling {@link #calculatePosition(CelestialObject, Observer, AstroTime)}.
 * The resulting {@link PositionResult} can be projected into 3D coordinates
 * via {@link #project(HorizontalCoordinate)} for use in rendering pipelines.
 *
 * For batch rendering use {@link #createSnapshot(List, Observer, AstroTime)}
 * to produce a self-contained {@link SkySnapshot} with all object positions
 * and Cartesian coordinates pre-computed.
 *
 * Extended ephemeris data (rise/set times, apparent magnitude, apparent diameter)
 * can be obtained via {@link #calculateEphemeris(CelestialObject, Observer, AstroTime)}.
 * All fields in {@link EphemerisResult} are derived from SuperNOVAS computations;
 * no static catalog values are injected.
 *
 * This interface intentionally hides all calculation strategy, engine selection, and
 * native layer details from the application layer.
 *
 * Implementations are obtained via {@link AstroSdk}.
 */
public interface AstroEngine {

    /**
     * Calculates the apparent topocentric position of a celestial object as seen
     * by the given observer at the given time.
     *
     * @param target   the celestial body to locate
     * @param observer the ground or space observer
     * @param time     the time for the observation in TT
     * @return a {@link PositionResult} containing azimuth and altitude
     * @throws AstroException if the calculation cannot be performed
     */
    PositionResult calculatePosition(CelestialObject target, Observer observer, AstroTime time)
            throws AstroException;

    /**
     * Computes extended ephemeris data for a celestial object.
     *
     * <p>Populates rise/set times (via iterative altitude sampling over the
     * calendar day containing {@code time}), visual magnitude (from observer
     * distance via the distance modulus), and apparent angular diameter (from
     * distance and IAU mean radius). All values are derived from SuperNOVAS
     * calculations; no static values are injected.</p>
     *
     * <p>Fields that cannot be computed for the given object are absent
     * ({@code null}) in the returned result.</p>
     *
     * @param target   the celestial body
     * @param observer the ground observer
     * @param time     the reference time (rise/set are searched within the
     *                 calendar day that contains this time)
     * @return an {@link EphemerisResult} with available fields populated
     * @throws AstroException if the underlying position calculations fail
     */
    EphemerisResult calculateEphemeris(CelestialObject target, Observer observer, AstroTime time)
            throws AstroException;

    /**
     * Projects a {@link HorizontalCoordinate} into a {@link SkyCoordinate}
     * containing both the horizontal and Cartesian representations.
     *
     * <p>Cartesian coordinate convention (right-handed):</p>
     * <ul>
     *   <li><b>X</b>: east-west. Positive = east.</li>
     *   <li><b>Y</b>: up-down. Positive = up (zenith).</li>
     *   <li><b>Z</b>: north-south. Positive = south.</li>
     * </ul>
     *
     * @param coordinate the horizontal coordinate to project; must not be null
     * @return a {@link SkyCoordinate} with both representations
     */
    SkyCoordinate project(HorizontalCoordinate coordinate);

    /**
     * Creates a complete {@link SkySnapshot} containing computed positions for
     * all requested celestial objects.
     *
     * <p>This is the primary entry point for rendering pipelines. A single
     * snapshot provides every value a renderer needs to draw a frame:
     * object positions (horizontal and Cartesian), visibility state, observer
     * location, and observation time.</p>
     *
     * <p>The snapshot is an immutable value object. Creating a new snapshot
     * at a different time or for different targets is a stateless operation
     * that does not modify any prior snapshot.</p>
     *
     * @param targets  the celestial objects to compute positions for; must
     *                 not be null or empty
     * @param observer the observer location; must not be null
     * @param time     the observation time; must not be null
     * @return a fully populated {@link SkySnapshot}
     * @throws AstroException if any calculation fails
     */
    SkySnapshot createSnapshot(List<CelestialObject> targets, Observer observer, AstroTime time)
            throws AstroException;

    /**
     * Determines which objects from a {@link SkySnapshot} fall inside the
     * given {@link Viewport} frustum.
     *
     * <p>This is the primary entry point for viewport-aware rendering. A
     * single call returns a {@link VisibleSkyRegion} with the objects split
     * into visible and hidden sets, pre-filtered by both horizon visibility
     * and frustum culling.</p>
     *
     * <p>The calculation accounts for the camera orientation, field of view,
     * and each object's Cartesian position in the topocentric frame.</p>
     *
     * @param snapshot the sky snapshot containing objects to evaluate
     * @param viewport the camera viewport defining the frustum
     * @return a {@link VisibleSkyRegion} with visible and hidden object lists
     */
    VisibleSkyRegion calculateVisibleRegion(SkySnapshot snapshot, Viewport viewport);
}
