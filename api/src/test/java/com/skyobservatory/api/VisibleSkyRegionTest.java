package com.skyobservatory.api;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class VisibleSkyRegionTest {

    private static final AstroTime TIME = new AstroTime(2460000.5);
    private static final Observer OBSERVER = new Observer(0.0, 0.0, 0.0);
    private static final CameraOrientation ORIENTATION =
            new CameraOrientation(0.0, 0.0, 0.0);
    private static final Viewport VIEWPORT = new Viewport(90.0, 60.0, ORIENTATION);
    private static final SkyCoordinate POSITION = new SkyCoordinate(
            new HorizontalCoordinate(180.0, 45.0),
            new CartesianCoordinate(0.0, 0.7071, -0.7071));

    private static final CelestialObject SUN = CelestialObject.sun();
    private static final ObservableObject VISIBLE =
            new ObservableObject(SUN, POSITION, VisibilityState.VISIBLE,
                    ObservableObject.ObjectCategory.SOLAR_SYSTEM_BODY);

    @Test(expected = IllegalArgumentException.class)
    public void nullSnapshotThrows() {
        new VisibleSkyRegion(null, VIEWPORT, Collections.emptyList(), Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullViewportThrows() {
        SkySnapshot snap = new SkySnapshot.Builder(TIME, OBSERVER, Collections.emptyList()).build();
        new VisibleSkyRegion(snap, null, Collections.emptyList(), Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullVisibleThrows() {
        SkySnapshot snap = new SkySnapshot.Builder(TIME, OBSERVER, Collections.emptyList()).build();
        new VisibleSkyRegion(snap, VIEWPORT, null, Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHiddenThrows() {
        SkySnapshot snap = new SkySnapshot.Builder(TIME, OBSERVER, Collections.emptyList()).build();
        new VisibleSkyRegion(snap, VIEWPORT, Collections.emptyList(), null);
    }

    @Test
    public void storesAndReturnsValues() {
        SkySnapshot snap = new SkySnapshot.Builder(TIME, OBSERVER, Collections.emptyList()).build();
        List<ObservableObject> visible = Arrays.asList(VISIBLE);
        List<ObservableObject> hidden = Collections.emptyList();
        VisibleSkyRegion region = new VisibleSkyRegion(snap, VIEWPORT, visible, hidden);

        assertSame(snap, region.getSnapshot());
        assertSame(VIEWPORT, region.getViewport());
        assertEquals(1, region.getVisibleObjects().size());
        assertTrue(region.getHiddenObjects().isEmpty());
        assertEquals(1, region.getVisibleObjectCount());
        assertEquals(1, region.getTotalObjectCount());
    }

    @Test
    public void listsAreImmutableCopies() {
        SkySnapshot snap = new SkySnapshot.Builder(TIME, OBSERVER, Collections.emptyList()).build();
        List<ObservableObject> mutable = new ArrayList<>(Arrays.asList(VISIBLE));
        VisibleSkyRegion region = new VisibleSkyRegion(snap, VIEWPORT, mutable, Collections.emptyList());
        mutable.clear();
        assertEquals(1, region.getVisibleObjects().size());
    }

    @Test
    public void toStringContainsCounts() {
        SkySnapshot snap = new SkySnapshot.Builder(TIME, OBSERVER, Collections.emptyList()).build();
        List<ObservableObject> visible = Arrays.asList(VISIBLE);
        VisibleSkyRegion region = new VisibleSkyRegion(snap, VIEWPORT, visible, visible);
        String s = region.toString();
        assertTrue(s.contains("visible=1"));
        assertTrue(s.contains("hidden=1"));
    }
}
