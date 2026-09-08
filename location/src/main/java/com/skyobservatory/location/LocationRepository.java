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

package com.skyobservatory.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * Obtains the device's current location via {@link FusedLocationProviderClient}.
 */
public final class LocationRepository {

    public interface LocationCallback {
        void onLocation(Location location);
        void onError(String reason);
    }

    private final FusedLocationProviderClient fusedClient;

    public LocationRepository(Context context) {
        fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Requests a single current location fix.
     *
     * Callers are responsible for verifying that location permission is held.
     */
    public void getCurrentLocation(LocationCallback callback) {
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
