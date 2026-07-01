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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VisibilityStateTest {

    @Test
    public void fromAltitude_positive_returnsVisible() {
        assertEquals(VisibilityState.VISIBLE, VisibilityState.fromAltitude(45.0));
    }

    @Test
    public void fromAltitude_zero_returnsBelowHorizon() {
        assertEquals(VisibilityState.BELOW_HORIZON, VisibilityState.fromAltitude(0.0));
    }

    @Test
    public void fromAltitude_negative_returnsBelowHorizon() {
        assertEquals(VisibilityState.BELOW_HORIZON, VisibilityState.fromAltitude(-10.0));
    }

    @Test
    public void enumConstants_defined() {
        assertNotNull(VisibilityState.valueOf("VISIBLE"));
        assertNotNull(VisibilityState.valueOf("BELOW_HORIZON"));
        assertEquals(2, VisibilityState.values().length);
    }
}
