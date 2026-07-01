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
 * An immutable representation of horizontal (alt-azimuth) coordinates.
 *
 * <p>Coordinate conventions:</p>
 * <ul>
 *   <li>Azimuth: measured eastward from true north -- 0&deg; = North, 90&deg; = East,
 *       180&deg; = South, 270&deg; = West. Values are in the range [0, 360).</li>
 *   <li>Altitude: angular height above the astronomical horizon -- 0&deg; = on the
 *       horizon, +90&deg; = zenith (directly overhead), negative values = below
 *       the horizon. Values are in the range [-90, 90].</li>
 * </ul>
 *
 * All angular values are in degrees. Instances are immutable.
 */
public final class HorizontalCoordinate {

    private final double azimuthDegrees;
    private final double altitudeDegrees;

    /**
     * @param azimuthDegrees  azimuth in degrees [0, 360)
     * @param altitudeDegrees altitude in degrees [-90, 90]
     */
    public HorizontalCoordinate(double azimuthDegrees, double altitudeDegrees) {
        this.azimuthDegrees = azimuthDegrees;
        this.altitudeDegrees = altitudeDegrees;
    }

    /** Returns the azimuth in degrees [0, 360). */
    public double getAzimuthDegrees() { return azimuthDegrees; }

    /** Returns the altitude in degrees [-90, 90]. */
    public double getAltitudeDegrees() { return altitudeDegrees; }

    /** Returns {@code true} when this coordinate is above the astronomical horizon. */
    public boolean isAboveHorizon() { return altitudeDegrees > 0.0; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HorizontalCoordinate)) return false;
        HorizontalCoordinate other = (HorizontalCoordinate) obj;
        return Double.compare(azimuthDegrees, other.azimuthDegrees) == 0
                && Double.compare(altitudeDegrees, other.altitudeDegrees) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(azimuthDegrees);
        result = 31 * result + Double.hashCode(altitudeDegrees);
        return result;
    }

    @Override
    public String toString() {
        return "HorizontalCoordinate{az=" + azimuthDegrees + "\u00b0, alt=" + altitudeDegrees + "\u00b0}";
    }
}
