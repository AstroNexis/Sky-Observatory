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

public class CelestialObjectTest {

    @Test
    public void sunFactoryHasCorrectNaifId() {
        CelestialObject sun = CelestialObject.sun();
        assertEquals(CelestialObject.NAIF_SUN, sun.getNaifId());
        assertEquals("Sun", sun.getName());
    }

    @Test
    public void equalityBasedOnNaifId() {
        CelestialObject a = CelestialObject.sun();
        CelestialObject b = new CelestialObject(CelestialObject.NAIF_SUN, "Sol");

        // Two objects with the same NAIF id are equal regardless of display name.
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName() {
        new CelestialObject(1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyName() {
        new CelestialObject(1, "");
    }
}
