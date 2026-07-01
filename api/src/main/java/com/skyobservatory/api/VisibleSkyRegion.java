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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of a viewport visibility calculation.
 *
 * <p>{@link VisibleSkyRegion} contains all objects from a {@link SkySnapshot}
 * split into visible and hidden sets based on the {@link Viewport} frustum.
 * A renderer can draw the frame by iterating {@link #getVisibleObjects()}
 * and skipping {@link #getHiddenObjects()}.</p>
 *
 * <p>The original snapshot and viewport are preserved for context.</p>
 *
 * Instances are immutable.
 */
public final class VisibleSkyRegion {

    private final SkySnapshot snapshot;
    private final Viewport viewport;
    private final List<ObservableObject> visible;
    private final List<ObservableObject> hidden;

    /**
     * @param snapshot the original sky snapshot; must not be null
     * @param viewport the viewport used for culling; must not be null
     * @param visible  objects inside the viewport frustum; must not be null
     * @param hidden   objects outside the viewport frustum; must not be null
     */
    public VisibleSkyRegion(
            SkySnapshot snapshot,
            Viewport viewport,
            List<ObservableObject> visible,
            List<ObservableObject> hidden) {
        if (snapshot == null) throw new IllegalArgumentException("snapshot must not be null");
        if (viewport == null) throw new IllegalArgumentException("viewport must not be null");
        if (visible == null) throw new IllegalArgumentException("visible must not be null");
        if (hidden == null) throw new IllegalArgumentException("hidden must not be null");
        this.snapshot = snapshot;
        this.viewport = viewport;
        this.visible = Collections.unmodifiableList(new ArrayList<>(visible));
        this.hidden = Collections.unmodifiableList(new ArrayList<>(hidden));
    }

    /** Returns the original sky snapshot. */
    public SkySnapshot getSnapshot() { return snapshot; }

    /** Returns the viewport used for culling. */
    public Viewport getViewport() { return viewport; }

    /** Returns the list of objects currently inside the viewport frustum. */
    public List<ObservableObject> getVisibleObjects() { return visible; }

    /** Returns the list of objects currently outside the viewport frustum. */
    public List<ObservableObject> getHiddenObjects() { return hidden; }

    /** Returns the number of visible objects. */
    public int getVisibleObjectCount() { return visible.size(); }

    /** Returns the total number of objects considered. */
    public int getTotalObjectCount() { return visible.size() + hidden.size(); }

    @Override
    public String toString() {
        return "VisibleSkyRegion{visible=" + visible.size()
                + ", hidden=" + hidden.size()
                + ", viewport=" + viewport + "}";
    }
}
