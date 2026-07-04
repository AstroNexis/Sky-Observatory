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
 * Equatorial sky coordinates: right ascension and declination.
 *
 * Right ascension is measured eastward along the celestial equator from the
 * vernal equinox. Declination is measured north (+) or south (-) of the
 * celestial equator.
 *
 * The epoch of the coordinate frame (e.g. J2000.0, ICRS, apparent) is
 * determined by the context in which this object is produced and must be
 * documented by the producing layer.
 *
 * Instances are immutable.
 */
public final class EquatorialCoordinates {

    private final double rightAscensionDegrees;
    private final double declinationDegrees;

    /**
     * @param rightAscensionDegrees right ascension in degrees [0, 360)
     * @param declinationDegrees    declination in degrees [-90, 90]
     */
    public EquatorialCoordinates(double rightAscensionDegrees, double declinationDegrees) {
        this.rightAscensionDegrees = rightAscensionDegrees;
        this.declinationDegrees = declinationDegrees;
    }

    /**
     * Returns right ascension in degrees in the range [0, 360).
     */
    public double getRightAscensionDegrees() {
        return rightAscensionDegrees;
    }

    /**
     * Returns right ascension in hours in the range [0, 24).
     */
    public double getRightAscensionHours() {
        return rightAscensionDegrees / 15.0;
    }

    /**
     * Returns declination in degrees in the range [-90, 90].
     */
    public double getDeclinationDegrees() {
        return declinationDegrees;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EquatorialCoordinates)) return false;
        EquatorialCoordinates other = (EquatorialCoordinates) obj;
        return Double.compare(rightAscensionDegrees, other.rightAscensionDegrees) == 0
                && Double.compare(declinationDegrees, other.declinationDegrees) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(rightAscensionDegrees);
        result = 31 * result + Double.hashCode(declinationDegrees);
        return result;
    }

    @Override
    public String toString() {
        return "EquatorialCoordinates{RA=" + rightAscensionDegrees + "\u00b0"
                + ", Dec=" + declinationDegrees + "\u00b0}";
    }
}
