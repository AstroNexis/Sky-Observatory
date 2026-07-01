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

/**
 * Determines whether a {@link CartesianCoordinate} lies inside the viewport
 * frustum.
 *
 * <p>The check is performed in camera-relative space. A coordinate is inside
 * the frustum when its camera-relative azimuth and altitude fall within the
 * horizontal and vertical half-angles defined by the viewport.</p>
 *
 * <p>This class is package-private. Callers use
 * {@link VisibilityResolver} instead.</p>
 */
final class ViewFrustumCalculator {

    private static final double EPSILON = 1e-10;

    private final OrientationProjector projector;

    ViewFrustumCalculator() {
        this.projector = new OrientationProjector();
    }

    /**
     * Returns {@code true} when the world-space coordinate lies inside the
     * viewport frustum.
     *
     * @param world   the world-space Cartesian direction; must not be null
     * @param viewport the camera viewport; must not be null
     * @return {@code true} if the coordinate is inside the frustum
     */
    boolean isInsideFrustum(CartesianCoordinate world, Viewport viewport) {
        CameraOrientation orientation = viewport.getOrientation();
        CartesianCoordinate camera = projector.transform(world, orientation);

        double z = camera.getZ();
        if (z <= 0.0) {
            return false;
        }

        double horizAngle = Math.toDegrees(Math.atan2(camera.getX(), z));
        double vertAngle = Math.toDegrees(Math.atan2(camera.getY(), z));

        double halfH = viewport.getHorizontalHalfAngleDegrees();
        double halfV = viewport.getVerticalHalfAngleDegrees();

        return horizAngle <= halfH + EPSILON
                && horizAngle >= -(halfH + EPSILON)
                && vertAngle <= halfV + EPSILON
                && vertAngle >= -(halfV + EPSILON);
    }
}
