package com.skyobservatory.engine;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EphemerisResult;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;

import com.skyobservatory.engine.validation.InputValidator;

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
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), stub);
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
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), stub);
        EphemerisResult result = calc.calculate(CelestialObject.moon(), observer, time);

        assertTrue(result.hasVisualMagnitude());
        assertTrue(result.hasApparentDiameter());
    }

    @Test
    public void calculateWithoutDistanceReturnsOnlyRiseSet() throws AstroException {
        PositionProvider stub = (target, obs, t) ->
                new PositionResult(180.0, -10.0, target, obs, t);
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), stub);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        assertFalse(result.hasVisualMagnitude());
        assertFalse(result.hasApparentDiameter());
    }

    @Test
    public void calculateReturnsEmptyForUnknownBody() throws AstroException {
        CelestialObject asteroid = new CelestialObject(9999999, "Asteroid");
        PositionProvider stub = (target, obs, t) ->
                new PositionResult.Builder(0.0, 0.0, target, obs, t)
                        .distanceAu(2.5)
                        .build();
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), stub);
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
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), stub);
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
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), failing);
        calc.calculate(CelestialObject.sun(), observer, time);
    }

    @Test
    public void computeRiseSetWhenBodyCrossesHorizon() throws AstroException {
        // Parabolic altitude: -10 at midnight, peaks at +4.4 at midday,
        // back to -10 at next midnight. Crosses zero near 5.4h and 18.6h,
        // between sample points (48 samples = one every 0.5h).
        PositionProvider crossing = (target, obs, t) -> {
            double hours = (t.getJulianDateTT() - 2451544.5) * 24.0;
            double alt = -10.0 + hours * (24.0 - hours) * 0.1;
            return new PositionResult.Builder(180.0, alt, target, obs, t).build();
        };
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), crossing);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        assertTrue("Expected a rise time for crossing body", result.hasRiseTime());
        assertTrue("Expected a set time for crossing body", result.hasSetTime());
    }

    @Test
    public void computeRiseSetWhenCrossingsFallOnSampleBoundaries() throws AstroException {
        PositionProvider crossing = (target, obs, t) -> {
            double hours = (t.getJulianDateTT() - 2451544.5) * 24.0;
            double alt = (hours - 6.0) * (18.0 - hours);
            return new PositionResult.Builder(180.0, alt, target, obs, t).build();
        };
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), crossing);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        assertTrue("Expected a rise time at a sample boundary", result.hasRiseTime());
        assertTrue("Expected a set time at a sample boundary", result.hasSetTime());
        assertEquals(2451544.5 + 6.0 / 24.0, result.getRiseTimeJd(), 1e-8);
        assertEquals(2451544.5 + 18.0 / 24.0, result.getSetTimeJd(), 1e-8);
    }

    @Test
    public void riseSetBisectionStopsOnceBracketCannotShrink() throws AstroException {
        // Same parabola as above; roots are at 12 -/+ sqrt(44) hours after
        // JD 2451544.5. The call count covers one position fetch, 49 coarse
        // samples and two bisections; the limit sits below the 130 calls made
        // when each bisection ran a fixed 40 iterations.
        final int[] calls = {0};
        PositionProvider crossing = (target, obs, t) -> {
            calls[0]++;
            double hours = (t.getJulianDateTT() - 2451544.5) * 24.0;
            double alt = -10.0 + hours * (24.0 - hours) * 0.1;
            return new PositionResult.Builder(180.0, alt, target, obs, t).build();
        };
        EphemerisCalculator calc = new EphemerisCalculator(new InputValidator(), crossing);
        EphemerisResult result = calc.calculate(CelestialObject.sun(), observer, time);

        double expectedRiseHours = 12.0 - Math.sqrt(44.0);
        double expectedSetHours  = 12.0 + Math.sqrt(44.0);
        assertEquals(2451544.5 + expectedRiseHours / 24.0, result.getRiseTimeJd(), 1e-8);
        assertEquals(2451544.5 + expectedSetHours / 24.0, result.getSetTimeJd(), 1e-8);
        assertTrue("Bisection kept sampling past double precision: " + calls[0] + " calls",
                calls[0] < 110);
    }
}
