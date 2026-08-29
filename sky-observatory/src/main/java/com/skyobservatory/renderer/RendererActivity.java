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
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.VisibilityState;
import com.skyobservatory.engine.EngineInitializer;
import com.skyobservatory.engine.LocationRepository;
import com.skyobservatory.camera.SensorController;
import com.skyobservatory.util.CrashHandler;

import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Host activity for the sky renderer.
 *
 * The list of tracked bodies comes from {@link CelestialObject#defaultTargets()}.
 * To track a new object, add it to {@link CelestialObject#CATALOG} with
 * {@code enabledByDefault = true}. No changes are needed here.
 */
public class RendererActivity extends AppCompatActivity {

    private static final String TAG = "RendererActivity";

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

    private static final double FALLBACK_LATITUDE = 21.0285;
    private static final double FALLBACK_LONGITUDE = 105.8542;
    private static final double FALLBACK_ALTITUDE = 0.0;

    private void useFallbackLocation() {
        currentObserver = new Observer(FALLBACK_LATITUDE, FALLBACK_LONGITUDE, FALLBACK_ALTITUDE);
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
                    List<ObservableObject> observed = buildObservableList(obs, time);

                    if (!observed.isEmpty()) {
                        SkySnapshot snapshot = new SkySnapshot.Builder(time, obs, observed).build();
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

    private List<ObservableObject> buildObservableList(Observer obs, AstroTime time) {
        try {
            SkySnapshot snapshot = engine.createSnapshot(
                    CelestialObject.defaultTargets(), obs, time);
            return snapshot.getObjects();
        } catch (AstroException e) {
            Log.e(TAG, "Snapshot creation failed", e);
            return new ArrayList<>();
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
