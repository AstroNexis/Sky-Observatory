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

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.skyobservatory.api.AstroEngine;
import com.skyobservatory.api.AstroSdk;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.SkyCoordinate;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.VisibilityState;
import com.skyobservatory.engine.EngineInitializer;
import com.skyobservatory.camera.SensorController;
import com.skyobservatory.util.CrashHandler;

import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Host activity for the sky renderer.
 *
 * Refactored to build a full {@link SkySnapshot} on each tick and hand it
 * to {@link SkyRenderer#updateSnapshot}.  The renderer never receives
 * Sun- or Moon-specific arguments; it consumes only the generic snapshot.
 *
 * Adding a new celestial body (e.g. Mars, Venus, ISS) requires only:
 *   1. Adding it to the {@link #TRACKED_OBJECTS} list below.
 *   2. Optionally adding its texture asset and a case in
 *      {@link CelestialObjectFactory#textureFor}.
 * No renderer changes are needed.
 */
public class RendererActivity extends AppCompatActivity {

    private static final String TAG = "RendererActivity";

    /**
     * Celestial objects tracked in Phase 1.
     * Extend this list to track any additional solar system body; the
     * rendering pipeline scales automatically.
     */
    private static final List<CelestialObject> TRACKED_OBJECTS = new ArrayList<>();
    static {
        TRACKED_OBJECTS.add(CelestialObject.sun());
        TRACKED_OBJECTS.add(CelestialObject.moon());
        // Future: TRACKED_OBJECTS.add(CelestialObject.mars());
        // Future: TRACKED_OBJECTS.add(new CelestialObject(CelestialObject.NAIF_VENUS, "Venus"));
    }

    private GLSurfaceView glSurfaceView;
    private SkyRenderer skyRenderer;
    private SensorController sensorController;
    private LocationRepository locationRepository;

    private AstroEngine engine;
    private volatile Observer currentObserver;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean granted =
                                Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (granted) {
                            fetchLocationAndStart();
                        } else {
                            useFallbackLocation();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.init(getApplicationContext());

        EngineInitializer.register();
        AstroSdk.initialize();
        engine = AstroSdk.getEngine();
        Log.i(TAG, "SDK initialized");

        locationRepository = new LocationRepository(this);
        sensorController   = new SensorController((SensorManager) getSystemService(SENSOR_SERVICE));

        skyRenderer = new SkyRenderer(this);
        skyRenderer.setSensorController(sensorController);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setRenderer(skyRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener((v, event) -> {
            skyRenderer.getTouchController().onTouchEvent(event);
            return true;
        });

        setContentView(glSurfaceView);

        if (hasLocationPermission()) {
            fetchLocationAndStart();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchLocationAndStart() {
        locationRepository.getCurrentLocation(new LocationRepository.LocationCallback() {
            @Override
            public void onLocation(Location location) {
                currentObserver = new Observer(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.hasAltitude() ? location.getAltitude() : 0.0);
                Log.i(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
                startSdkUpdates();
            }

            @Override
            public void onError(String reason) {
                Log.e(TAG, "Location error: " + reason);
                useFallbackLocation();
            }
        });
    }

    private void useFallbackLocation() {
        currentObserver = new Observer(21.0285, 105.8542, 0);
        Log.w(TAG, "Using fallback location (Hanoi)");
        startSdkUpdates();
    }

    private volatile boolean running;
    private Thread sdkThread;

    private void startSdkUpdates() {
        running   = true;
        sdkThread = new Thread(() -> {
            while (running) {
                try {
                    Observer obs = currentObserver;
                    if (obs == null) { Thread.sleep(100); continue; }

                    AstroTime time = AstroTime.now();
                    List<ObservableObject> observedObjects = buildObservableList(obs, time);

                    if (!observedObjects.isEmpty()) {
                        SkySnapshot snapshot = new SkySnapshot.Builder(time, obs, observedObjects).build();
                        glSurfaceView.queueEvent(() -> skyRenderer.updateSnapshot(snapshot));
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        sdkThread.setDaemon(true);
        sdkThread.start();
    }

    /**
     * Computes positions for all tracked objects and assembles them into an
     * {@link ObservableObject} list.  Failures per-object are logged and
     * skipped; the remaining objects are still rendered.
     */
    private List<ObservableObject> buildObservableList(Observer obs, AstroTime time) {
        List<ObservableObject> result = new ArrayList<>();
        for (CelestialObject target : TRACKED_OBJECTS) {
            try {
                PositionResult pos = engine.calculatePosition(target, obs, time);
                SkyCoordinate coord = new SkyCoordinate(
                        new HorizontalCoordinate(pos.getAzimuthDegrees(), pos.getAltitudeDegrees()),
                        new CartesianCoordinate(0.0, 0.0, 0.0));
                VisibilityState vis = pos.getAltitudeDegrees() >= 0
                        ? VisibilityState.VISIBLE
                        : VisibilityState.BELOW_HORIZON;
                ObservableObject.ObjectCategory cat = categoryFor(target);
                result.add(new ObservableObject(target, coord, vis, cat));
            } catch (Exception e) {
                Log.e(TAG, "Position calc failed for " + target.getName(), e);
            }
        }
        return result;
    }

    private static ObservableObject.ObjectCategory categoryFor(CelestialObject obj) {
        switch (obj.getNaifId()) {
            case CelestialObject.NAIF_SUN:  return ObservableObject.ObjectCategory.SOLAR_SYSTEM_BODY;
            case CelestialObject.NAIF_MOON: return ObservableObject.ObjectCategory.MOON;
            default:                        return ObservableObject.ObjectCategory.PLANET;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        sensorController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        sensorController.stop();
        running = false;
        if (sdkThread != null) {
            sdkThread.interrupt();
            sdkThread = null;
        }
    }
}
