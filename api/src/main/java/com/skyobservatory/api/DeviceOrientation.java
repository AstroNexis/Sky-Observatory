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

/**
 * An immutable representation of the device's orientation in the local
 * topocentric frame.
 *
 * <p>Orientation is expressed as a rotation: the forward direction (where the
 * device camera or user is looking) and the up direction. Together these define
 * a full orthonormal basis suitable for constructing a view matrix.</p>
 *
 * <p>The coordinate system follows the same convention as
 * {@link CartesianCoordinate}:
 * <ul>
 *   <li><b>X</b>: east-west axis. Positive = east, negative = west.</li>
 *   <li><b>Y</b>: up-down axis. Positive = up (zenith), negative = down (nadir).</li>
 *   <li><b>Z</b>: north-south axis. Positive = south, negative = north.</li>
 * </ul>
 *
 * Instances are immutable.
 */
public final class DeviceOrientation {

    private final CartesianCoordinate forward;
    private final CartesianCoordinate up;

    /**
     * @param forward the forward direction unit vector (where the device points)
     * @param up      the upward direction unit vector (relative to the device)
     */
    public DeviceOrientation(CartesianCoordinate forward, CartesianCoordinate up) {
        if (forward == null) throw new IllegalArgumentException("forward must not be null");
        if (up == null) throw new IllegalArgumentException("up must not be null");
        this.forward = forward;
        this.up = up;
    }

    /** Returns the forward direction unit vector. */
    public CartesianCoordinate getForward() { return forward; }

    /** Returns the upward direction unit vector. */
    public CartesianCoordinate getUp() { return up; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeviceOrientation)) return false;
        DeviceOrientation other = (DeviceOrientation) obj;
        return forward.equals(other.forward) && up.equals(other.up);
    }

    @Override
    public int hashCode() {
        int result = forward.hashCode();
        result = 31 * result + up.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DeviceOrientation{forward=" + forward + ", up=" + up + "}";
    }
}
