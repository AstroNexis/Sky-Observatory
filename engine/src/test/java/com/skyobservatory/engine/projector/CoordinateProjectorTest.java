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

package com.skyobservatory.engine.projector;

import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.SkyCoordinate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoordinateProjectorTest {

    private static final double DELTA = 1e-9;
    private final CoordinateProjector projector = new CoordinateProjector();

    @Test
    public void project_zenith() {
        SkyCoordinate sky = projector.project(0.0, 90.0);
        assertEquals(90.0, sky.getAltitudeDegrees(), DELTA);
        assertEquals(0.0, sky.getX(), DELTA);
        assertEquals(1.0, sky.getY(), DELTA);
        assertEquals(0.0, sky.getZ(), DELTA);
        assertTrue(sky.getHorizontal().isAboveHorizon());
    }

    @Test
    public void project_north() {
        SkyCoordinate sky = projector.project(0.0, 0.0);
        assertEquals(0.0, sky.getAltitudeDegrees(), DELTA);
        assertEquals(0.0, sky.getX(), DELTA);
        assertEquals(0.0, sky.getY(), DELTA);
        assertEquals(-1.0, sky.getZ(), DELTA);
    }

    @Test
    public void project_east() {
        SkyCoordinate sky = projector.project(90.0, 0.0);
        assertEquals(1.0, sky.getX(), DELTA);
        assertEquals(0.0, sky.getY(), DELTA);
        assertEquals(0.0, sky.getZ(), DELTA);
    }

    @Test
    public void project_south() {
        SkyCoordinate sky = projector.project(180.0, 0.0);
        assertEquals(0.0, sky.getX(), DELTA);
        assertEquals(0.0, sky.getY(), DELTA);
        assertEquals(1.0, sky.getZ(), DELTA);
    }

    @Test
    public void project_west() {
        SkyCoordinate sky = projector.project(270.0, 0.0);
        assertEquals(-1.0, sky.getX(), DELTA);
        assertEquals(0.0, sky.getY(), DELTA);
        assertEquals(0.0, sky.getZ(), DELTA);
    }

    @Test
    public void projectNadirl() {
        SkyCoordinate sky = projector.project(0.0, -90.0);
        assertEquals(-90.0, sky.getAltitudeDegrees(), DELTA);
        assertEquals(0.0, sky.getX(), DELTA);
        assertEquals(-1.0, sky.getY(), DELTA);
        assertEquals(0.0, sky.getZ(), DELTA);
        assertFalse(sky.getHorizontal().isAboveHorizon());
    }

    @Test
    public void toCartesian_roundTrip() {
        HorizontalCoordinate original = new HorizontalCoordinate(123.4, 45.6);
        CartesianCoordinate cart = projector.toCartesian(original);
        HorizontalCoordinate result = projector.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void isVisible_positiveAltitude() {
        assertTrue(projector.isVisible(45.0));
    }

    @Test
    public void isVisible_zeroAltitude() {
        assertFalse(projector.isVisible(0.0));
    }

    @Test
    public void isVisible_negativeAltitude() {
        assertFalse(projector.isVisible(-10.0));
    }

    @Test
    public void isVisible_withCoordinate() {
        assertTrue(projector.isVisible(new HorizontalCoordinate(0.0, 45.0)));
        assertFalse(projector.isVisible(new HorizontalCoordinate(0.0, 0.0)));
    }

    @Test
    public void describeVisibility_above() {
        assertEquals("above horizon", projector.describeVisibility(45.0));
    }

    @Test
    public void describeVisibility_below() {
        assertEquals("below horizon", projector.describeVisibility(-5.0));
    }

    @Test
    public void project_returnsNonNull() {
        assertNotNull(projector.project(0.0, 0.0));
    }

    @Test
    public void project_containBothRepresentations() {
        SkyCoordinate sky = projector.project(45.0, 30.0);
        assertNotNull(sky.getHorizontal());
        assertNotNull(sky.getCartesian());
    }
}
