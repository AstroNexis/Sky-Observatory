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

package com.skyobservatory.renderer;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * Obtains the device's current location via {@link FusedLocationProviderClient}.
 *
 * Callers are responsible for verifying that location permission has been granted
 * before calling {@link #getCurrentLocation(LocationCallback)}. This class does not
 * check permissions; it assumes they are already held.
 */
final class LocationRepository {

    interface LocationCallback {
        void onLocation(Location location);
        void onError(String reason);
    }

    private final FusedLocationProviderClient fusedClient;

    LocationRepository(Context context) {
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Requests a single current location fix.
     *
     * Uses {@link Priority#PRIORITY_HIGH_ACCURACY} with a 10-second timeout and a
     * 30-second max accepted cache age. The callback is invoked on the main thread.
     *
     * @param callback receives the location or a user-readable error description
     */
    void getCurrentLocation(LocationCallback callback) {
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(10_000)
                .setMaxUpdateAgeMillis(30_000)
                .build();

        CancellationTokenSource cts = new CancellationTokenSource();

        try {
            fusedClient.getCurrentLocation(request, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocation(location);
                        } else {
                            callback.onError("Location unavailable. Ensure GPS is enabled.");
                        }
                    })
                    .addOnFailureListener(e ->
                            callback.onError("Location request failed: " + e.getMessage()));
        } catch (SecurityException e) {
            callback.onError("Location permission was revoked.");
        }
    }
}
