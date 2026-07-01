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

import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.Viewport;
import com.skyobservatory.api.VisibleSkyRegion;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines the set of celestial objects visible through a given viewport.
 *
 * <p>The resolver first filters objects that are above the astronomical
 * horizon (already computed in the snapshot), then checks whether each
 * object falls inside the viewport frustum using the
 * {@link ViewFrustumCalculator}.</p>
 *
 * <p>This class is package-private. Callers use
 * {@link com.skyobservatory.api.AstroEngine#calculateVisibleRegion(
 * SkySnapshot, Viewport)} instead.</p>
 */
public final class VisibilityResolver {

    private final ViewFrustumCalculator frustum;

    public VisibilityResolver() {
        this.frustum = new ViewFrustumCalculator();
    }

    /**
     * Splits the objects in the snapshot into visible and hidden sets based
     * on the viewport frustum.
     *
     * @param snapshot the sky snapshot to evaluate; must not be null
     * @param viewport the camera viewport defining the frustum; must not be null
     * @return a {@link VisibleSkyRegion} with visible and hidden object lists
     */
    public VisibleSkyRegion resolve(SkySnapshot snapshot, Viewport viewport) {
        List<ObservableObject> visible = new ArrayList<>();
        List<ObservableObject> hidden = new ArrayList<>();

        for (ObservableObject obj : snapshot.getObjects()) {
            switch (obj.getVisibility()) {
                case VISIBLE:
                    CartesianCoordinate world = obj.getPosition().getCartesian();
                    if (frustum.isInsideFrustum(world, viewport)) {
                        visible.add(obj);
                    } else {
                        hidden.add(obj);
                    }
                    break;
                default:
                    hidden.add(obj);
                    break;
            }
        }

        return new VisibleSkyRegion(snapshot, viewport, visible, hidden);
    }
}
