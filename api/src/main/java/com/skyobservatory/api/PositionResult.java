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
 * The result of a sky position calculation.
 *
 * Horizontal coordinates (azimuth, altitude) are always present. Equatorial
 * coordinates, distance, and the ICRS position vector are populated by the
 * calculation engine when available. Callers must check presence before use.
 *
 * <h3>Coordinate conventions</h3>
 * <ul>
 *   <li>Azimuth: eastward from true north -- 0 = North, 90 = East, 180 = South, 270 = West.</li>
 *   <li>Altitude: from the horizon -- 0 = on horizon, +90 = directly overhead.</li>
 *   <li>Right ascension and declination: epoch defined by the producing engine layer.</li>
 *   <li>Distance: AU from the observer to the target body.</li>
 *   <li>Position vector: ICRS Cartesian position in AU.</li>
 * </ul>
 *
 * All angular values are in degrees. Instances are immutable.
 *
 * <h3>Building</h3>
 * Use {@link Builder} to construct instances with optional fields:
 * <pre>{@code
 * PositionResult result = new PositionResult.Builder(az, alt, target, observer, time)
 *         .equatorial(new EquatorialCoordinates(ra, dec))
 *         .distanceAu(1.0)
 *         .positionVector(new Vector3(x, y, z))
 *         .build();
 * }</pre>
 *
 * The legacy 5-argument constructor is retained for backwards compatibility and
 * produces a result with only horizontal coordinates populated.
 */
public final class PositionResult {

    private final double azimuthDegrees;
    private final double altitudeDegrees;
    private final CelestialObject target;
    private final Observer observer;
    private final AstroTime time;

    // Optional fields -- null means "not computed by this engine/phase".
    private final EquatorialCoordinates equatorial;
    private final Double distanceAu;
    private final Vector3 positionVector;

    /**
     * Constructs a result with horizontal coordinates only.
     *
     * @param azimuthDegrees  topocentric azimuth in degrees [0, 360)
     * @param altitudeDegrees topocentric altitude in degrees [-90, 90]
     * @param target          the object that was positioned
     * @param observer        the observer location used for the calculation
     * @param time            the time at which the calculation was performed
     */
    public PositionResult(
            double azimuthDegrees,
            double altitudeDegrees,
            CelestialObject target,
            Observer observer,
            AstroTime time) {
        this(new Builder(azimuthDegrees, altitudeDegrees, target, observer, time));
    }

    private PositionResult(Builder builder) {
        this.azimuthDegrees = builder.azimuthDegrees;
        this.altitudeDegrees = builder.altitudeDegrees;
        this.target = builder.target;
        this.observer = builder.observer;
        this.time = builder.time;
        this.equatorial = builder.equatorial;
        this.distanceAu = builder.distanceAu;
        this.positionVector = builder.positionVector;
    }

    /** Returns the topocentric azimuth in degrees [0, 360). */
    public double getAzimuthDegrees() { return azimuthDegrees; }

    /** Returns the topocentric altitude in degrees [-90, 90]. */
    public double getAltitudeDegrees() { return altitudeDegrees; }

    /** Returns the target body. */
    public CelestialObject getTarget() { return target; }

    /** Returns the observer location used for this calculation. */
    public Observer getObserver() { return observer; }

    /** Returns the observation time. */
    public AstroTime getTime() { return time; }

    /**
     * Returns the equatorial coordinates (RA/Dec) if computed, or {@code null}.
     * Phase 2+ populates this field.
     */
    public EquatorialCoordinates getEquatorial() { return equatorial; }

    /**
     * Returns whether equatorial coordinates are present in this result.
     */
    public boolean hasEquatorial() { return equatorial != null; }

    /**
     * Returns the distance from the observer to the target body in AU,
     * or {@code null} if not computed.
     * Phase 2+ populates this field.
     */
    public Double getDistanceAu() { return distanceAu; }

    /**
     * Returns whether the distance field is present in this result.
     */
    public boolean hasDistance() { return distanceAu != null; }

    /**
     * Returns the ICRS Cartesian position vector of the target in AU,
     * or {@code null} if not computed.
     *
     * This field is the primary input for the OpenGL and AR rendering layers.
     * Phase 2+ populates this field.
     */
    public Vector3 getPositionVector() { return positionVector; }

    /**
     * Returns whether the position vector is present in this result.
     */
    public boolean hasPositionVector() { return positionVector != null; }

    /** Returns {@code true} when the object is above the observer's horizon. */
    public boolean isAboveHorizon() { return altitudeDegrees > 0.0; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PositionResult{")
                .append("target=").append(target.getName())
                .append(", az=").append(azimuthDegrees).append("\u00b0")
                .append(", alt=").append(altitudeDegrees).append("\u00b0");
        if (equatorial != null) {
            sb.append(", RA=").append(equatorial.getRightAscensionDegrees()).append("\u00b0")
              .append(", Dec=").append(equatorial.getDeclinationDegrees()).append("\u00b0");
        }
        if (distanceAu != null) {
            sb.append(", dist=").append(distanceAu).append("AU");
        }
        sb.append("}");
        return sb.toString();
    }

    // Builder

    /**
     * Builder for {@link PositionResult}.
     *
     * The horizontal coordinate fields are mandatory. All other fields are optional
     * and default to {@code null} (not computed).
     */
    public static final class Builder {

        private final double azimuthDegrees;
        private final double altitudeDegrees;
        private final CelestialObject target;
        private final Observer observer;
        private final AstroTime time;

        private EquatorialCoordinates equatorial;
        private Double distanceAu;
        private Vector3 positionVector;

        /**
         * @param azimuthDegrees  topocentric azimuth in degrees [0, 360)
         * @param altitudeDegrees topocentric altitude in degrees [-90, 90]
         * @param target          the target body; must not be null
         * @param observer        the observer; must not be null
         * @param time            the observation time; must not be null
         */
        public Builder(
                double azimuthDegrees,
                double altitudeDegrees,
                CelestialObject target,
                Observer observer,
                AstroTime time) {
            if (target == null) throw new IllegalArgumentException("target must not be null");
            if (observer == null) throw new IllegalArgumentException("observer must not be null");
            if (time == null) throw new IllegalArgumentException("time must not be null");
            this.azimuthDegrees = azimuthDegrees;
            this.altitudeDegrees = altitudeDegrees;
            this.target = target;
            this.observer = observer;
            this.time = time;
        }

        /**
         * Sets the equatorial coordinates (RA/Dec) for this result.
         *
         * @param equatorial the equatorial coordinates; may be null to clear
         * @return this builder
         */
        public Builder equatorial(EquatorialCoordinates equatorial) {
            this.equatorial = equatorial;
            return this;
        }

        /**
         * Sets the distance from the observer to the target in AU.
         *
         * @param distanceAu distance in astronomical units; must be non-negative
         * @return this builder
         */
        public Builder distanceAu(double distanceAu) {
            if (distanceAu < 0.0) {
                throw new IllegalArgumentException("distanceAu must be non-negative, got: " + distanceAu);
            }
            this.distanceAu = distanceAu;
            return this;
        }

        /**
         * Sets the ICRS Cartesian position vector of the target in AU.
         *
         * @param positionVector the position vector; may be null to clear
         * @return this builder
         */
        public Builder positionVector(Vector3 positionVector) {
            this.positionVector = positionVector;
            return this;
        }

        /** Builds the {@link PositionResult}. */
        public PositionResult build() {
            return new PositionResult(this);
        }
    }
}
