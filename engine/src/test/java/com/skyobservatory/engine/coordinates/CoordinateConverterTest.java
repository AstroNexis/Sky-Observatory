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

package com.skyobservatory.engine.coordinates;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoordinateConverterTest {

    private static final double DELTA = 1e-10;

    private CoordinateConverter converter;

    @Before
    public void setUp() {
        converter = new CoordinateConverter();
    }

    @Test
    public void degreesToRadiansConvertsCorrectly() {
        assertEquals(Math.PI, converter.degreesToRadians(180.0), DELTA);
        assertEquals(Math.PI / 2.0, converter.degreesToRadians(90.0), DELTA);
        assertEquals(0.0, converter.degreesToRadians(0.0), DELTA);
    }

    @Test
    public void radiansToDegreesConvertsCorrectly() {
        assertEquals(180.0, converter.radiansToDegrees(Math.PI), DELTA);
        assertEquals(90.0, converter.radiansToDegrees(Math.PI / 2.0), DELTA);
    }

    @Test
    public void normalizeAzimuthKeepsAngleInRange() {
        assertEquals(0.0, converter.normalizeAzimuth(0.0), DELTA);
        assertEquals(90.0, converter.normalizeAzimuth(90.0), DELTA);
        assertEquals(359.0, converter.normalizeAzimuth(359.0), DELTA);
    }

    @Test
    public void normalizeAzimuthWrapsOver360() {
        assertEquals(10.0, converter.normalizeAzimuth(370.0), DELTA);
    }

    @Test
    public void normalizeAzimuthWrapsNegative() {
        assertEquals(350.0, converter.normalizeAzimuth(-10.0), DELTA);
    }

    @Test
    public void clampAltitudePreservesValueInRange() {
        assertEquals(45.0, converter.clampAltitude(45.0), DELTA);
        assertEquals(-45.0, converter.clampAltitude(-45.0), DELTA);
        assertEquals(90.0, converter.clampAltitude(90.0), DELTA);
        assertEquals(-90.0, converter.clampAltitude(-90.0), DELTA);
    }

    @Test
    public void clampAltitudeClampsAbove90() {
        assertEquals(90.0, converter.clampAltitude(95.0), DELTA);
    }

    @Test
    public void clampAltitudeClampsBelow90() {
        assertEquals(-90.0, converter.clampAltitude(-95.0), DELTA);
    }
}
