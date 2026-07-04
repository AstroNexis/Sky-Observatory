package com.skyobservatory.engine;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EphemerisResult;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EphemerisCalculatorTest {

    private static final double DELTA = 1e-9;

    private Observer observer;
    private AstroTime time;

    @Before
    public void setUp() {
        observer = new Observer(51.5, -0.1, 10.0);
        time = AstroTime.j2000();
    }

    @Test
    public void calculateForSunReturnsMagnitudeAndDiameter() throws AstroException {
        PositionProvider stub = (target, obs, t) ->
                new PositionResult.Builder(180.0, 45.0, target, obs, t)
                        .distanceAu(1.0)
                        .build();
        EphemerisCalculator calc = new EphemerisCalculator(stub);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        assertTrue(result.hasVisualMagnitude());
        assertEquals(-26.74, result.getVisualMagnitude(), 0.1);
        assertTrue(result.hasApparentDiameter());
        assertTrue(result.getApparentDiameterArcmin() > 0);
    }

    @Test
    public void calculateForMoonReturnsMagnitudeAndDiameter() throws AstroException {
        PositionProvider stub = (target, obs, t) ->
                new PositionResult.Builder(90.0, 30.0, target, obs, t)
                        .distanceAu(0.002569)
                        .build();
        EphemerisCalculator calc = new EphemerisCalculator(stub);
        EphemerisResult result = calc.calculate(CelestialObject.moon(), observer, time);

        assertTrue(result.hasVisualMagnitude());
        assertTrue(result.hasApparentDiameter());
    }

    @Test
    public void calculateWithoutDistanceReturnsOnlyRiseSet() throws AstroException {
        PositionProvider stub = (target, obs, t) ->
                new PositionResult(180.0, -10.0, target, obs, t);
        EphemerisCalculator calc = new EphemerisCalculator(stub);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        assertFalse(result.hasVisualMagnitude());
        assertFalse(result.hasApparentDiameter());
    }

    @Test
    public void calculateReturnsEmptyForUnknownBody() throws AstroException {
        CelestialObject asteroid = new CelestialObject("Asteroid", 9999999, CelestialObject.Category.ASTEROID);
        PositionProvider stub = (target, obs, t) ->
                new PositionResult.Builder(0.0, 0.0, target, obs, t)
                        .distanceAu(2.5)
                        .build();
        EphemerisCalculator calc = new EphemerisCalculator(stub);
        EphemerisResult result = calc.calculate(asteroid, observer, time);

        assertFalse(result.hasVisualMagnitude());
        assertFalse(result.hasApparentDiameter());
    }

    @Test
    public void computeRiseSetWhenBodyIsAlwaysAboveHorizon() throws AstroException {
        PositionProvider stub = (target, obs, t) ->
                new PositionResult.Builder(180.0, 45.0, target, obs, t)
                        .distanceAu(1.0)
                        .build();
        EphemerisCalculator calc = new EphemerisCalculator(stub);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        // Body is always above horizon, so rise/set may or may not be found
        // depending on whether altitude changes across the day. The stub
        // returns constant 45 deg altitude, so no crossing is expected.
        assertFalse("No rise should be found for constant above-horizon body",
                result.hasRiseTime());
        assertFalse("No set should be found for constant above-horizon body",
                result.hasSetTime());
    }

    @Test(expected = AstroException.class)
    public void calculatePropagatesProviderError() throws AstroException {
        PositionProvider failing = (target, obs, t) -> {
            throw new AstroException("provider failed");
        };
        EphemerisCalculator calc = new EphemerisCalculator(failing);
        calc.calculate(CelestialObject.sun(), observer, time);
    }
}
