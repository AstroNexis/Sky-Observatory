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

package com.skyobservatory.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Choreographer;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.skyobservatory.api.AstroEngine;
import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroSdk;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EphemerisResult;
import com.skyobservatory.api.EquatorialCoordinates;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.engine.EngineInitializer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Validation and verification application for the astronomy SDK pipeline.
 *
 * <h3>Purpose</h3>
 * Confirms that calculations flowing through
 * Application -> API -> Engine -> Native -> SuperNOVAS
 * are working correctly. Displays every ephemeris field derivable from
 * the SuperNOVAS pipeline in real time. Static physical catalog fields
 * (radius, mass, density, etc.) are not displayed here.
 *
 * <h3>Flow</h3>
 * <ol>
 *   <li>On start, the SDK is bootstrapped and location permission is requested
 *       if not already granted.</li>
 *   <li>Once permission is held, the current location is obtained via
 *       {@link LocationRepository}.</li>
 *   <li>A periodic refresh timer fires every {@link #REFRESH_INTERVAL_MS} ms,
 *       capturing {@link AstroTime#now()} and calling both
 *       {@link AstroEngine#calculatePosition} and
 *       {@link AstroEngine#calculateEphemeris} for the Sun.</li>
 *   <li>All returned fields are displayed; fields not computable from the
 *       current SuperNOVAS integration are shown as "unsupported".</li>
 * </ol>
 *
 * No manual interaction is required after launch.
 */
public class MainActivity extends AppCompatActivity implements Choreographer.FrameCallback {

    /** Refresh period in milliseconds. */
    private static final long REFRESH_INTERVAL_MS = 5_000L;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.systemDefault());

    /** Julian Date formatter for rise/set times (hours and minutes only). */
    private static final DateTimeFormatter RISE_SET_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss z")
                    .withZone(ZoneId.systemDefault());

    private static final String UNSUPPORTED = "unsupported";

    private TextView resultView;
    private LocationRepository locationRepository;

    private Location lastKnownLocation;
    private Choreographer choreographer;
    private boolean isRunning = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean granted = Boolean.TRUE.equals(
                                permissions.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(
                                permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (granted) {
                            fetchLocationThenStart();
                        } else {
                            showResult(getString(R.string.error_permission_denied));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.scroll_root),
                (v, insets) -> {
                    androidx.core.graphics.Insets bars = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        bootstrapSdk();

        resultView = findViewById(R.id.text_result);
        locationRepository = new LocationRepository(this);
        choreographer = Choreographer.getInstance();

        if (hasLocationPermission()) {
            fetchLocationThenStart();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        if (lastKnownLocation != null) {
            choreographer.postFrameCallback(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        choreographer.removeFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning || lastKnownLocation == null) {
            return;
        }

        calculateAndDisplay(lastKnownLocation);
        choreographer.postFrameCallback(this);
    }

    private void bootstrapSdk() {
        AstroSdk.registerProvider(EngineInitializer.provider());
        AstroSdk.initialize();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchLocationThenStart() {
        showResult(getString(R.string.status_obtaining_location));

        locationRepository.getCurrentLocation(new LocationRepository.LocationCallback() {
            @Override
            public void onLocation(Location location) {
                lastKnownLocation = location;
                if (isRunning) {
                    choreographer.postFrameCallback(MainActivity.this);
                }
            }

            @Override
            public void onError(String reason) {
                showResult(reason);
            }
        });
    }

    private void calculateAndDisplay(Location location) {
        try {
            AstroEngine engine = AstroSdk.getEngine();

            Observer observer = new Observer(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.hasAltitude() ? location.getAltitude() : 0.0);

            AstroTime time = AstroTime.now();

            PositionResult position  = engine.calculatePosition(CelestialObject.sun(), observer, time);
            EphemerisResult ephemeris = engine.calculateEphemeris(CelestialObject.sun(), observer, time);

            showResult(formatResult(location, time, position, ephemeris));

        } catch (AstroException e) {
            showResult(getString(R.string.error_calculation_failed, e.getMessage()));
        }
    }

    /**
     * Formats every astronomy calculation field returned by the SuperNOVAS pipeline.
     *
     * Fields sourced directly from SuperNOVAS (azimuth, altitude, RA, dec, distance)
     * are always shown when available. Derived fields (magnitude, apparent diameter,
     * rise/set times) are computed by the engine layer from SuperNOVAS output and
     * shown when computable. Nothing is hardcoded.
     */
    private String formatResult(
            Location location,
            AstroTime time,
            PositionResult position,
            EphemerisResult ephemeris) {

        // --- Observation time (from device clock) ---
        String observationTime = TIME_FORMATTER.format(
                Instant.ofEpochMilli(System.currentTimeMillis()));

        // --- Equatorial coordinates (RA / dec) from SuperNOVAS sky_pos ---
        String raDeg   = UNSUPPORTED;
        String raHours = UNSUPPORTED;
        String decDeg  = UNSUPPORTED;
        if (position.hasEquatorial()) {
            EquatorialCoordinates eq = position.getEquatorial();
            raDeg   = String.format("%.4f", eq.getRightAscensionDegrees()) + "\u00b0";
            raHours = String.format("%.4f", eq.getRightAscensionHours()) + " h";
            decDeg  = String.format("%.4f", eq.getDeclinationDegrees()) + "\u00b0";
        }

        // --- Distance (AU) from SuperNOVAS sky_pos.dis ---
        String distAu = UNSUPPORTED;
        if (position.hasDistance()) {
            distAu = String.format("%.6f", position.getDistanceAu()) + " AU";
        }

        // --- Rise / set times (engine bisection over SuperNOVAS altitude samples) ---
        String riseTime = UNSUPPORTED;
        String setTime  = UNSUPPORTED;
        if (ephemeris.hasRiseTime()) {
            riseTime = formatJdAsLocalTime(ephemeris.getRiseTimeJd());
        }
        if (ephemeris.hasSetTime()) {
            setTime = formatJdAsLocalTime(ephemeris.getSetTimeJd());
        }

        // --- Visual magnitude (engine: distance modulus from SuperNOVAS distance) ---
        String visualMag = UNSUPPORTED;
        if (ephemeris.hasVisualMagnitude()) {
            visualMag = String.format("%.2f", ephemeris.getVisualMagnitude());
        }

        // --- Apparent diameter (engine: geometry from SuperNOVAS distance + IAU radius) ---
        String apparentDiam = UNSUPPORTED;
        if (ephemeris.hasApparentDiameter()) {
            apparentDiam = String.format("%.2f", ephemeris.getApparentDiameterArcmin()) + "'";
        }

        return getString(R.string.label_observation_time) + ": " + observationTime + "\n"
                + getString(R.string.label_julian_date) + ": "
                + String.format("%.6f", time.getJulianDateTT()) + "\n"
                + "\n"
                + getString(R.string.label_latitude) + ": "
                + String.format("%.5f", location.getLatitude()) + "\u00b0\n"
                + getString(R.string.label_longitude) + ": "
                + String.format("%.5f", location.getLongitude()) + "\u00b0\n"
                + "\n"
                + getString(R.string.label_azimuth) + ": "
                + String.format("%.2f", position.getAzimuthDegrees()) + "\u00b0\n"
                + getString(R.string.label_altitude) + ": "
                + String.format("%.2f", position.getAltitudeDegrees()) + "\u00b0\n"
                + (position.isAboveHorizon()
                        ? getString(R.string.label_above_horizon)
                        : getString(R.string.label_below_horizon)) + "\n"
                + "\n"
                + getString(R.string.label_right_ascension) + ": " + raDeg
                + " (" + raHours + ")\n"
                + getString(R.string.label_declination) + ": " + decDeg + "\n"
                + "\n"
                + getString(R.string.label_distance) + ": " + distAu + "\n"
                + getString(R.string.label_rise_time) + ": " + riseTime + "\n"
                + getString(R.string.label_set_time) + ": " + setTime + "\n"
                + "\n"
                + getString(R.string.label_visual_magnitude) + ": " + visualMag + "\n"
                + getString(R.string.label_apparent_diameter) + ": " + apparentDiam;
    }

    /**
     * Converts a Julian Date (TT) to a local time string (HH:mm:ss z).
     *
     * Treats TT as UTC for display purposes; the TT-UTC offset (~69 s) is
     * below the precision shown to the user.
     *
     * @param jd Julian Date in TT
     * @return formatted local time string
     */
    private static String formatJdAsLocalTime(double jd) {
        // JD 2440587.5 = 1970-01-01 00:00:00 UTC (Unix epoch).
        double jdUnixEpoch = 2440587.5;
        double secondsFromEpoch = (jd - jdUnixEpoch) * 86400.0;
        long epochSecond = (long) secondsFromEpoch;
        Instant instant = Instant.ofEpochSecond(epochSecond);
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return RISE_SET_FORMATTER.format(zdt);
    }

    private void showResult(String text) {
        resultView.setText(text);
    }
}
