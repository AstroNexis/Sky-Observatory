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
import com.skyobservatory.api.Viewport;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ViewFrustumCalculatorTest {

    private static final double DELTA = 1e-9;
    private final ViewFrustumCalculator frustum = new ViewFrustumCalculator();

    @Test
    public void objectDirectlyAhead_isInside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // Facing north. Directly ahead = north = (0, 0, -1) in world.
        CartesianCoordinate north = new CartesianCoordinate(0.0, 0.0, -1.0);
        assertTrue(frustum.isInsideFrustum(north, viewport));
    }

    @Test
    public void objectBehindCamera_isOutside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // Behind north-facing camera = south = (0, 0, 1) in world.
        CartesianCoordinate south = new CartesianCoordinate(0.0, 0.0, 1.0);
        assertFalse(frustum.isInsideFrustum(south, viewport));
    }

    @Test
    public void objectJustOnHorizontalEdge_isInside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 30 degrees right of north = (sin30, 0, -cos30)
        double angle = Math.toRadians(30.0);
        CartesianCoordinate edge = new CartesianCoordinate(
                Math.sin(angle), 0.0, -Math.cos(angle));
        assertTrue(frustum.isInsideFrustum(edge, viewport));
    }

    @Test
    public void objectJustPastHorizontalEdge_isOutside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 31 degrees right of north = beyond the 30 degree half-FOV.
        double angle = Math.toRadians(31.0);
        CartesianCoordinate outside = new CartesianCoordinate(
                Math.sin(angle), 0.0, -Math.cos(angle));
        assertFalse(frustum.isInsideFrustum(outside, viewport));
    }

    @Test
    public void objectJustAboveVertically_isInside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 22.5 degrees up from north = (0, sin22.5, -cos22.5)
        double angle = Math.toRadians(22.5);
        CartesianCoordinate above = new CartesianCoordinate(
                0.0, Math.sin(angle), -Math.cos(angle));
        assertTrue(frustum.isInsideFrustum(above, viewport));
    }

    @Test
    public void objectJustAboveVertically_isOutside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 23 degrees up from north = beyond the 22.5 degree half-FOV.
        double angle = Math.toRadians(23.0);
        CartesianCoordinate above = new CartesianCoordinate(
                0.0, Math.sin(angle), -Math.cos(angle));
        assertFalse(frustum.isInsideFrustum(above, viewport));
    }

    @Test
    public void objectJustBelowVertically_isOutside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 23 degrees down from north.
        double angle = Math.toRadians(23.0);
        CartesianCoordinate below = new CartesianCoordinate(
                0.0, -Math.sin(angle), -Math.cos(angle));
        assertFalse(frustum.isInsideFrustum(below, viewport));
    }

    @Test
    public void objectJustBelowVertically_isInside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 22.5 degrees down from north.
        double angle = Math.toRadians(22.5);
        CartesianCoordinate below = new CartesianCoordinate(
                0.0, -Math.sin(angle), -Math.cos(angle));
        assertTrue(frustum.isInsideFrustum(below, viewport));
    }

    @Test
    public void wideViewport_acceptsWiderAngle() {
        Viewport viewport = new Viewport(120.0, 90.0, new CameraOrientation(0.0, 0.0, 0.0));
        // 55 degrees right of north = inside the 60 degree half-FOV.
        double angle = Math.toRadians(55.0);
        CartesianCoordinate wide = new CartesianCoordinate(
                Math.sin(angle), 0.0, -Math.cos(angle));
        assertTrue(frustum.isInsideFrustum(wide, viewport));
    }

    @Test
    public void objectFacingSouth_aheadIsPositiveZ() {
        // Camera facing south: ahead = (0, 0, 1) in world.
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(180.0, 0.0, 0.0));
        CartesianCoordinate south = new CartesianCoordinate(0.0, 0.0, 1.0);
        assertTrue(frustum.isInsideFrustum(south, viewport));
    }

    @Test
    public void objectFacingSouth_BehindIsNegativeZ() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(180.0, 0.0, 0.0));
        CartesianCoordinate north = new CartesianCoordinate(0.0, 0.0, -1.0);
        assertFalse(frustum.isInsideFrustum(north, viewport));
    }

    // Left-of-viewport tests
    @Test
    public void objectJustLeftHorizontally_isInside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        double angle = Math.toRadians(30.0);
        CartesianCoordinate left = new CartesianCoordinate(
                -Math.sin(angle), 0.0, -Math.cos(angle));
        assertTrue(frustum.isInsideFrustum(left, viewport));
    }

    @Test
    public void objectJustLeftHorizontally_isOutside() {
        Viewport viewport = new Viewport(60.0, 45.0, new CameraOrientation(0.0, 0.0, 0.0));
        double angle = Math.toRadians(31.0);
        CartesianCoordinate left = new CartesianCoordinate(
                -Math.sin(angle), 0.0, -Math.cos(angle));
        assertFalse(frustum.isInsideFrustum(left, viewport));
    }
}
