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

import com.skyobservatory.api.HorizontalCoordinate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VisibilityCalculatorTest {

    private final VisibilityCalculator calculator = new VisibilityCalculator();

    @Test
    public void positiveAltitude_isVisible() {
        assertTrue(calculator.isVisible(45.0));
    }

    @Test
    public void zeroAltitude_isNotVisible() {
        assertFalse(calculator.isVisible(0.0));
    }

    @Test
    public void negativeAltitude_isNotVisible() {
        assertFalse(calculator.isVisible(-10.0));
    }

    @Test
    public void zenith_isVisible() {
        HorizontalCoordinate zenith = new HorizontalCoordinate(0.0, 90.0);
        assertTrue(calculator.isVisible(zenith));
    }

    @Test
    public void horizon_isNotVisible() {
        HorizontalCoordinate horizon = new HorizontalCoordinate(0.0, 0.0);
        assertFalse(calculator.isVisible(horizon));
    }

    @Test
    public void belowHorizon_isNotVisible() {
        HorizontalCoordinate below = new HorizontalCoordinate(180.0, -30.0);
        assertFalse(calculator.isVisible(below));
    }

    @Test
    public void describeVisibility_aboveHorizon() {
        assertEquals("above horizon", calculator.describeVisibility(45.0));
    }

    @Test
    public void describeVisibility_atHorizon() {
        assertEquals("below horizon", calculator.describeVisibility(0.0));
    }

    @Test
    public void describeVisibility_belowHorizon() {
        assertEquals("below horizon", calculator.describeVisibility(-10.0));
    }
}
