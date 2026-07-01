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

package com.skyobservatory.engine.validation;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.Observer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InputValidatorTest {

    private InputValidator validator;

    @Before
    public void setUp() {
        validator = new InputValidator();
    }

    // Observer latitude tests

    @Test
    public void validObserverPassesValidation() throws AstroException {
        validator.validateObserver(new Observer(51.5, -0.1, 10.0));
    }

    @Test(expected = AstroException.class)
    public void latitudeTooHighThrows() throws AstroException {
        validator.validateObserver(new Observer(91.0, 0.0, 0.0));
    }

    @Test(expected = AstroException.class)
    public void latitudeTooLowThrows() throws AstroException {
        validator.validateObserver(new Observer(-91.0, 0.0, 0.0));
    }

    @Test(expected = AstroException.class)
    public void longitudeTooHighThrows() throws AstroException {
        validator.validateObserver(new Observer(0.0, 181.0, 0.0));
    }

    @Test(expected = AstroException.class)
    public void longitudeTooLowThrows() throws AstroException {
        validator.validateObserver(new Observer(0.0, -181.0, 0.0));
    }

    @Test(expected = AstroException.class)
    public void nullObserverThrows() throws AstroException {
        validator.validateObserver(null);
    }

    // AstroTime tests

    @Test
    public void j2000TimePassesValidation() throws AstroException {
        validator.validateTime(AstroTime.j2000());
    }

    @Test(expected = AstroException.class)
    public void nanJulianDateThrows() throws AstroException {
        validator.validateTime(new AstroTime(Double.NaN));
    }

    @Test(expected = AstroException.class)
    public void infiniteJulianDateThrows() throws AstroException {
        validator.validateTime(new AstroTime(Double.POSITIVE_INFINITY));
    }

    @Test(expected = AstroException.class)
    public void nullTimeThrows() throws AstroException {
        validator.validateTime(null);
    }

    // CelestialObject tests

    @Test
    public void validTargetPassesValidation() throws AstroException {
        validator.validateTarget(CelestialObject.sun());
    }

    @Test(expected = AstroException.class)
    public void nullTargetThrows() throws AstroException {
        validator.validateTarget(null);
    }
}
