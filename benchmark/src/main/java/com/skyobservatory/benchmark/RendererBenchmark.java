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

package com.skyobservatory.benchmark;

import android.util.Log;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroEngine;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.SkyCoordinate;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.VisibilityState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class RendererBenchmark {

    private static final String TAG = "RendererBenchmark";

    private static final int WARMUP_FRAMES = 10;
    private static final int MEASURE_FRAMES = 200;

    private static final float WORLD_SCALE = 10f;

    private final AstroEngine engine;

    RendererBenchmark(AstroEngine engine) {
        this.engine = engine;
    }

    void run() {
        Log.i(TAG, "--- RendererBenchmark start ---");

        Observer observer = new Observer(21.0285, 105.8542, 12.0);
        AstroTime time = AstroTime.now();
        List<CelestialObject> targets = Arrays.asList(
            CelestialObject.sun(),
            CelestialObject.moon(),
            CelestialObject.mars()
        );

        SkySnapshot snapshot = buildSnapshot(targets, observer, time);
        if (snapshot == null) {
            Log.w(TAG, "snapshot unavailable -- skipping frame-time benchmark");
        } else {
            benchmarkSnapshotIteration(snapshot);
        }

        benchmarkPlaceEntry(targets, observer, time);
        benchmarkMatrixProjection();

        Log.i(TAG, "--- RendererBenchmark end ---");
    }

    private SkySnapshot buildSnapshot(
            List<CelestialObject> targets, Observer observer, AstroTime time) {
        try {
            return engine.createSnapshot(targets, observer, time);
        } catch (AstroException e) {
            Log.w(TAG, "createSnapshot failed: " + e.getMessage());
            return buildFallbackSnapshot(targets, observer, time);
        }
    }

    private SkySnapshot buildFallbackSnapshot(
            List<CelestialObject> targets, Observer observer, AstroTime time) {
        List<ObservableObject> objects = new ArrayList<>();
        double az = 180.0;
        for (CelestialObject target : targets) {
            SkyCoordinate coord = new SkyCoordinate(
                new HorizontalCoordinate(az, 30.0),
                new CartesianCoordinate(0.0, 0.0, 0.0));
            objects.add(new ObservableObject(
                target, coord, VisibilityState.VISIBLE,
                ObservableObject.ObjectCategory.SOLAR_SYSTEM_BODY));
            az += 45.0;
        }
        return new SkySnapshot.Builder(time, observer, objects).build();
    }

    private void benchmarkSnapshotIteration(SkySnapshot snapshot) {
        for (int i = 0; i < WARMUP_FRAMES; i++) {
            iterateSnapshot(snapshot);
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_FRAMES; i++) {
            iterateSnapshot(snapshot);
        }
        long elapsed = System.nanoTime() - start;

        logResult("snapshotIteration(n=" + snapshot.getObjects().size() + ")",
                MEASURE_FRAMES, elapsed);
    }

    private void benchmarkPlaceEntry(
            List<CelestialObject> targets, Observer observer, AstroTime time) {
        SkySnapshot snap = buildSnapshot(targets, observer, time);
        if (snap == null) return;

        for (int i = 0; i < WARMUP_FRAMES; i++) {
            placeAllEntries(snap);
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_FRAMES; i++) {
            placeAllEntries(snap);
        }
        long elapsed = System.nanoTime() - start;

        logResult("placeEntry(n=" + snap.getObjects().size() + ")", MEASURE_FRAMES, elapsed);
    }

    private void benchmarkMatrixProjection() {
        float[] vp = buildIdentityVp();
        float wx = 5f, wy = 3f, wz = -8f;

        for (int i = 0; i < WARMUP_FRAMES; i++) {
            projectToClip(vp, wx, wy, wz);
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_FRAMES; i++) {
            projectToClip(vp, wx, wy, wz);
        }
        long elapsed = System.nanoTime() - start;

        logResult("matrixProjection", MEASURE_FRAMES, elapsed);
    }

    private static void iterateSnapshot(SkySnapshot snapshot) {
        for (ObservableObject obj : snapshot.getObjects()) {
            double azRad  = Math.toRadians(obj.getAzimuthDegrees());
            double altRad = Math.toRadians(obj.getAltitudeDegrees());
            double cosAlt = Math.cos(altRad);
            float wx = (float)( cosAlt * Math.sin(azRad)) * WORLD_SCALE;
            float wy = (float)( Math.sin(altRad))         * WORLD_SCALE;
            float wz = (float)(-cosAlt * Math.cos(azRad)) * WORLD_SCALE;
            sinkFloat(wx + wy + wz);
        }
    }

    private static void placeAllEntries(SkySnapshot snapshot) {
        for (ObservableObject obj : snapshot.getObjects()) {
            double azRad  = Math.toRadians(obj.getAzimuthDegrees());
            double altRad = Math.toRadians(obj.getAltitudeDegrees());
            double cosAlt = Math.cos(altRad);
            float[] model = new float[16];
            model[0]  = 1f; model[5]  = 1f; model[10] = 1f; model[15] = 1f;
            model[12] = (float)( cosAlt * Math.sin(azRad)) * WORLD_SCALE;
            model[13] = (float)( Math.sin(altRad))         * WORLD_SCALE;
            model[14] = (float)(-cosAlt * Math.cos(azRad)) * WORLD_SCALE;
            sinkFloat(model[12] + model[13] + model[14]);
        }
    }

    private static float[] projectToClip(float[] vp, float wx, float wy, float wz) {
        float cx = vp[0]*wx + vp[4]*wy + vp[8]*wz  + vp[12];
        float cy = vp[1]*wx + vp[5]*wy + vp[9]*wz  + vp[13];
        float cw = vp[3]*wx + vp[7]*wy + vp[11]*wz + vp[15];
        return new float[]{ cx, cy, cw };
    }

    private static float[] buildIdentityVp() {
        float[] m = new float[16];
        m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f;
        return m;
    }

    @SuppressWarnings("unused")
    private static volatile float sink;

    private static void sinkFloat(float v) {
        sink = v;
    }

    private static void logResult(String label, int iterations, long totalNs) {
        long avgNs = totalNs / iterations;
        Log.i(TAG, label
                + " iterations=" + iterations
                + " total=" + (totalNs / 1_000_000) + "ms"
                + " avg=" + (avgNs / 1_000) + "us");
    }
}
