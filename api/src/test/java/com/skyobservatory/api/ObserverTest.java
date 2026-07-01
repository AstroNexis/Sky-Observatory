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

public class ObserverTest {

    @Test
    public void constructorPreservesAllFields() {
        Observer observer = new Observer(48.8566, 2.3522, 35.0);

        assertEquals(48.8566, observer.getLatitudeDegrees(), 1e-9);
        assertEquals(2.3522, observer.getLongitudeDegrees(), 1e-9);
        assertEquals(35.0, observer.getAltitudeMeters(), 1e-9);
    }

    @Test
    public void equalityByValue() {
        Observer a = new Observer(51.5074, -0.1278, 10.0);
        Observer b = new Observer(51.5074, -0.1278, 10.0);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void inequalityWhenFieldsDiffer() {
        Observer base = new Observer(0.0, 0.0, 0.0);
        Observer differentLat = new Observer(1.0, 0.0, 0.0);
        Observer differentLon = new Observer(0.0, 1.0, 0.0);
        Observer differentAlt = new Observer(0.0, 0.0, 100.0);

        assertNotEquals(base, differentLat);
        assertNotEquals(base, differentLon);
        assertNotEquals(base, differentAlt);
    }

    @Test
    public void toStringContainsLatAndLon() {
        Observer observer = new Observer(40.7128, -74.0060, 0.0);
        String text = observer.toString();

        assertTrue(text.contains("40.7128"));
        assertTrue(text.contains("-74.006"));
    }
}
