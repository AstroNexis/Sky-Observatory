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

import com.skyobservatory.api.CameraOrientation;
import com.skyobservatory.api.CartesianCoordinate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrientationProjectorTest {

    private static final double DELTA = 1e-9;
    private final OrientationProjector projector = new OrientationProjector();

    @Test
    public void facingNorth_objectAhead_returnsPositiveZ() {
        CameraOrientation orient = new CameraOrientation(0.0, 0.0, 0.0);
        // World: north is -Z. Camera facing north: ahead = -Z world -> +Z camera.
        CartesianCoordinate north = new CartesianCoordinate(0.0, 0.0, -1.0);
        CartesianCoordinate result = projector.transform(north, orient);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(0.0, result.getY(), DELTA);
        assertEquals(1.0, result.getZ(), DELTA);
    }

    @Test
    public void facingNorth_objectBehind_returnsNegativeZ() {
        CameraOrientation orient = new CameraOrientation(0.0, 0.0, 0.0);
        // South is +Z world, behind the north-facing camera -> -Z camera.
        CartesianCoordinate south = new CartesianCoordinate(0.0, 0.0, 1.0);
        CartesianCoordinate result = projector.transform(south, orient);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(0.0, result.getY(), DELTA);
        assertEquals(-1.0, result.getZ(), DELTA);
    }

    @Test
    public void facingNorth_objectEast_returnsPositiveX() {
        CameraOrientation orient = new CameraOrientation(0.0, 0.0, 0.0);
        // East is +X world, right of north-facing camera -> +X camera.
        CartesianCoordinate east = new CartesianCoordinate(1.0, 0.0, 0.0);
        CartesianCoordinate result = projector.transform(east, orient);
        assertEquals(1.0, result.getX(), DELTA);
    }

    @Test
    public void facingNorth_objectWest_returnsNegativeX() {
        CameraOrientation orient = new CameraOrientation(0.0, 0.0, 0.0);
        CartesianCoordinate west = new CartesianCoordinate(-1.0, 0.0, 0.0);
        CartesianCoordinate result = projector.transform(west, orient);
        assertEquals(-1.0, result.getX(), DELTA);
    }

    @Test
    public void facingNorth_objectZenith_returnsPositiveY() {
        CameraOrientation orient = new CameraOrientation(0.0, 0.0, 0.0);
        CartesianCoordinate up = new CartesianCoordinate(0.0, 1.0, 0.0);
        CartesianCoordinate result = projector.transform(up, orient);
        assertEquals(1.0, result.getY(), DELTA);
    }

    @Test
    public void facingEast_objectAhead_becomesPositiveZ() {
        CameraOrientation orient = new CameraOrientation(90.0, 0.0, 0.0);
        // East is +X world. Camera facing east: ahead = +X world -> +Z camera.
        CartesianCoordinate east = new CartesianCoordinate(1.0, 0.0, 0.0);
        CartesianCoordinate result = projector.transform(east, orient);
        assertEquals(1.0, result.getZ(), DELTA);
    }

    @Test
    public void pitchUp_shiftsZenithBelow() {
        CameraOrientation orient = new CameraOrientation(0.0, 45.0, 0.0);
        // Zenith is (0,1,0) world. With 45 deg pitch up, zenith is now 45 deg
        // above center in camera space, so camY = sin(45) = 0.707, camZ = cos(45) = 0.707.
        CartesianCoordinate zenith = new CartesianCoordinate(0.0, 1.0, 0.0);
        CartesianCoordinate result = projector.transform(zenith, orient);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(0.7071067811865476, result.getY(), 1e-6);
        assertEquals(0.7071067811865476, result.getZ(), 1e-6);
    }

    @Test
    public void roll90_eastBecomesNegativeY() {
        CameraOrientation orient = new CameraOrientation(0.0, 0.0, 90.0);
        // With 90 deg roll clockwise, camera right = world down, camera up = world west.
        // East (+X world) should appear as camera down (-Y).
        CartesianCoordinate east = new CartesianCoordinate(1.0, 0.0, 0.0);
        CartesianCoordinate result = projector.transform(east, orient);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(-1.0, result.getY(), DELTA);
        assertEquals(0.0, result.getZ(), DELTA);
    }

    @Test
    public void returnsNonNull() {
        CartesianCoordinate result = projector.transform(
                new CartesianCoordinate(0.0, 0.0, -1.0),
                new CameraOrientation(0.0, 0.0, 0.0));
        assertNotNull(result);
    }
}
