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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ObservableObjectTest {

    private static final double DELTA = 1e-12;

    @Test
    public void constructor_createsObject() {
        CelestialObject sun = CelestialObject.sun();
        HorizontalCoordinate h = new HorizontalCoordinate(45.0, 30.0);
        CartesianCoordinate c = new CartesianCoordinate(0.5, 0.5, 0.7071067811865476);
        SkyCoordinate pos = new SkyCoordinate(h, c);
        ObservableObject obj = new ObservableObject(
                sun, pos, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        assertEquals(sun, obj.getTarget());
        assertEquals(pos, obj.getPosition());
        assertEquals(VisibilityState.VISIBLE, obj.getVisibility());
        assertEquals(ObservableObject.ObjectCategory.STAR, obj.getCategory());
    }

    @Test
    public void equals_byValue() {
        ObservableObject a = makeSunObj(45.0, 30.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        ObservableObject b = makeSunObj(45.0, 30.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void notEquals_differentTarget() {
        ObservableObject a = makeSunObj(45.0, 30.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        ObservableObject b = makeMoonObj(45.0, 30.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.MOON);
        assertFalse(a.equals(b));
    }

    @Test
    public void notEquals_differentVisibility() {
        ObservableObject a = makeSunObj(45.0, 30.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        ObservableObject b = makeSunObj(45.0, 30.0, VisibilityState.BELOW_HORIZON, ObservableObject.ObjectCategory.STAR);
        assertFalse(a.equals(b));
    }

    @Test
    public void convenienceAccessors() {
        ObservableObject obj = makeSunObj(90.0, 45.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        assertEquals(90.0, obj.getAzimuthDegrees(), DELTA);
        assertEquals(45.0, obj.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void toString_containsTargetName() {
        ObservableObject obj = makeSunObj(0.0, 0.0, VisibilityState.VISIBLE, ObservableObject.ObjectCategory.STAR);
        assertTrue(obj.toString().contains("Sun"));
    }

    @Test
    public void objectCategory_hasExpectedValues() {
        assertEquals(8, ObservableObject.ObjectCategory.values().length);
        assertNotNull(ObservableObject.ObjectCategory.valueOf("STAR"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("PLANET"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("MOON"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("SATELLITE"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("DEEP_SKY"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("CONSTELLATION"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("SOLAR_SYSTEM_BODY"));
        assertNotNull(ObservableObject.ObjectCategory.valueOf("UNKNOWN"));
    }

    private static ObservableObject makeSunObj(double az, double alt, VisibilityState v, ObservableObject.ObjectCategory cat) {
        return new ObservableObject(
                CelestialObject.sun(),
                new SkyCoordinate(new HorizontalCoordinate(az, alt), new CartesianCoordinate(0.0, 0.0, -1.0)),
                v, cat);
    }

    private static ObservableObject makeMoonObj(double az, double alt, VisibilityState v, ObservableObject.ObjectCategory cat) {
        return new ObservableObject(
                CelestialObject.moon(),
                new SkyCoordinate(new HorizontalCoordinate(az, alt), new CartesianCoordinate(0.0, 0.0, -1.0)),
                v, cat);
    }
}
