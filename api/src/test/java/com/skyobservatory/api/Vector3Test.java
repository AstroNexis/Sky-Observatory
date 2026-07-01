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

import org.junit.Test;

import static org.junit.Assert.*;

public class Vector3Test {

    private static final double DELTA = 1e-12;

    @Test
    public void constructorPreservesComponents() {
        Vector3 v = new Vector3(1.0, 2.0, 3.0);
        assertEquals(1.0, v.getX(), DELTA);
        assertEquals(2.0, v.getY(), DELTA);
        assertEquals(3.0, v.getZ(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNaN() {
        new Vector3(Double.NaN, 0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsInfinity() {
        new Vector3(0.0, Double.POSITIVE_INFINITY, 0.0);
    }

    @Test
    public void zeroConstantHasZeroComponents() {
        assertEquals(0.0, Vector3.ZERO.getX(), DELTA);
        assertEquals(0.0, Vector3.ZERO.getY(), DELTA);
        assertEquals(0.0, Vector3.ZERO.getZ(), DELTA);
    }

    @Test
    public void magnitudeOfUnitVector() {
        Vector3 v = new Vector3(1.0, 0.0, 0.0);
        assertEquals(1.0, v.magnitude(), DELTA);
    }

    @Test
    public void magnitude_pythagoras() {
        // 3-4-5 right triangle in 3D
        Vector3 v = new Vector3(3.0, 4.0, 0.0);
        assertEquals(5.0, v.magnitude(), DELTA);
    }

    @Test
    public void dotProductOfOrthogonalVectors() {
        Vector3 x = new Vector3(1.0, 0.0, 0.0);
        Vector3 y = new Vector3(0.0, 1.0, 0.0);
        assertEquals(0.0, x.dot(y), DELTA);
    }

    @Test
    public void dotProductOfParallelVectors() {
        Vector3 v = new Vector3(2.0, 0.0, 0.0);
        assertEquals(4.0, v.dot(v), DELTA);
    }

    @Test
    public void scaleMultipliesComponents() {
        Vector3 v = new Vector3(1.0, 2.0, 3.0);
        Vector3 scaled = v.scale(2.0);
        assertEquals(2.0, scaled.getX(), DELTA);
        assertEquals(4.0, scaled.getY(), DELTA);
        assertEquals(6.0, scaled.getZ(), DELTA);
    }

    @Test
    public void addProducesComponentWiseSum() {
        Vector3 a = new Vector3(1.0, 2.0, 3.0);
        Vector3 b = new Vector3(4.0, 5.0, 6.0);
        Vector3 sum = a.add(b);
        assertEquals(5.0, sum.getX(), DELTA);
        assertEquals(7.0, sum.getY(), DELTA);
        assertEquals(9.0, sum.getZ(), DELTA);
    }

    @Test
    public void equalityByValue() {
        Vector3 a = new Vector3(1.0, 2.0, 3.0);
        Vector3 b = new Vector3(1.0, 2.0, 3.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void inequalityWhenComponentsDiffer() {
        Vector3 base = new Vector3(1.0, 2.0, 3.0);
        assertNotEquals(base, new Vector3(9.0, 2.0, 3.0));
        assertNotEquals(base, new Vector3(1.0, 9.0, 3.0));
        assertNotEquals(base, new Vector3(1.0, 2.0, 9.0));
    }

    @Test
    public void toStringContainsComponents() {
        Vector3 v = new Vector3(1.5, -2.5, 3.5);
        String s = v.toString();
        assertTrue(s.contains("1.5"));
        assertTrue(s.contains("-2.5"));
        assertTrue(s.contains("3.5"));
    }
}
