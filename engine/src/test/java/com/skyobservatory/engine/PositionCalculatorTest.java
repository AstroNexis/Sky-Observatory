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
import com.skyobservatory.engine.coordinates.CoordinateConverter;
import com.skyobservatory.engine.validation.InputValidator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests PositionCalculator's orchestration logic using a {@link PositionProvider}
 * lambda as a test double. No native library loading is required.
 */
public class PositionCalculatorTest {

    private PositionCalculator calculator;

    private static final double STUB_AZIMUTH  = 180.0;
    private static final double STUB_ALTITUDE = 45.0;

    @Before
    public void setUp() {
        InputValidator validator = new InputValidator();
        CoordinateConverter converter = new CoordinateConverter();

        // PositionProvider is a functional interface -- lambda avoids subclassing.
        PositionProvider stubProvider = (target, observer, time) ->
                new PositionResult(STUB_AZIMUTH, STUB_ALTITUDE, target, observer, time);

        calculator = new PositionCalculator(validator, converter, stubProvider);
    }

    @Test
    public void calculateReturnsNormalizedResult() throws AstroException {
        Observer observer = new Observer(51.5, -0.1, 10.0);
        AstroTime time = AstroTime.j2000();
        CelestialObject target = CelestialObject.sun();

        PositionResult result = calculator.calculate(target, observer, time);

        assertEquals(STUB_AZIMUTH, result.getAzimuthDegrees(), 1e-9);
        assertEquals(STUB_ALTITUDE, result.getAltitudeDegrees(), 1e-9);
        assertEquals(target, result.getTarget());
    }

    @Test(expected = AstroException.class)
    public void calculateThrowsForNullObserver() throws AstroException {
        calculator.calculate(CelestialObject.sun(), null, AstroTime.j2000());
    }

    @Test(expected = AstroException.class)
    public void calculateThrowsForNullTarget() throws AstroException {
        calculator.calculate(null, new Observer(0, 0, 0), AstroTime.j2000());
    }

    @Test(expected = AstroException.class)
    public void calculateThrowsForInvalidLatitude() throws AstroException {
        calculator.calculate(
                CelestialObject.sun(), new Observer(200.0, 0.0, 0.0), AstroTime.j2000());
    }

    @Test
    public void azimuthIsNormalizedBeforeReturn() throws AstroException {
        Observer observer = new Observer(0.0, 0.0, 0.0);
        PositionResult result = calculator.calculate(
                CelestialObject.sun(), observer, AstroTime.j2000());

        double az = result.getAzimuthDegrees();
        assertTrue("Azimuth must be in [0, 360)", az >= 0.0 && az < 360.0);
    }

    @Test
    public void optionalFieldsFromProviderArePreserved() throws AstroException {
        // Verify that optional fields set by the provider survive through the calculator.
        com.skyobservatory.api.EquatorialCoordinates eq =
                new com.skyobservatory.api.EquatorialCoordinates(270.0, -23.5);
        com.skyobservatory.api.Vector3 vec =
                new com.skyobservatory.api.Vector3(0.5, 0.3, 0.1);

        PositionProvider richProvider = (target, observer, time) ->
                new PositionResult.Builder(STUB_AZIMUTH, STUB_ALTITUDE, target, observer, time)
                        .equatorial(eq)
                        .distanceAu(1.0)
                        .positionVector(vec)
                        .build();

        PositionCalculator richCalculator = new PositionCalculator(
                new InputValidator(), new CoordinateConverter(), richProvider);

        PositionResult result = richCalculator.calculate(
                CelestialObject.sun(), new Observer(0, 0, 0), AstroTime.j2000());

        assertTrue(result.hasEquatorial());
        assertEquals(270.0, result.getEquatorial().getRightAscensionDegrees(), 1e-9);
        assertTrue(result.hasDistance());
        assertEquals(1.0, result.getDistanceAu(), 1e-9);
        assertTrue(result.hasPositionVector());
        assertEquals(vec, result.getPositionVector());
    }
}
