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

package com.skyobservatory.api;

/**
 * Factory and configuration entry point for the astronomy SDK.
 *
 * <h3>Typical usage</h3>
 * <pre>{@code
 * // At application startup (before any calculation):
 * AstroSdk.registerProvider(myProvider);
 * AstroSdk.initialize();
 *
 * // Anywhere after initialization:
 * AstroEngine engine = AstroSdk.getEngine();
 * PositionResult result = engine.calculatePosition(target, observer, time);
 * }</pre>
 *
 * The SDK must be initialized before {@link #getEngine()} is called. Calling
 * {@link #initialize()} more than once is a no-op unless the SDK has been reset.
 *
 * <h3>Provider registration</h3>
 * The concrete engine implementation is supplied by an {@link AstronomyProvider}.
 * Applications should obtain a provider from the engine module rather than
 * constructing one directly. The engine module registers its built-in provider
 * automatically when included on the classpath; applications only need to call
 * {@link #initialize()}.
 */
public final class AstroSdk {

    private static volatile AstroEngine engine;
    private static volatile AstronomyProvider provider;

    private AstroSdk() {}

    /**
     * Registers the astronomy provider that will be used to create the engine.
     *
     * Must be called before {@link #initialize()}. Calling this after initialization
     * replaces the registered provider but does not rebuild the engine; call
     * {@link #resetForTesting()} followed by {@link #initialize()} to force a rebuild.
     *
     * @param astronomyProvider the provider; must not be null
     * @throws IllegalArgumentException if {@code astronomyProvider} is null
     */
    public static synchronized void registerProvider(AstronomyProvider astronomyProvider) {
        if (astronomyProvider == null) {
            throw new IllegalArgumentException("AstronomyProvider must not be null");
        }
        provider = astronomyProvider;
    }

    /**
     * Registers an engine provider using the legacy {@link EngineProvider} contract.
     *
     * @param engineProvider the provider; must not be null
     * @throws IllegalArgumentException if {@code engineProvider} is null
     * @deprecated Use {@link #registerProvider(AstronomyProvider)} instead.
     *             {@link EngineProvider} is retained for source compatibility
     *             but {@link AstronomyProvider} is the preferred interface.
     */
    @Deprecated
    public static synchronized void registerEngineProvider(EngineProvider engineProvider) {
        if (engineProvider == null) {
            throw new IllegalArgumentException("EngineProvider must not be null");
        }
        provider = engineProvider::create;
    }

    /**
     * Initializes the SDK using the registered provider.
     *
     * Safe to call more than once; subsequent calls are no-ops if the engine
     * has already been built.
     *
     * @throws IllegalStateException if no provider has been registered
     */
    public static synchronized void initialize() {
        if (engine != null) {
            return;
        }
        if (provider == null) {
            throw new IllegalStateException(
                    "No provider registered. Ensure the engine module is on the classpath "
                            + "and EngineInitializer.register() has been called, or register a "
                            + "custom AstronomyProvider via AstroSdk.registerProvider().");
        }
        engine = provider.create();
    }

    /**
     * Returns the initialized {@link AstroEngine}.
     *
     * @return the active engine
     * @throws IllegalStateException if {@link #initialize()} has not been called
     */
    public static AstroEngine getEngine() {
        AstroEngine local = engine;
        if (local == null) {
            throw new IllegalStateException(
                    "AstroSdk has not been initialized. Call AstroSdk.initialize() first.");
        }
        return local;
    }

    /**
     * Resets the SDK to its uninitialized state.
     *
     * Intended for use in unit tests only. Must not be called in production code.
     */
    static synchronized void resetForTesting() {
        engine = null;
        provider = null;
    }

    // Legacy inner interface -- retained for source compatibility.
    // New code should implement AstronomyProvider directly.

    /**
     * @deprecated Implement {@link AstronomyProvider} instead.
     *             This interface is retained only for source compatibility with
     *             existing {@code EngineInitializer} registrations.
     */
    @Deprecated
    public interface EngineProvider {
        AstroEngine create();
    }
}
