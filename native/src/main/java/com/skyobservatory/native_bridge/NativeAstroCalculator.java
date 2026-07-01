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

import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EquatorialCoordinates;
import com.skyobservatory.api.NativeException;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;

public class NativeAstroCalculator {

    private static final double ERROR_FLAG = -1.0;

    public NativeAstroCalculator() {}

    public String getNativeLibraryVersion() {
        return NativeGateway.nativeGetVersion();
    }

    public PositionResult calculatePosition(
            CelestialObject target,
            Observer observer,
            AstroTime time) throws NativeException {

        double[] r = NativeGateway.nativeCalculatePosition(
                target.getNaifId(),
                time.getJulianDateTT(),
                observer.getLatitudeDegrees(),
                observer.getLongitudeDegrees(),
                observer.getAltitudeMeters());

        if (r.length < 7 || r[6] == ERROR_FLAG) {
            throw new NativeException(
                    "Native calculation failed for " + target.getName());
        }

        double azimuth     = r[0];
        double altitude    = r[1];
        double raHours     = r[2];
        double decDeg      = r[3];
        double distanceAu  = r[4];

        PositionResult.Builder builder = new PositionResult.Builder(
                azimuth, altitude, target, observer, time);

        if (!Double.isNaN(raHours) && !Double.isNaN(decDeg)) {
            builder.equatorial(new EquatorialCoordinates(raHours * 15.0, decDeg));
        }

        if (distanceAu > 0.0) {
            builder.distanceAu(distanceAu);
        }

        return builder.build();
    }
}
