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

package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;

public class CelestialObjectTest {

    @Test
    public void defaultTargetsReturnsEnabledObjects() {
        java.util.List<CelestialObject> targets = CelestialObject.defaultTargets();
        assertFalse(targets.isEmpty());
        for (CelestialObject obj : targets) {
            assertTrue(obj.isEnabledByDefault());
        }
    }

    @Test
    public void defaultTargetsIncludesSun() {
        java.util.List<CelestialObject> targets = CelestialObject.defaultTargets();
        assertTrue(targets.contains(CelestialObject.sun()));
    }

    @Test
    public void sunFactoryHasCorrectNaifId() {
        CelestialObject sun = CelestialObject.sun();
        assertEquals(CelestialObject.NAIF_SUN, sun.getNaifId());
        assertEquals("Sun", sun.getName());
    }

    @Test
    public void physicalPropertiesAreExposed() {
        CelestialObject saturn = CelestialObject.fromNaifId(CelestialObject.NAIF_SATURN);

        assertEquals(60268.0, saturn.getEquatorialRadiusKm(), 0.0);
        assertEquals(58232.0, saturn.getMeanRadiusKm(), 0.0);
        assertEquals(120536.0, saturn.getDiameterKm(), 0.0);
        assertTrue(saturn.hasRings());
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

    @Test
    public void moonFactoryHasCorrectNaifId() {
        CelestialObject moon = CelestialObject.moon();
        assertEquals(CelestialObject.NAIF_MOON, moon.getNaifId());
    }

    @Test
    public void marsFactoryHasCorrectNaifId() {
        CelestialObject mars = CelestialObject.mars();
        assertEquals(CelestialObject.NAIF_MARS, mars.getNaifId());
    }

    @Test
    public void fromNaifIdReturnsKnownObject() {
        CelestialObject jupiter = CelestialObject.fromNaifId(CelestialObject.NAIF_JUPITER);
        assertNotNull(jupiter);
        assertEquals("Jupiter", jupiter.getName());
    }

    @Test
    public void fromNaifIdReturnsNullForUnknown() {
        assertNull(CelestialObject.fromNaifId(999999));
    }

    @Test
    public void toStringContainsName() {
        String s = CelestialObject.sun().toString();
        assertTrue(s.contains("Sun"));
    }

    @Test
    public void notEqualsDifferentNaifId() {
        CelestialObject a = CelestialObject.sun();
        CelestialObject b = CelestialObject.moon();
        assertNotEquals(a, b);
    }

    @Test
    public void notEqualsNull() {
        assertNotEquals(null, CelestialObject.sun());
    }

    @Test
    public void notEqualsDifferentType() {
        assertNotEquals("string", CelestialObject.sun());
    }
}
