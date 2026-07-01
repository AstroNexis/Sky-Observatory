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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PositionResultTest {

    private CelestialObject sun;
    private Observer greenwich;
    private AstroTime j2000;

    @Before
    public void setUp() {
        sun = CelestialObject.sun();
        greenwich = new Observer(51.4769, -0.0005, 46.0);
        j2000 = AstroTime.j2000();
    }

    @Test
    public void legacyConstructorPopulatesHorizontalCoordinates() {
        PositionResult result = new PositionResult(180.0, 45.0, sun, greenwich, j2000);

        assertEquals(180.0, result.getAzimuthDegrees(), 1e-9);
        assertEquals(45.0, result.getAltitudeDegrees(), 1e-9);
        assertEquals(sun, result.getTarget());
        assertEquals(greenwich, result.getObserver());
        assertEquals(j2000, result.getTime());
    }

    @Test
    public void legacyConstructorLeavesOptionalFieldsNull() {
        PositionResult result = new PositionResult(180.0, 45.0, sun, greenwich, j2000);

        assertFalse(result.hasEquatorial());
        assertNull(result.getEquatorial());
        assertFalse(result.hasDistance());
        assertNull(result.getDistanceAu());
        assertFalse(result.hasPositionVector());
        assertNull(result.getPositionVector());
    }

    @Test
    public void builderPopulatesAllFields() {
        EquatorialCoordinates eq = new EquatorialCoordinates(270.0, -23.44);
        Vector3 vec = new Vector3(0.5, -0.2, 0.1);

        PositionResult result = new PositionResult.Builder(90.0, 30.0, sun, greenwich, j2000)
                .equatorial(eq)
                .distanceAu(1.016)
                .positionVector(vec)
                .build();

        assertEquals(90.0, result.getAzimuthDegrees(), 1e-9);
        assertEquals(30.0, result.getAltitudeDegrees(), 1e-9);
        assertTrue(result.hasEquatorial());
        assertEquals(270.0, result.getEquatorial().getRightAscensionDegrees(), 1e-9);
        assertTrue(result.hasDistance());
        assertEquals(1.016, result.getDistanceAu(), 1e-9);
        assertTrue(result.hasPositionVector());
        assertEquals(vec, result.getPositionVector());
    }

    @Test
    public void isAboveHorizonTrueWhenAltitudePositive() {
        PositionResult above = new PositionResult(0.0, 10.0, sun, greenwich, j2000);
        PositionResult below = new PositionResult(0.0, -5.0, sun, greenwich, j2000);
        PositionResult onHorizon = new PositionResult(0.0, 0.0, sun, greenwich, j2000);

        assertTrue(above.isAboveHorizon());
        assertFalse(below.isAboveHorizon());
        assertFalse(onHorizon.isAboveHorizon());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderRejectsNullTarget() {
        new PositionResult.Builder(0.0, 0.0, null, greenwich, j2000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderRejectsNullObserver() {
        new PositionResult.Builder(0.0, 0.0, sun, null, j2000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderRejectsNullTime() {
        new PositionResult.Builder(0.0, 0.0, sun, greenwich, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderRejectsNegativeDistance() {
        new PositionResult.Builder(0.0, 0.0, sun, greenwich, j2000).distanceAu(-1.0);
    }

    @Test
    public void toStringContainsTargetName() {
        PositionResult result = new PositionResult(180.0, 45.0, sun, greenwich, j2000);
        assertTrue(result.toString().contains("Sun"));
    }
}
