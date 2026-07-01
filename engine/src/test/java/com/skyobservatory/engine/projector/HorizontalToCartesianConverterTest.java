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

package com.skyobservatory.engine.projector;

import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.HorizontalCoordinate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HorizontalToCartesianConverterTest {

    private static final double DELTA = 1e-9;
    private final HorizontalToCartesianConverter converter = new HorizontalToCartesianConverter();

    @Test
    public void zenith_producesUnitVectorUp() {
        HorizontalCoordinate zenith = new HorizontalCoordinate(0.0, 90.0);
        CartesianCoordinate result = converter.toCartesian(zenith);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(1.0, result.getY(), DELTA);
        assertEquals(0.0, result.getZ(), DELTA);
    }

    @Test
    public void nadir_producesUnitVectorDown() {
        HorizontalCoordinate nadir = new HorizontalCoordinate(0.0, -90.0);
        CartesianCoordinate result = converter.toCartesian(nadir);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(-1.0, result.getY(), DELTA);
        assertEquals(0.0, result.getZ(), DELTA);
    }

    @Test
    public void northHorizon_producesNegativeZ() {
        HorizontalCoordinate north = new HorizontalCoordinate(0.0, 0.0);
        CartesianCoordinate result = converter.toCartesian(north);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(0.0, result.getY(), DELTA);
        assertEquals(-1.0, result.getZ(), DELTA);
    }

    @Test
    public void eastHorizon_producesPositiveX() {
        HorizontalCoordinate east = new HorizontalCoordinate(90.0, 0.0);
        CartesianCoordinate result = converter.toCartesian(east);
        assertEquals(1.0, result.getX(), DELTA);
        assertEquals(0.0, result.getY(), DELTA);
        assertEquals(0.0, result.getZ(), DELTA);
    }

    @Test
    public void southHorizon_producesPositiveZ() {
        HorizontalCoordinate south = new HorizontalCoordinate(180.0, 0.0);
        CartesianCoordinate result = converter.toCartesian(south);
        assertEquals(0.0, result.getX(), DELTA);
        assertEquals(0.0, result.getY(), DELTA);
        assertEquals(1.0, result.getZ(), DELTA);
    }

    @Test
    public void westHorizon_producesNegativeX() {
        HorizontalCoordinate west = new HorizontalCoordinate(270.0, 0.0);
        CartesianCoordinate result = converter.toCartesian(west);
        assertEquals(-1.0, result.getX(), DELTA);
        assertEquals(0.0, result.getY(), DELTA);
        assertEquals(0.0, result.getZ(), DELTA);
    }

    @Test
    public void roundTrip_zenith() {
        HorizontalCoordinate original = new HorizontalCoordinate(0.0, 90.0);
        CartesianCoordinate cart = converter.toCartesian(original);
        HorizontalCoordinate result = converter.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void roundTrip_north() {
        HorizontalCoordinate original = new HorizontalCoordinate(0.0, 0.0);
        CartesianCoordinate cart = converter.toCartesian(original);
        HorizontalCoordinate result = converter.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void roundTrip_east() {
        HorizontalCoordinate original = new HorizontalCoordinate(90.0, 0.0);
        CartesianCoordinate cart = converter.toCartesian(original);
        HorizontalCoordinate result = converter.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void roundTrip_south() {
        HorizontalCoordinate original = new HorizontalCoordinate(180.0, 0.0);
        CartesianCoordinate cart = converter.toCartesian(original);
        HorizontalCoordinate result = converter.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void roundTrip_west() {
        HorizontalCoordinate original = new HorizontalCoordinate(270.0, 0.0);
        CartesianCoordinate cart = converter.toCartesian(original);
        HorizontalCoordinate result = converter.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void roundTrip_arbitrary() {
        HorizontalCoordinate original = new HorizontalCoordinate(123.4, 45.6);
        CartesianCoordinate cart = converter.toCartesian(original);
        HorizontalCoordinate result = converter.toHorizontal(cart);
        assertEquals(original.getAzimuthDegrees(), result.getAzimuthDegrees(), DELTA);
        assertEquals(original.getAltitudeDegrees(), result.getAltitudeDegrees(), DELTA);
    }

    @Test
    public void resultMagnitude_isApproximatelyOne() {
        HorizontalCoordinate coord = new HorizontalCoordinate(123.4, 45.6);
        CartesianCoordinate result = converter.toCartesian(coord);
        double magnitude = result.magnitude();
        assertEquals(1.0, magnitude, 1e-12);
    }

    @Test
    public void toCartesian_returnsNonNull() {
        assertNotNull(converter.toCartesian(new HorizontalCoordinate(0.0, 0.0)));
    }

    @Test
    public void toHorizontal_returnsNonNull() {
        assertNotNull(converter.toHorizontal(new CartesianCoordinate(0.0, 0.0, -1.0)));
    }
}
