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

import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.SkyCoordinate;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class AstroSdkTest {

    @After
    public void tearDown() {
        AstroSdk.resetForTesting();
    }

    @Test(expected = IllegalStateException.class)
    public void getEngine_beforeInitialize_throwsIllegalState() {
        AstroSdk.getEngine();
    }

    @Test(expected = IllegalStateException.class)
    public void initialize_withNoProvider_throwsIllegalState() {
        AstroSdk.initialize();
    }

    @Test
    public void initialize_withProvider_returnsEngine() {
        AstroSdk.registerProvider(stubProvider());
        AstroSdk.initialize();

        AstroEngine engine = AstroSdk.getEngine();
        assertNotNull(engine);
    }

    @Test
    public void initialize_calledTwice_isNoOp() {
        AstroSdk.registerProvider(stubProvider());
        AstroSdk.initialize();
        AstroEngine first = AstroSdk.getEngine();
        AstroSdk.initialize();
        AstroEngine second = AstroSdk.getEngine();

        assertSame("initialize() must be idempotent", first, second);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerProvider_withNull_throwsIllegalArgument() {
        AstroSdk.registerProvider(null);
    }

    @Test
    public void resetForTesting_allowsReinitialization() {
        AstroSdk.registerProvider(stubProvider());
        AstroSdk.initialize();
        AstroSdk.resetForTesting();

        // After reset, getEngine must throw again.
        try {
            AstroSdk.getEngine();
            fail("Expected IllegalStateException after reset");
        } catch (IllegalStateException expected) {
            // correct
        }
    }

    private static AstronomyProvider stubProvider() {
        return () -> new AstroEngine() {
            @Override
            public PositionResult calculatePosition(
                    CelestialObject target, Observer observer, AstroTime time) {
                return new PositionResult(0.0, 0.0, target, observer, time);
            }

            @Override
            public EphemerisResult calculateEphemeris(
                    CelestialObject target, Observer observer, AstroTime time) {
                return new EphemerisResult.Builder().build();
            }

            @Override
            public SkyCoordinate project(HorizontalCoordinate coordinate) {
                return new SkyCoordinate(coordinate,
                        new CartesianCoordinate(0.0, 0.0, -1.0));
            }

            @Override
            public SkySnapshot createSnapshot(
                    java.util.List<CelestialObject> targets,
                    Observer observer,
                    AstroTime time) {
                java.util.List<ObservableObject> objects = new java.util.ArrayList<>();
                for (CelestialObject t : targets) {
                    objects.add(new ObservableObject(
                            t,
                            new SkyCoordinate(
                                    new HorizontalCoordinate(0.0, 0.0),
                                    new CartesianCoordinate(0.0, 0.0, -1.0)),
                            VisibilityState.BELOW_HORIZON,
                            ObservableObject.ObjectCategory.UNKNOWN));
                }
                return new SkySnapshot.Builder(time, observer, objects).build();
            }

            @Override
            public VisibleSkyRegion calculateVisibleRegion(
                    SkySnapshot snapshot, Viewport viewport) {
                return new VisibleSkyRegion(
                        snapshot, viewport,
                        java.util.Collections.emptyList(),
                        java.util.Collections.emptyList());
            }
        };
    }
}
