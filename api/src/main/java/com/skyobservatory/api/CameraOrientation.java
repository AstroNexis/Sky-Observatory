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
 * Immutable orientation of a camera or device in the local topocentric frame.
 *
 * <p>All angles are in degrees and represent the orientation of the camera
 * relative to the world. The SDK computes view frustum culling from these
 * values but never accesses device sensors directly.</p>
 *
 * <p>Coordinate conventions:</p>
 * <ul>
 *   <li><b>Yaw</b>: rotation around the up (Y) axis. 0&deg; = facing north,
 *       positive = turning right (eastward). Range (-180, 180] or [0, 360).</li>
 *   <li><b>Pitch</b>: rotation around the right (X) axis. 0&deg; = level,
 *       positive = tilting up toward zenith. Range [-90, 90].</li>
 *   <li><b>Roll</b>: rotation around the forward (Z) axis. 0&deg; = level,
 *       positive = rolling clockwise. Range (-180, 180].</li>
 * </ul>
 *
 * Instances are immutable.
 */
public final class CameraOrientation {

    private final double yawDegrees;
    private final double pitchDegrees;
    private final double rollDegrees;

    /**
     * @param yawDegrees   yaw in degrees; 0 = north, positive = eastward
     * @param pitchDegrees pitch in degrees; 0 = level, positive = up
     * @param rollDegrees  roll in degrees; 0 = level, positive = clockwise
     */
    public CameraOrientation(double yawDegrees, double pitchDegrees, double rollDegrees) {
        this.yawDegrees = yawDegrees;
        this.pitchDegrees = pitchDegrees;
        this.rollDegrees = rollDegrees;
    }

    /** Returns yaw in degrees. */
    public double getYawDegrees() { return yawDegrees; }

    /** Returns pitch in degrees. */
    public double getPitchDegrees() { return pitchDegrees; }

    /** Returns roll in degrees. */
    public double getRollDegrees() { return rollDegrees; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CameraOrientation)) return false;
        CameraOrientation other = (CameraOrientation) obj;
        return Double.compare(yawDegrees, other.yawDegrees) == 0
                && Double.compare(pitchDegrees, other.pitchDegrees) == 0
                && Double.compare(rollDegrees, other.rollDegrees) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(yawDegrees);
        result = 31 * result + Double.hashCode(pitchDegrees);
        result = 31 * result + Double.hashCode(rollDegrees);
        return result;
    }

    @Override
    public String toString() {
        return "CameraOrientation{yaw=" + yawDegrees + "\u00b0"
                + ", pitch=" + pitchDegrees + "\u00b0"
                + ", roll=" + rollDegrees + "\u00b0}";
    }
}
