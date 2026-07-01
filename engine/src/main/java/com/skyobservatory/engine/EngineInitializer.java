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

package com.skyobservatory.engine;

import com.skyobservatory.api.AstroEngine;
import com.skyobservatory.api.AstronomyProvider;
import com.skyobservatory.api.AstroSdk;
import com.skyobservatory.engine.coordinates.CoordinateConverter;
import com.skyobservatory.engine.validation.InputValidator;
import com.skyobservatory.native_bridge.NativeAstroCalculator;

/**
 * Bootstraps the engine module and registers it with {@link AstroSdk}.
 *
 * Call {@link #register()} once at application startup, before calling
 * {@link AstroSdk#initialize()}. After registration, the sample or application
 * code only needs to call {@link AstroSdk#initialize()} and then use
 * {@link AstroSdk#getEngine()} -- no further references to engine internals
 * are required.
 *
 * <pre>{@code
 * // Application startup:
 * EngineInitializer.register();
 * AstroSdk.initialize();
 *
 * // Usage:
 * AstroEngine engine = AstroSdk.getEngine();
 * }</pre>
 *
 * This class is the only public entry point into the engine module's internals.
 * All other engine types are package-private.
 */
public final class EngineInitializer {

    private EngineInitializer() {}

    /**
     * Builds the engine object graph and registers it as the active
     * {@link AstronomyProvider} with {@link AstroSdk}.
     *
     * Safe to call multiple times; subsequent calls replace the registered
     * provider with an equivalent one.
     */
    public static void register() {
        AstroSdk.registerProvider(EngineInitializer::buildEngine);
    }

    /**
     * Returns an {@link AstronomyProvider} backed by this engine.
     *
     * Provides an alternative registration path for applications that prefer
     * to manage providers explicitly:
     * <pre>{@code
     * AstroSdk.registerProvider(EngineInitializer.provider());
     * AstroSdk.initialize();
     * }</pre>
     *
     * @return a provider that creates and returns a {@link DefaultAstroEngine}
     */
    public static AstronomyProvider provider() {
        return EngineInitializer::buildEngine;
    }

    private static AstroEngine buildEngine() {
        InputValidator validator = new InputValidator();
        CoordinateConverter converter = new CoordinateConverter();
        NativeAstroCalculator nativeCalculator = new NativeAstroCalculator();
        PositionProvider positionProvider = nativeCalculator::calculatePosition;
        PositionCalculator positionCalculator =
                new PositionCalculator(validator, converter, positionProvider);
        EphemerisCalculator ephemerisCalculator = new EphemerisCalculator(positionProvider);
        return new DefaultAstroEngine(positionCalculator, ephemerisCalculator);
    }
}
