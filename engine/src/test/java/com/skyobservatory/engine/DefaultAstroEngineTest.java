package com.skyobservatory.engine;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CameraOrientation;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EphemerisResult;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.api.SkyCoordinate;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.Viewport;
import com.skyobservatory.api.VisibleSkyRegion;
import com.skyobservatory.api.VisibilityState;
import com.skyobservatory.engine.coordinates.CoordinateConverter;
import com.skyobservatory.engine.validation.InputValidator;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultAstroEngineTest {

    private static final double DELTA = 1e-9;
    private DefaultAstroEngine engine;
    private Observer observer;
    private AstroTime time;

    @Before
    public void setUp() {
        observer = new Observer(51.5, -0.1, 10.0);
        time = AstroTime.j2000();

        PositionProvider stubProvider = (target, obs, t) ->
                new PositionResult.Builder(180.0, 45.0, target, obs, t)
                        .distanceAu(1.0)
                        .build();

        PositionCalculator posCalc = new PositionCalculator(
                new InputValidator(), new CoordinateConverter(), stubProvider);
        EphemerisCalculator ephCalc = new EphemerisCalculator(stubProvider);
        engine = new DefaultAstroEngine(posCalc, ephCalc);
    }

    @Test
    public void calculatePositionReturnsResult() throws AstroException {
        PositionResult result = engine.calculatePosition(
                CelestialObject.sun(), observer, time);
        assertEquals(180.0, result.getAzimuthDegrees(), DELTA);
        assertEquals(45.0, result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void calculateEphemerisReturnsResult() throws AstroException {
        EphemerisResult result = engine.calculateEphemeris(
                CelestialObject.sun(), observer, time);
        assertTrue(result.hasVisualMagnitude());
    }

    @Test
    public void projectReturnsSkyCoordinate() {
        HorizontalCoordinate h = new HorizontalCoordinate(180.0, 45.0);
        SkyCoordinate c = engine.project(h);
        assertEquals(180.0, c.getAzimuthDegrees(), DELTA);
        assertEquals(45.0, c.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void createSnapshotReturnsPopulatedSnapshot() throws AstroException {
        List<CelestialObject> targets = Arrays.asList(
                CelestialObject.sun(), CelestialObject.moon());
        SkySnapshot snapshot = engine.createSnapshot(targets, observer, time);

        assertEquals(2, snapshot.getObjects().size());
        assertEquals(time, snapshot.getTime());
        assertEquals(observer, snapshot.getObserver());
    }

    @Test
    public void objectsInSnapshotHaveVisibilityAboveHorizon() throws AstroException {
        List<CelestialObject> targets = Arrays.asList(CelestialObject.sun());
        SkySnapshot snapshot = engine.createSnapshot(targets, observer, time);

        for (ObservableObject obj : snapshot.getObjects()) {
            assertEquals(VisibilityState.VISIBLE, obj.getVisibility());
        }
    }

    @Test
    public void calculateVisibleRegionReturnsPartitionedLists() throws AstroException {
        List<CelestialObject> targets = Arrays.asList(CelestialObject.sun());
        SkySnapshot snapshot = engine.createSnapshot(targets, observer, time);
        CameraOrientation orientation = new CameraOrientation(0, 0, 0);
        Viewport viewport = new Viewport(90.0, 60.0, orientation);

        VisibleSkyRegion region = engine.calculateVisibleRegion(snapshot, viewport);
        assertEquals(1, region.getVisibleObjectCount() + region.getHiddenObjects().size());
    }

    @Test(expected = AstroException.class)
    public void calculatePositionThrowsOnInvalidTarget() throws AstroException {
        engine.calculatePosition(null, observer, time);
    }
}
