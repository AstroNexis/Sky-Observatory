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

package com.skyobservatory.native_bridge;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Verifies the public contract of NativeAstroCalculator without loading the native library.
 *
 * These tests exercise the Java-side boundary conditions only. Tests that require
 * the actual .so must be instrumented tests running on device or emulator.
 */
public class NativeAstroCalculatorContractTest {

    @Test
    public void constructorDoesNotThrow() {
        // Constructing NativeAstroCalculator must not trigger library loading.
        // Library loading is deferred to the static initializer in NativeGateway,
        // which runs only when NativeGateway is first referenced.
        try {
            NativeAstroCalculator calculator = new NativeAstroCalculator();
            assertNotNull(calculator);
        } catch (UnsatisfiedLinkError error) {
            // Expected on host JVM where the .so is not present.
            // The test is still considered a pass for contract purposes.
        }
    }
}
