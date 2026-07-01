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

package com.skyobservatory.native_bridge;

/**
 * The single crossing point between Java and the native layer.
 *
 * All JNI calls pass through this class. No other class in any module calls
 * System.loadLibrary or declares native methods. This boundary makes it
 * straightforward to add mocking, versioning, or alternative implementations
 * in future phases.
 *
 * This class is package-private to the native module. Engine module accesses
 * its functionality through {@link NativeAstroCalculator}.
 */
final class NativeGateway {

    private static final String LIBRARY_NAME = "astro_bridge";

    static {
        System.loadLibrary(LIBRARY_NAME);
    }

    // No instances.
    private NativeGateway() {}

    /**
     * Returns the version string reported by the native library.
     * Used at startup to confirm the library loaded and linked correctly.
     */
    static native String nativeGetVersion();

    /**
     * Computes the topocentric azimuth of a solar system body.
     *
     * @param naifId          NAIF integer identifier of the target
     * @param julianDateTT    observation time as Julian Date in TT
     * @param latitudeDeg     observer geodetic latitude in degrees
     * @param longitudeDeg    observer geodetic longitude in degrees
     * @param altitudeMeters  observer altitude above WGS84 ellipsoid in metres
     * @return azimuth in degrees [0, 360), or -1.0 if the call failed
     */
    static native double nativeCalculateAzimuth(
            int naifId,
            double julianDateTT,
            double latitudeDeg,
            double longitudeDeg,
            double altitudeMeters);

    /**
     * Computes the topocentric altitude of a solar system body.
     *
     * @param naifId          NAIF integer identifier of the target
     * @param julianDateTT    observation time as Julian Date in TT
     * @param latitudeDeg     observer geodetic latitude in degrees
     * @param longitudeDeg    observer geodetic longitude in degrees
     * @param altitudeMeters  observer altitude above WGS84 ellipsoid in metres
     * @return altitude in degrees [-90, 90], or -999.0 if the call failed
     */
    static native double nativeCalculateAltitude(
            int naifId,
            double julianDateTT,
            double latitudeDeg,
            double longitudeDeg,
            double altitudeMeters);

    /**
     * Computes azimuth, altitude, RA, dec, distance, and radial velocity
     * in a single native invocation. More efficient than separate calls.
     *
     * @return double[7]: [azimuth, altitude, raHours, decDeg, distanceAu,
     *         radialVelocity, errorFlag]. errorFlag 0 = success, -1 = error.
     */
    static native double[] nativeCalculatePosition(
            int naifId,
            double julianDateTT,
            double latitudeDeg,
            double longitudeDeg,
            double altitudeMeters);
}
