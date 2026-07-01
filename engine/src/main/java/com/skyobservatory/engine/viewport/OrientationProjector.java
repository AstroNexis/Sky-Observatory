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

/**
 * Transforms world (topocentric) Cartesian coordinates into
 * camera-relative coordinates.
 *
 * <p>World coordinate system (right-handed):</p>
 * <ul>
 *   <li><b>X</b>: east. Positive = east, negative = west.</li>
 *   <li><b>Y</b>: up. Positive = zenith, negative = nadir.</li>
 *   <li><b>Z</b>: south. Positive = south, negative = north.</li>
 * </ul>
 *
 * <p>Camera coordinate system (right-handed):</p>
 * <ul>
 *   <li><b>X</b>: camera right.</li>
 *   <li><b>Y</b>: camera up.</li>
 *   <li><b>Z</b>: camera forward (into the screen).</li>
 * </ul>
 *
 * <p>The transformation is derived from the camera's yaw (heading), pitch,
 * and roll. Yaw=0 points north (-Z world), pitch=0 is level, roll=0 is
 * level horizon.</p>
 *
 * <p>This class is package-private. Callers use
 * {@link com.skyobservatory.engine.viewport.VisibilityResolver}
 * instead.</p>
 */
final class OrientationProjector {

    /**
     * Transforms a world-space direction vector to camera-relative space.
     *
     * @param world       the world-space Cartesian direction; must not be null
     * @param orientation the camera orientation; must not be null
     * @return the direction in camera-relative space (x=right, y=up, z=forward)
     */
    CartesianCoordinate transform(CartesianCoordinate world, CameraOrientation orientation) {
        double yawRad = Math.toRadians(orientation.getYawDegrees());
        double pitchRad = Math.toRadians(orientation.getPitchDegrees());
        double rollRad = Math.toRadians(orientation.getRollDegrees());

        double cr = Math.cos(rollRad);
        double sr = Math.sin(rollRad);
        double cp = Math.cos(pitchRad);
        double sp = Math.sin(pitchRad);
        double cy = Math.cos(yawRad);
        double sy = Math.sin(yawRad);

        // Forward vector in world space.
        double fx = sy * cp;
        double fy = sp;
        double fz = -cy * cp;

        // Right vector in world space: cross(forward, world_up) then apply roll.
        double rx = -fz;
        double ry = 0.0;
        double rz = fx;
        double invLenR = 1.0 / Math.sqrt(rx * rx + ry * ry + rz * rz);
        rx *= invLenR;
        ry *= invLenR;
        rz *= invLenR;

        // Up vector in world space: cross(right, forward).
        double ux = ry * fz - rz * fy;
        double uy = rz * fx - rx * fz;
        double uz = rx * fy - ry * fx;

        // Apply roll: rotate right and up around forward.
        double rxr = rx * cr + ux * sr;
        double ryr = ry * cr + uy * sr;
        double rzr = rz * cr + uz * sr;

        double uxr = -rx * sr + ux * cr;
        double uyr = -ry * sr + uy * cr;
        double uzr = -rz * sr + uz * cr;

        // Project world point onto camera axes.
        double wX = world.getX();
        double wY = world.getY();
        double wZ = world.getZ();

        double camX = wX * rxr + wY * ryr + wZ * rzr;
        double camY = wX * uxr + wY * uyr + wZ * uzr;
        double camZ = wX * fx + wY * fy + wZ * fz;

        return new CartesianCoordinate(camX, camY, camZ);
    }
}
