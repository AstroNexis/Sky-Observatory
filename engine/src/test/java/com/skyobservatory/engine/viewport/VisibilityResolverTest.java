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

package com.skyobservatory.engine.viewport;

import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CameraOrientation;
import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.SkyCoordinate;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.Viewport;
import com.skyobservatory.api.VisibilityState;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VisibilityResolverTest {

    private final VisibilityResolver resolver = new VisibilityResolver();

    @Test
    public void visibleObjectInsideFrustum_isInVisibleList() {
        SkySnapshot snapshot = createSnapshot(
                makeObject(CelestialObject.sun(), 0.0, 15.0, VisibilityState.VISIBLE));
        Viewport viewport = new Viewport(90.0, 60.0, new CameraOrientation(0.0, 0.0, 0.0));
        var region = resolver.resolve(snapshot, viewport);
        assertEquals(1, region.getVisibleObjectCount());
        assertEquals(0, region.getHiddenObjects().size());
    }

    @Test
    public void visibleObjectBehindCamera_isInHiddenList() {
        SkySnapshot snapshot = createSnapshot(
                makeObject(CelestialObject.sun(), 180.0, 0.0, VisibilityState.VISIBLE));
        Viewport viewport = new Viewport(90.0, 60.0, new CameraOrientation(0.0, 0.0, 0.0));
        var region = resolver.resolve(snapshot, viewport);
        assertEquals(0, region.getVisibleObjectCount());
        assertEquals(1, region.getHiddenObjects().size());
    }

    @Test
    public void belowHorizonObject_isInHiddenList() {
        SkySnapshot snapshot = createSnapshot(
                makeObject(CelestialObject.sun(), 0.0, -10.0, VisibilityState.BELOW_HORIZON));
        Viewport viewport = new Viewport(90.0, 60.0, new CameraOrientation(0.0, 0.0, 0.0));
        var region = resolver.resolve(snapshot, viewport);
        assertEquals(0, region.getVisibleObjectCount());
        assertEquals(1, region.getHiddenObjects().size());
    }

    @Test
    public void mixedVisibility_splitsCorrectly() {
        List<ObservableObject> objects = Arrays.asList(
                makeObject(CelestialObject.sun(), 0.0, 10.0, VisibilityState.VISIBLE),
                makeObject(CelestialObject.moon(), 180.0, 0.0, VisibilityState.VISIBLE),
                makeObject(new CelestialObject(199, "Mercury"), 90.0, -5.0, VisibilityState.BELOW_HORIZON));
        SkySnapshot snapshot = createSnapshot(objects);
        Viewport viewport = new Viewport(90.0, 60.0, new CameraOrientation(0.0, 0.0, 0.0));
        var region = resolver.resolve(snapshot, viewport);
        assertEquals(1, region.getVisibleObjectCount());
        assertEquals(2, region.getHiddenObjects().size());
    }

    @Test
    public void totalCount_matchesSnapshot() {
        List<ObservableObject> objects = Arrays.asList(
                makeObject(CelestialObject.sun(), 0.0, 45.0, VisibilityState.VISIBLE),
                makeObject(CelestialObject.moon(), 180.0, 0.0, VisibilityState.VISIBLE));
        SkySnapshot snapshot = createSnapshot(objects);
        Viewport viewport = new Viewport(90.0, 60.0, new CameraOrientation(0.0, 0.0, 0.0));
        var region = resolver.resolve(snapshot, viewport);
        assertEquals(2, region.getTotalObjectCount());
    }

    private static SkySnapshot createSnapshot(ObservableObject... objects) {
        return createSnapshot(Arrays.asList(objects));
    }

    private static SkySnapshot createSnapshot(List<ObservableObject> objects) {
        return new SkySnapshot.Builder(
                AstroTime.j2000(),
                new Observer(0.0, 0.0, 0.0),
                objects).build();
    }

    private static ObservableObject makeObject(CelestialObject target, double az, double alt, VisibilityState v) {
        HorizontalCoordinate h = new HorizontalCoordinate(az, alt);
        double azRad = Math.toRadians(az);
        double altRad = Math.toRadians(alt);
        double cosAlt = Math.cos(altRad);
        double x = cosAlt * Math.sin(azRad);
        double y = Math.sin(altRad);
        double z = -cosAlt * Math.cos(azRad);
        CartesianCoordinate c = new CartesianCoordinate(x, y, z);
        return new ObservableObject(target, new SkyCoordinate(h, c), v, ObservableObject.ObjectCategory.STAR);
    }
}
