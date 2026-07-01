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
 * An immutable, self-contained snapshot of the entire sky at a given time
 * and location.
 *
 * <p>{@link SkySnapshot} is the primary output type for rendering pipelines.
 * A renderer can render a complete frame using nothing but a single snapshot:
 * iterate {@link #getObjects()} to position and draw each celestial body,
 * and consult {@link #getOrientation()} to align the virtual camera.</p>
 *
 * <p>The snapshot contains:</p>
 * <ul>
 *   <li>The observation time ({@link AstroTime})</li>
 *   <li>The observer location ({@link Observer})</li>
 *   <li>All computed celestial objects with positions and visibility
 *       ({@link ObservableObject})</li>
 *   <li>An optional device orientation ({@link DeviceOrientation}) for
 *       sensor-driven rendering -- the SDK accepts orientation data but
 *       never accesses device sensors directly</li>
 * </ul>
 *
 * <p>The snapshot is designed to support arbitrary update frequencies
 * (1 Hz, 10 Hz, 30 Hz, 60 Hz, or higher). Creating a snapshot is an
 * stateless computation: the caller decides when to produce a new one.</p>
 *
 * <p>Instances are immutable and thread-safe.</p>
 */
public final class SkySnapshot {

    private final AstroTime time;
    private final Observer observer;
    private final DeviceOrientation orientation;
    private final List<ObservableObject> objects;

    private SkySnapshot(Builder builder) {
        this.time = builder.time;
        this.observer = builder.observer;
        this.orientation = builder.orientation;
        this.objects = Collections.unmodifiableList(new ArrayList<>(builder.objects));
    }

    /** Returns the observation time for this snapshot. */
    public AstroTime getTime() { return time; }

    /** Returns the observer location. */
    public Observer getObserver() { return observer; }

    /**
     * Returns the device orientation if set, or {@code null}.
     *
     * When present, this allows a renderer to align the virtual camera with
     * the physical device without the SDK directly accessing sensor hardware.
     */
    public DeviceOrientation getOrientation() { return orientation; }

    /**
     * Returns an unmodifiable list of all celestial objects in this snapshot,
     * each with its computed position and visibility state.
     */
    public List<ObservableObject> getObjects() { return objects; }

    /**
     * Returns an unmodifiable list of only the objects that are currently
     * above the horizon.
     */
    public List<ObservableObject> getVisibleObjects() {
        List<ObservableObject> visible = new ArrayList<>();
        for (ObservableObject obj : objects) {
            if (obj.getVisibility() == VisibilityState.VISIBLE) {
                visible.add(obj);
            }
        }
        return Collections.unmodifiableList(visible);
    }

    /**
     * Returns the number of objects in this snapshot.
     */
    public int getObjectCount() { return objects.size(); }

    /**
     * Returns the number of objects above the horizon in this snapshot.
     */
    public int getVisibleObjectCount() {
        int count = 0;
        for (ObservableObject obj : objects) {
            if (obj.getVisibility() == VisibilityState.VISIBLE) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "SkySnapshot{time=" + time
                + ", observer=" + observer
                + ", objects=" + objects.size()
                + ", visible=" + getVisibleObjectCount()
                + "}";
    }

    /**
     * Builder for {@link SkySnapshot}.
     *
     * All fields except {@link #orientation(DeviceOrientation)} are mandatory.
     */
    public static final class Builder {

        private final AstroTime time;
        private final Observer observer;
        private final List<ObservableObject> objects;
        private DeviceOrientation orientation;

        /**
         * @param time     the observation time; must not be null
         * @param observer the observer location; must not be null
         * @param objects  the list of celestial objects and their positions;
         *                 a defensive copy is made; must not be null
         */
        public Builder(AstroTime time, Observer observer, List<ObservableObject> objects) {
            if (time == null) throw new IllegalArgumentException("time must not be null");
            if (observer == null) throw new IllegalArgumentException("observer must not be null");
            if (objects == null) throw new IllegalArgumentException("objects must not be null");
            this.time = time;
            this.observer = observer;
            this.objects = new ArrayList<>(objects);
        }

        /**
         * Sets the device orientation for this snapshot.
         *
         * @param orientation the device orientation; may be null to clear
         * @return this builder
         */
        public Builder orientation(DeviceOrientation orientation) {
            this.orientation = orientation;
            return this;
        }

        /** Builds the {@link SkySnapshot}. */
        public SkySnapshot build() {
            return new SkySnapshot(this);
        }
    }
}
