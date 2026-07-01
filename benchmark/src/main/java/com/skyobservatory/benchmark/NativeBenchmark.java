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

import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.native_bridge.NativeAstroCalculator;

final class NativeBenchmark {

    private static final String TAG = "NativeBenchmark";

    private static final int WARMUP_ITERATIONS = 10;
    private static final int MEASURE_ITERATIONS = 100;

    private final NativeAstroCalculator calculator = new NativeAstroCalculator();

    void run() {
        Log.i(TAG, "--- NativeBenchmark start ---");

        Observer observer = new Observer(21.0285, 105.8542, 12.0);
        AstroTime time = AstroTime.now();
        CelestialObject[] targets = {
            CelestialObject.sun(),
            CelestialObject.moon(),
            CelestialObject.mars(),
        };

        benchmarkVersion();
        benchmarkCalculatePosition(targets, observer, time);

        Log.i(TAG, "--- NativeBenchmark end ---");
    }

    private void benchmarkVersion() {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            calculator.getNativeLibraryVersion();
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            calculator.getNativeLibraryVersion();
        }
        long elapsed = System.nanoTime() - start;

        logResult("nativeGetVersion", MEASURE_ITERATIONS, elapsed);
    }

    private void benchmarkCalculatePosition(
            CelestialObject[] targets, Observer observer, AstroTime time) {
        for (CelestialObject target : targets) {
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                try {
                    calculator.calculatePosition(target, observer, time);
                } catch (Exception ignored) {}
            }

            long start = System.nanoTime();
            int success = 0;
            for (int i = 0; i < MEASURE_ITERATIONS; i++) {
                try {
                    PositionResult r = calculator.calculatePosition(target, observer, time);
                    if (r != null) success++;
                } catch (Exception ignored) {}
            }
            long elapsed = System.nanoTime() - start;

            Log.i(TAG, "calculatePosition(" + target.getName() + ")"
                    + " success=" + success + "/" + MEASURE_ITERATIONS);
            logResult("calculatePosition(" + target.getName() + ")", MEASURE_ITERATIONS, elapsed);
        }
    }

    private static void logResult(String label, int iterations, long totalNs) {
        long avgNs = totalNs / iterations;
        Log.i(TAG, label
                + " iterations=" + iterations
                + " total=" + (totalNs / 1_000_000) + "ms"
                + " avg=" + (avgNs / 1_000) + "us");
    }
}
