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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SkySnapshotTest {

    @Test
    public void builder_createsSnapshot() {
        AstroTime time = new AstroTime(2451545.0);
        Observer observer = new Observer(40.0, -74.0, 10.0);
        List<ObservableObject> objects = createSampleObjects();
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, objects).build();
        assertEquals(time, snapshot.getTime());
        assertEquals(observer, snapshot.getObserver());
        assertEquals(objects, snapshot.getObjects());
    }

    @Test
    public void builder_orientation_isOptional() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, createSampleObjects()).build();
        assertNull(snapshot.getOrientation());
    }

    @Test
    public void builder_withOrientation() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        DeviceOrientation orientation = new DeviceOrientation(
                CartesianCoordinate.SOUTH, CartesianCoordinate.UP);
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, createSampleObjects())
                .orientation(orientation)
                .build();
        assertEquals(orientation, snapshot.getOrientation());
    }

    @Test
    public void objectsList_isUnmodifiable() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, createSampleObjects()).build();
        try {
            snapshot.getObjects().add(null);
            assertFalse("List must be unmodifiable", true);
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void getVisibleObjects_returnsOnlyVisible() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        List<ObservableObject> objects = new ArrayList<>();
        objects.add(makeObject(45.0, 30.0, VisibilityState.VISIBLE));
        objects.add(makeObject(45.0, -10.0, VisibilityState.BELOW_HORIZON));
        objects.add(makeObject(45.0, 0.0, VisibilityState.BELOW_HORIZON));
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, objects).build();
        assertEquals(1, snapshot.getVisibleObjects().size());
        assertEquals(1, snapshot.getVisibleObjectCount());
        assertEquals(3, snapshot.getObjectCount());
    }

    @Test
    public void builder_defensiveCopy() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        List<ObservableObject> original = new ArrayList<>(createSampleObjects());
        List<ObservableObject> builderInput = new ArrayList<>(original);
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, builderInput).build();
        builderInput.clear();
        assertEquals(original, snapshot.getObjects());
    }

    @Test
    public void getVisibleObjectsList_isUnmodifiable() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, createSampleObjects()).build();
        try {
            snapshot.getVisibleObjects().add(null);
            assertFalse("Visible list must be unmodifiable", true);
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void toString_containsObjectCount() {
        AstroTime time = AstroTime.j2000();
        Observer observer = new Observer(0.0, 0.0, 0.0);
        SkySnapshot snapshot = new SkySnapshot.Builder(time, observer, createSampleObjects()).build();
        String s = snapshot.toString();
        assertTrue(s.contains("objects=2"));
    }

    private static List<ObservableObject> createSampleObjects() {
        return Arrays.asList(
                makeObject(0.0, 45.0, VisibilityState.VISIBLE),
                makeObject(180.0, -20.0, VisibilityState.BELOW_HORIZON));
    }

    private static ObservableObject makeObject(double az, double alt, VisibilityState v) {
        return new ObservableObject(
                CelestialObject.sun(),
                new SkyCoordinate(
                        new HorizontalCoordinate(az, alt),
                        new CartesianCoordinate(0.0, 0.0, -1.0)),
                v,
                ObservableObject.ObjectCategory.STAR);
    }
}
