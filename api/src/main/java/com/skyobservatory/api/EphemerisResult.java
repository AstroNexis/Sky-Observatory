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

/**
 * Extended ephemeris data for a celestial body at a given observation.
 *
 * <p>Bundles fields that are computable from ephemeris data (SuperNOVAS output
 * and geometry derived from it) but do not fit inside {@link PositionResult},
 * which covers only the core positional calculation.</p>
 *
 * <p>All optional fields that cannot be produced for a given object or time
 * are represented as {@code null} (absent), not as sentinel numbers. Callers
 * must check presence with the corresponding {@code has*()} method before use.</p>
 *
 * <p>Field sourcing:</p>
 * <ul>
 *   <li><b>Rise/set times</b> -- computed by the engine via iterative altitude
 *       sampling; no catalog data involved.</li>
 *   <li><b>Visual magnitude</b> -- computed from distance and known solar
 *       absolute magnitude using the standard distance modulus formula.</li>
 *   <li><b>Apparent diameter</b> -- computed from distance and the body's
 *       physical radius, itself sourced from IAU-published constants that are
 *       part of the SuperNOVAS distribution (not injected as display strings).</li>
 * </ul>
 *
 * <p>Instances are immutable.</p>
 */
public final class EphemerisResult {

    // Optional computed fields -- null means "not available for this body/time".

    /** Rise time as a Julian Date in TT, or {@code null} if not computed. */
    private final Double riseTimeJd;

    /** Set time as a Julian Date in TT, or {@code null} if not computed. */
    private final Double setTimeJd;

    /**
     * Apparent visual magnitude, or {@code null} if not computable.
     *
     * Computed from the observer--body distance via the standard formula;
     * no catalog values are injected.
     */
    private final Double visualMagnitude;

    /**
     * Apparent angular diameter in arc-minutes, or {@code null} if not computable.
     *
     * Derived from the observer--body distance and the body's IAU mean radius.
     */
    private final Double apparentDiameterArcmin;

    private EphemerisResult(Builder builder) {
        this.riseTimeJd          = builder.riseTimeJd;
        this.setTimeJd           = builder.setTimeJd;
        this.visualMagnitude     = builder.visualMagnitude;
        this.apparentDiameterArcmin = builder.apparentDiameterArcmin;
    }

    /** Returns the rise time as a Julian Date in TT, or {@code null} if absent. */
    public Double getRiseTimeJd() { return riseTimeJd; }

    /** Returns {@code true} when a rise time was computed for this observation. */
    public boolean hasRiseTime() { return riseTimeJd != null; }

    /** Returns the set time as a Julian Date in TT, or {@code null} if absent. */
    public Double getSetTimeJd() { return setTimeJd; }

    /** Returns {@code true} when a set time was computed for this observation. */
    public boolean hasSetTime() { return setTimeJd != null; }

    /** Returns the apparent visual magnitude, or {@code null} if absent. */
    public Double getVisualMagnitude() { return visualMagnitude; }

    /** Returns {@code true} when a visual magnitude was computed. */
    public boolean hasVisualMagnitude() { return visualMagnitude != null; }

    /** Returns the apparent diameter in arc-minutes, or {@code null} if absent. */
    public Double getApparentDiameterArcmin() { return apparentDiameterArcmin; }

    /** Returns {@code true} when an apparent diameter was computed. */
    public boolean hasApparentDiameter() { return apparentDiameterArcmin != null; }

    @Override
    public String toString() {
        return "EphemerisResult{"
                + "rise=" + riseTimeJd
                + ", set=" + setTimeJd
                + ", mag=" + visualMagnitude
                + ", diam=" + apparentDiameterArcmin + "'"
                + "}";
    }

    /**
     * Builder for {@link EphemerisResult}.
     *
     * All fields are optional; any field left unset is absent in the result.
     */
    public static final class Builder {

        private Double riseTimeJd;
        private Double setTimeJd;
        private Double visualMagnitude;
        private Double apparentDiameterArcmin;

        public Builder() {}

        /**
         * Sets the rise time.
         *
         * @param riseTimeJd Julian Date in TT; must be finite
         * @return this builder
         */
        public Builder riseTimeJd(double riseTimeJd) {
            this.riseTimeJd = riseTimeJd;
            return this;
        }

        /**
         * Sets the set time.
         *
         * @param setTimeJd Julian Date in TT; must be finite
         * @return this builder
         */
        public Builder setTimeJd(double setTimeJd) {
            this.setTimeJd = setTimeJd;
            return this;
        }

        /**
         * Sets the apparent visual magnitude.
         *
         * @param visualMagnitude apparent magnitude (dimensionless)
         * @return this builder
         */
        public Builder visualMagnitude(double visualMagnitude) {
            this.visualMagnitude = visualMagnitude;
            return this;
        }

        /**
         * Sets the apparent angular diameter.
         *
         * @param apparentDiameterArcmin apparent diameter in arc-minutes; must be non-negative
         * @return this builder
         */
        public Builder apparentDiameterArcmin(double apparentDiameterArcmin) {
            if (apparentDiameterArcmin < 0.0) {
                throw new IllegalArgumentException(
                        "apparentDiameterArcmin must be non-negative, got: " + apparentDiameterArcmin);
            }
            this.apparentDiameterArcmin = apparentDiameterArcmin;
            return this;
        }

        /** Builds the {@link EphemerisResult}. */
        public EphemerisResult build() {
            return new EphemerisResult(this);
        }
    }
}
