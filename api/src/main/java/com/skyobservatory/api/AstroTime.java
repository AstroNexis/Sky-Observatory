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

import java.time.Instant;

/**
 * Represents an astronomical time as a Julian Date (JD) in the Terrestrial Time (TT) scale.
 *
 * Julian Dates are the standard time representation used in astrometry.
 * Instances are immutable.
 */
public final class AstroTime {

    // Julian Date of the J2000.0 epoch (2000 January 1.5 TT)
    public static final double J2000_EPOCH = 2451545.0;

    // Julian Date of the Unix epoch (1970 January 1.0 UTC), approximated as TT.
    // The difference between TT and UTC (currently ~69 seconds) is negligible for Phase 1.
    private static final double JD_UNIX_EPOCH = 2440587.5;

    // Seconds per day
    private static final double SECONDS_PER_DAY = 86400.0;

    private final double julianDateTT;

    /**
     * @param julianDateTT Julian Date in Terrestrial Time scale
     */
    public AstroTime(double julianDateTT) {
        this.julianDateTT = julianDateTT;
    }

    /**
     * Returns the Julian Date in TT.
     */
    public double getJulianDateTT() {
        return julianDateTT;
    }

    /**
     * Returns the number of Julian centuries elapsed since J2000.0.
     * This is the standard "T" parameter used in many astronomical formulae.
     */
    public double julianCenturiesFromJ2000() {
        return (julianDateTT - J2000_EPOCH) / 36525.0;
    }

    /**
     * Creates an AstroTime representing the J2000.0 epoch.
     */
    public static AstroTime j2000() {
        return new AstroTime(J2000_EPOCH);
    }

    /**
     * Creates an AstroTime from a {@link java.time.Instant}.
     *
     * Converts the instant to a Julian Date using the Unix epoch offset.
     * The conversion treats UTC as TT, which introduces an error of ~69 seconds
     * (the current TT-UTC offset). This is acceptable for Phase 1 stub calculations
     * and will be refined in Phase 2 with a proper UTC-to-TT conversion.
     *
     * @param instant the instant to convert; must not be null
     * @return an AstroTime representing the given instant
     */
    public static AstroTime fromInstant(Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("Instant must not be null");
        }
        double julianDate = JD_UNIX_EPOCH
                + instant.getEpochSecond() / SECONDS_PER_DAY
                + instant.getNano() / (SECONDS_PER_DAY * 1_000_000_000.0);
        return new AstroTime(julianDate);
    }

    /**
     * Creates an AstroTime representing the current system time.
     *
     * Equivalent to {@code AstroTime.fromInstant(Instant.now())}.
     * See {@link #fromInstant(Instant)} for accuracy notes.
     *
     * @return an AstroTime representing now
     */
    public static AstroTime now() {
        return fromInstant(Instant.now());
    }

    @Override
    public String toString() {
        return "AstroTime{JD(TT)=" + julianDateTT + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AstroTime)) return false;
        AstroTime that = (AstroTime) other;
        return Double.compare(that.julianDateTT, julianDateTT) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(julianDateTT);
    }
}
