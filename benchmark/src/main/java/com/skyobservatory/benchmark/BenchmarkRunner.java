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
import com.skyobservatory.api.AstroSdk;
import com.skyobservatory.engine.EngineInitializer;

/**
 * Runs all benchmark suites in sequence.
 *
 * Call {@link #runAll()} from any non-UI context (background thread, test).
 * The SDK is bootstrapped internally; callers must not call
 * {@link AstroSdk#initialize()} beforehand.
 */
public final class BenchmarkRunner {

    private static final String TAG = "BenchmarkRunner";

    private BenchmarkRunner() {}

    public static void runAll() {
        Log.i(TAG, "=== BenchmarkRunner start ===");

        EngineInitializer.register();
        AstroSdk.initialize();
        AstroEngine engine = AstroSdk.getEngine();

        new NativeBenchmark().run();
        new EngineBenchmark(engine).run();
        new RendererBenchmark(engine).run();

        Log.i(TAG, "=== BenchmarkRunner end ===");
    }
}
