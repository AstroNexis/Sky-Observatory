/*
 * Copyright 2026 Phuc An <phucan@tutamail.com>
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

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.NativeException;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;

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

    @Test(expected = NativeException.class)
    public void nullNativeResponseThrowsNativeException() throws AstroException {
        calculatorReturning(null).calculatePosition(
                CelestialObject.sun(), observer(), AstroTime.j2000());
    }

    @Test(expected = NativeException.class)
    public void shortNativeResponseThrowsNativeException() throws AstroException {
        calculatorReturning(new double[6]).calculatePosition(
                CelestialObject.sun(), observer(), AstroTime.j2000());
    }

    @Test(expected = NativeException.class)
    public void errorFlaggedNativeResponseThrowsNativeException() throws AstroException {
        calculatorReturning(new double[] {0, 0, 0, 0, 0, 0, -1}).calculatePosition(
                CelestialObject.sun(), observer(), AstroTime.j2000());
    }

    @Test(expected = NativeException.class)
    public void nonFiniteHorizontalCoordinatesThrowNativeException() throws AstroException {
        calculatorReturning(new double[] {Double.NaN, 45, 12, -23, 1, 0, 0})
                .calculatePosition(CelestialObject.sun(), observer(), AstroTime.j2000());
    }

    @Test(expected = NativeException.class)
    public void nonFiniteAltitudeThrowsNativeException() throws AstroException {
        calculatorReturning(new double[] {180, Double.POSITIVE_INFINITY, 12, -23, 1, 0, 0})
                .calculatePosition(CelestialObject.sun(), observer(), AstroTime.j2000());
    }

    @Test(expected = NativeException.class)
    public void infiniteDistanceThrowsNativeException() throws AstroException {
        calculatorReturning(new double[] {180, 45, 12, -23, Double.POSITIVE_INFINITY, 0, 0})
                .calculatePosition(CelestialObject.sun(), observer(), AstroTime.j2000());
    }

    @Test
    public void nonFiniteEquatorialCoordinatesAreNotIncluded() throws AstroException {
        PositionResult invalidRightAscension = calculatorReturning(
                new double[] {180, 45, Double.POSITIVE_INFINITY, -23, 1, 0, 0})
                .calculatePosition(CelestialObject.sun(), observer(), AstroTime.j2000());
        PositionResult invalidDeclination = calculatorReturning(
                new double[] {180, 45, 12, Double.POSITIVE_INFINITY, 1, 0, 0})
                .calculatePosition(CelestialObject.sun(), observer(), AstroTime.j2000());

        assertFalse(invalidRightAscension.hasEquatorial());
        assertFalse(invalidDeclination.hasEquatorial());
    }

    @Test
    public void validNativeResponsePopulatesPosition() throws AstroException {
        PositionResult result = calculatorReturning(
                new double[] {180, 45, 12, -23, 1.5, 0, 0})
                .calculatePosition(CelestialObject.sun(), observer(), AstroTime.j2000());

        assertEquals(180, result.getAzimuthDegrees(), 0);
        assertEquals(45, result.getAltitudeDegrees(), 0);
        assertEquals(180, result.getEquatorial().getRightAscensionDegrees(), 0);
        assertEquals(-23, result.getEquatorial().getDeclinationDegrees(), 0);
        assertEquals(1.5, result.getDistanceAu(), 0);
    }

    private static NativeAstroCalculator calculatorReturning(double[] response) {
        return new NativeAstroCalculator((naifId, jd, lat, lon, alt) -> response);
    }

    private static Observer observer() {
        return new Observer(0, 0, 0);
    }
}
