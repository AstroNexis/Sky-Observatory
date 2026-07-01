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

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class AstroTimeTest {

    private static final double DELTA = 1e-9;

    @Test
    public void fromInstant_unixEpoch_returnsCorrectJulianDate() {
        // Unix epoch (1970-01-01T00:00:00Z) corresponds to JD 2440587.5
        AstroTime time = AstroTime.fromInstant(Instant.EPOCH);
        assertEquals(2440587.5, time.getJulianDateTT(), DELTA);
    }

    @Test
    public void fromInstant_j2000Epoch_returnsJ2000JulianDate() {
        // J2000.0 = 2000-01-01T12:00:00Z = JD 2451545.0
        // Seconds from Unix epoch to J2000.0 = 946728000
        Instant j2000Instant = Instant.ofEpochSecond(946728000L);
        AstroTime time = AstroTime.fromInstant(j2000Instant);
        assertEquals(AstroTime.J2000_EPOCH, time.getJulianDateTT(), 1e-6);
    }

    @Test
    public void fromInstant_nullInstant_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> AstroTime.fromInstant(null));
    }

    @Test
    public void fromInstant_subSecondPrecision_preservesNanos() {
        // 0.5 seconds after Unix epoch = JD 2440587.5 + 0.5/86400
        Instant halfSecond = Instant.ofEpochSecond(0, 500_000_000L);
        AstroTime time = AstroTime.fromInstant(halfSecond);
        double expected = 2440587.5 + 0.5 / 86400.0;
        assertEquals(expected, time.getJulianDateTT(), 1e-12);
    }

    @Test
    public void now_returnsNonNull() {
        AstroTime time = AstroTime.now();
        assertNotNull(time);
    }

    @Test
    public void now_returnsReasonableJulianDate() {
        // JD for 2020-01-01 = ~2458849. JD for 2050-01-01 = ~2469807.
        // Any call to now() should land in this range for the foreseeable future.
        AstroTime time = AstroTime.now();
        assertTrue("JD should be after 2020", time.getJulianDateTT() > 2458849.0);
        assertTrue("JD should be before 2050", time.getJulianDateTT() < 2469807.0);
    }

    @Test
    public void j2000_returnsCorrectConstant() {
        assertEquals(AstroTime.J2000_EPOCH, AstroTime.j2000().getJulianDateTT(), 0.0);
    }

    @Test
    public void julianCenturiesFromJ2000_atJ2000_returnsZero() {
        assertEquals(0.0, AstroTime.j2000().julianCenturiesFromJ2000(), DELTA);
    }

    @Test
    public void julianCenturiesFromJ2000_oneJulianCenturyAfterJ2000_returnsOne() {
        AstroTime onecenturyLater = new AstroTime(AstroTime.J2000_EPOCH + 36525.0);
        assertEquals(1.0, onecenturyLater.julianCenturiesFromJ2000(), DELTA);
    }
}
