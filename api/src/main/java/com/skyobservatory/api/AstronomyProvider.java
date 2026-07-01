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
 * Factory contract for astronomy engine implementations.
 *
 * An {@code AstronomyProvider} is responsible for constructing and returning a
 * fully initialized {@link AstroEngine}. The provider is registered with
 * {@link AstroSdk#registerProvider(AstronomyProvider)} before the SDK is
 * initialized.
 *
 * <h3>Planned implementations</h3>
 * <ul>
 *   <li>{@code SuperNovasProvider} -- backed by the SuperNOVAS C library via JNI (Phase 2)</li>
 *   <li>{@code SpiceProvider} -- backed by the NAIF SPICE toolkit (future)</li>
 *   <li>{@code MockProvider} -- deterministic stub for testing</li>
 * </ul>
 *
 * Only one provider may be active at a time. Registering a new provider after
 * {@link AstroSdk#initialize()} has been called replaces the current engine on
 * the next call to {@link AstroSdk#initialize()}.
 */
public interface AstronomyProvider {

    /**
     * Creates and returns a fully initialized {@link AstroEngine}.
     *
     * This method is called once by {@link AstroSdk#initialize()}. The returned
     * engine is cached and returned by subsequent calls to {@link AstroSdk#getEngine()}.
     *
     * @return a ready-to-use astronomy engine; must not be null
     */
    AstroEngine create();
}
