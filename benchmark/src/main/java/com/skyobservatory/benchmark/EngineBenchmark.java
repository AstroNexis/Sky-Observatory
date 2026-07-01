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

import com.skyobservatory.api.AstroEngine;
import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.api.SkySnapshot;

import java.util.Arrays;
import java.util.List;

final class EngineBenchmark {

    private static final String TAG = "EngineBenchmark";

    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASURE_ITERATIONS = 50;

    private final AstroEngine engine;

    EngineBenchmark(AstroEngine engine) {
        this.engine = engine;
    }

    void run() {
        Log.i(TAG, "--- EngineBenchmark start ---");

        Observer observer = new Observer(21.0285, 105.8542, 12.0);
        AstroTime time = AstroTime.now();
        List<CelestialObject> targets = Arrays.asList(
            CelestialObject.sun(),
            CelestialObject.moon(),
            CelestialObject.mars()
        );

        benchmarkCalculatePosition(targets, observer, time);
        benchmarkCreateSnapshot(targets, observer, time);

        Log.i(TAG, "--- EngineBenchmark end ---");
    }

    private void benchmarkCalculatePosition(
            List<CelestialObject> targets, Observer observer, AstroTime time) {
        for (CelestialObject target : targets) {
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                try {
                    engine.calculatePosition(target, observer, time);
                } catch (AstroException ignored) {}
            }

            long start = System.nanoTime();
            int success = 0;
            for (int i = 0; i < MEASURE_ITERATIONS; i++) {
                try {
                    PositionResult r = engine.calculatePosition(target, observer, time);
                    if (r != null) success++;
                } catch (AstroException ignored) {}
            }
            long elapsed = System.nanoTime() - start;

            Log.i(TAG, "calculatePosition(" + target.getName() + ")"
                    + " success=" + success + "/" + MEASURE_ITERATIONS);
            logResult("calculatePosition(" + target.getName() + ")", MEASURE_ITERATIONS, elapsed);
        }
    }

    private void benchmarkCreateSnapshot(
            List<CelestialObject> targets, Observer observer, AstroTime time) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try {
                engine.createSnapshot(targets, observer, time);
            } catch (AstroException ignored) {}
        }

        long start = System.nanoTime();
        int success = 0;
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            try {
                SkySnapshot snap = engine.createSnapshot(targets, observer, time);
                if (snap != null) success++;
            } catch (AstroException ignored) {}
        }
        long elapsed = System.nanoTime() - start;

        Log.i(TAG, "createSnapshot(n=" + targets.size() + ")"
                + " success=" + success + "/" + MEASURE_ITERATIONS);
        logResult("createSnapshot(n=" + targets.size() + ")", MEASURE_ITERATIONS, elapsed);
    }

    private static void logResult(String label, int iterations, long totalNs) {
        long avgNs = totalNs / iterations;
        Log.i(TAG, label
                + " iterations=" + iterations
                + " total=" + (totalNs / 1_000_000) + "ms"
                + " avg=" + (avgNs / 1_000) + "us");
    }
}
