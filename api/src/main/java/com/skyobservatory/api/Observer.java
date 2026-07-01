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
 * Represents a ground-based or space-based observer.
 *
 * All coordinates are in degrees (latitude, longitude) and meters (altitude).
 * Instances are immutable.
 */
public final class Observer {

    private final double latitudeDegrees;
    private final double longitudeDegrees;
    private final double altitudeMeters;

    /**
     * @param latitudeDegrees  geodetic latitude in degrees, range [-90, 90]
     * @param longitudeDegrees geodetic longitude in degrees, range [-180, 180]
     * @param altitudeMeters   altitude above the WGS84 ellipsoid in meters
     */
    public Observer(double latitudeDegrees, double longitudeDegrees, double altitudeMeters) {
        this.latitudeDegrees = latitudeDegrees;
        this.longitudeDegrees = longitudeDegrees;
        this.altitudeMeters = altitudeMeters;
    }

    public double getLatitudeDegrees() {
        return latitudeDegrees;
    }

    public double getLongitudeDegrees() {
        return longitudeDegrees;
    }

    public double getAltitudeMeters() {
        return altitudeMeters;
    }

    @Override
    public String toString() {
        return "Observer{"
                + "lat=" + latitudeDegrees
                + ", lon=" + longitudeDegrees
                + ", alt=" + altitudeMeters + "m"
                + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Observer)) return false;
        Observer that = (Observer) other;
        return Double.compare(that.latitudeDegrees, latitudeDegrees) == 0
                && Double.compare(that.longitudeDegrees, longitudeDegrees) == 0
                && Double.compare(that.altitudeMeters, altitudeMeters) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(latitudeDegrees);
        result = 31 * result + Double.hashCode(longitudeDegrees);
        result = 31 * result + Double.hashCode(altitudeMeters);
        return result;
    }
}
