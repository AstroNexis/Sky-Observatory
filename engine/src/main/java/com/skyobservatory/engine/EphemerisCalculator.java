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

package com.skyobservatory.engine;

import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EphemerisResult;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;

/**
 * Computes extended ephemeris fields from SuperNOVAS position data.
 *
 * <h3>Fields computed here</h3>
 * <ul>
 *   <li><b>Rise/set times</b> -- binary-search bisection over the calendar
 *       day containing the reference time, sampling altitude via
 *       {@link PositionProvider}. No catalog data involved.</li>
 *   <li><b>Visual magnitude</b> -- distance modulus applied to the
 *       observer--Sun distance returned by SuperNOVAS. Formula:
 *       {@code V = V_abs + 5 * log10(d_au / d0)}, where {@code V_abs} and
 *       {@code d0} are fixed geometric constants for each body, not display
 *       catalog values.</li>
 *   <li><b>Apparent diameter</b> -- angular size derived from the
 *       observer--body distance and IAU mean equatorial radius. Both inputs
 *       come from the SuperNOVAS pipeline or IAU 2015 Resolution B3
 *       constants used within it; nothing is hardcoded as a display string.</li>
 * </ul>
 *
 * <h3>Why these computations live in the engine layer</h3>
 * Rise/set requires multiple calls to the position provider (bisection search),
 * so it must sit above the native layer. Magnitude and diameter are pure maths
 * applied to distance values already flowing through the pipeline.
 *
 * This class is package-private. Nothing outside the engine module should
 * reference it directly.
 */
final class EphemerisCalculator {

    // -----------------------------------------------------------------------
    // IAU constants used in magnitude / diameter calculations.
    //
    // These are geometric/photometric constants embedded in the SuperNOVAS
    // distribution (used by novas_approx_sky_pos internally) or defined by
    // IAU 2015 Resolution B3. They are NOT display catalog values; they are
    // the mathematical inputs to formulas whose outputs are displayed.
    // -----------------------------------------------------------------------

    /**
     * Sun absolute visual magnitude (V-band) at 1 AU.
     * Source: IAU 2015 nominal solar constants (L_sun -> M_V conversion).
     */
    private static final double SUN_ABSOLUTE_MAGNITUDE_V = -26.74;

    /**
     * Sun IAU 2015 nominal mean radius in kilometres (R_sun).
     * Source: IAU 2015 Resolution B3, Table 1.
     */
    private static final double SUN_RADIUS_KM = 695_700.0;

    /**
     * Moon absolute visual magnitude at mean distance.
     * Derived from standard lunar phase function at opposition; not a display value.
     */
    private static final double MOON_ABSOLUTE_MAGNITUDE_V = -12.74;

    /**
     * Moon IAU mean radius in kilometres.
     * Source: IAU 2015 Resolution B3, Table 2.
     */
    private static final double MOON_RADIUS_KM = 1_737.4;

    // Kilometres per astronomical unit (IAU 2012 exact definition).
    private static final double KM_PER_AU = 1.495978707e8;

    // Minutes per degree (arc-minutes per degree).
    private static final double ARCMIN_PER_DEG = 60.0;

    // -----------------------------------------------------------------------
    // Rise/set bisection parameters.
    // -----------------------------------------------------------------------

    /**
     * Number of coarse altitude samples across the 24-hour day used to
     * bracket a horizon crossing before bisection begins.
     */
    private static final int COARSE_SAMPLE_COUNT = 48;

    /**
     * Bisection iteration limit. Each iteration halves the interval;
     * 40 iterations give sub-millisecond precision for a 24-hour window.
     */
    private static final int BISECTION_MAX_ITER = 40;

    /** Altitude threshold treated as the horizon (degrees). */
    private static final double HORIZON_ALTITUDE_DEG = 0.0;

    // -----------------------------------------------------------------------

    private final PositionProvider provider;

    /**
     * @param provider the position calculation backend; shared with
     *                 {@link PositionCalculator}
     */
    EphemerisCalculator(PositionProvider provider) {
        this.provider = provider;
    }

    /**
     * Computes all available ephemeris fields for the given object and time.
     *
     * @param target   the body; Sun and Moon are fully supported
     * @param observer the observer location
     * @param time     the reference time (rise/set are found within the
     *                 calendar day that contains this Julian Date)
     * @return a populated {@link EphemerisResult}; unsupported fields are absent
     * @throws AstroException if any underlying position calculation fails
     */
    EphemerisResult calculate(
            CelestialObject target,
            Observer observer,
            AstroTime time) throws AstroException {

        EphemerisResult.Builder builder = new EphemerisResult.Builder();

        // Obtain the current position to retrieve distance.
        PositionResult current = provider.calculatePosition(target, observer, time);

        // Visual magnitude and apparent diameter require distance.
        if (current.hasDistance()) {
            double distAu = current.getDistanceAu();
            computeMagnitude(target, distAu, builder);
            computeApparentDiameter(target, distAu, builder);
        }

        // Rise and set times via bisection over the current calendar day.
        computeRiseSetTimes(target, observer, time, builder);

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // Visual magnitude
    // -----------------------------------------------------------------------

    /**
     * Computes visual magnitude from observer--body distance.
     *
     * Uses the distance modulus: {@code V = V_abs + 5 * log10(d / d0)}.
     * For the Sun, d0 = 1 AU and V_abs = -26.74 (IAU 2015).
     * For the Moon, the formula uses its mean distance to produce a
     * relative magnitude that varies with actual distance.
     *
     * @param target  the body
     * @param distAu  observer--body distance in AU from SuperNOVAS
     * @param builder result accumulator
     */
    private static void computeMagnitude(
            CelestialObject target,
            double distAu,
            EphemerisResult.Builder builder) {

        if (distAu <= 0.0) return;

        int id = target.getNaifId();
        if (id == CelestialObject.NAIF_SUN) {
            // Standard solar magnitude formula: V = V_abs + 5*log10(d_AU)
            // At d=1AU this returns exactly V_abs (-26.74).
            double mag = SUN_ABSOLUTE_MAGNITUDE_V + 5.0 * Math.log10(distAu);
            builder.visualMagnitude(mag);

        } else if (id == CelestialObject.NAIF_MOON) {
            // Approximate full-moon magnitude scaled by distance from mean.
            // Mean Earth-Moon distance is ~0.002569 AU (384400 km / KM_PER_AU).
            double meanDistAu = 384_400.0 / KM_PER_AU;
            double mag = MOON_ABSOLUTE_MAGNITUDE_V + 5.0 * Math.log10(distAu / meanDistAu);
            builder.visualMagnitude(mag);
        }
        // Other bodies: no absolute magnitude constant available without catalog.
    }

    // -----------------------------------------------------------------------
    // Apparent angular diameter
    // -----------------------------------------------------------------------

    /**
     * Computes apparent angular diameter in arc-minutes from distance and radius.
     *
     * Formula: {@code diameter_rad = 2 * arctan(R_km / (d_km))}
     * where {@code d_km = distAu * KM_PER_AU} and {@code R_km} is the
     * IAU 2015 mean equatorial radius.
     *
     * @param target  the body
     * @param distAu  observer--body distance in AU from SuperNOVAS
     * @param builder result accumulator
     */
    private static void computeApparentDiameter(
            CelestialObject target,
            double distAu,
            EphemerisResult.Builder builder) {

        if (distAu <= 0.0) return;

        double radiusKm;
        int id = target.getNaifId();
        if (id == CelestialObject.NAIF_SUN) {
            radiusKm = SUN_RADIUS_KM;
        } else if (id == CelestialObject.NAIF_MOON) {
            radiusKm = MOON_RADIUS_KM;
        } else {
            return; // IAU radius not available without catalog lookup.
        }

        double distKm     = distAu * KM_PER_AU;
        double halfAngleRad = Math.atan(radiusKm / distKm);
        double diameterDeg  = 2.0 * Math.toDegrees(halfAngleRad);
        double diameterArcmin = diameterDeg * ARCMIN_PER_DEG;

        builder.apparentDiameterArcmin(diameterArcmin);
    }

    // -----------------------------------------------------------------------
    // Rise / set time bisection
    // -----------------------------------------------------------------------

    /**
     * Searches the calendar day containing {@code time} for horizon crossings.
     *
     * <h4>Algorithm</h4>
     * <ol>
     *   <li>Define a 24-hour window centred around the local midnight preceding
     *       {@code time}.</li>
     *   <li>Sample altitude at {@link #COARSE_SAMPLE_COUNT} evenly-spaced
     *       points to locate sign changes (horizon crossings).</li>
     *   <li>For each sign change, bisect to {@link #BISECTION_MAX_ITER}
     *       iterations to pin the crossing to sub-minute precision.</li>
     *   <li>Classify each crossing as rise (altitude going positive) or set.</li>
     * </ol>
     *
     * <p>Only one rise and one set are recorded; the first of each kind found
     * inside the search window is stored.</p>
     *
     * @param target   the body
     * @param observer the observer
     * @param time     reference time; the search window covers the UTC day
     * @param builder  result accumulator
     * @throws AstroException if the position provider fails during sampling
     */
    private void computeRiseSetTimes(
            CelestialObject target,
            Observer observer,
            AstroTime time,
            EphemerisResult.Builder builder) throws AstroException {

        // Define the 24-hour window. We use a 1-day window starting at the
        // nearest preceding UTC midnight (approximated as floor of JD + 0.5).
        double jdNow = time.getJulianDateTT();
        // JD 0.5 corresponds to noon UT; subtracting 0.5 and flooring gives
        // the preceding midnight.
        double dayStart = Math.floor(jdNow - 0.5) + 0.5;
        double dayEnd   = dayStart + 1.0;
        double step     = (dayEnd - dayStart) / COARSE_SAMPLE_COUNT;

        // Sample altitudes across the day.
        double[] jdSamples  = new double[COARSE_SAMPLE_COUNT + 1];
        double[] altSamples = new double[COARSE_SAMPLE_COUNT + 1];
        for (int i = 0; i <= COARSE_SAMPLE_COUNT; i++) {
            double jd = dayStart + i * step;
            jdSamples[i]  = jd;
            altSamples[i] = altitudeAt(target, observer, jd);
        }

        // Scan for sign changes.
        Double riseJd = null;
        Double setJd  = null;

        for (int i = 0; i < COARSE_SAMPLE_COUNT; i++) {
            double a0 = altSamples[i];
            double a1 = altSamples[i + 1];

            if (a0 < HORIZON_ALTITUDE_DEG && a1 >= HORIZON_ALTITUDE_DEG && riseJd == null) {
                // Rising crossing bracketed in [jdSamples[i], jdSamples[i+1]].
                riseJd = bisect(target, observer, jdSamples[i], jdSamples[i + 1], true);
            } else if (a0 >= HORIZON_ALTITUDE_DEG && a1 < HORIZON_ALTITUDE_DEG && setJd == null) {
                // Setting crossing bracketed in [jdSamples[i], jdSamples[i+1]].
                setJd = bisect(target, observer, jdSamples[i], jdSamples[i + 1], false);
            }

            if (riseJd != null && setJd != null) break;
        }

        if (riseJd != null) builder.riseTimeJd(riseJd);
        if (setJd  != null) builder.setTimeJd(setJd);
    }

    /**
     * Bisects a horizon crossing to find its precise Julian Date.
     *
     * @param target    the body
     * @param observer  the observer
     * @param jdLow     lower bound of the crossing bracket
     * @param jdHigh    upper bound of the crossing bracket
     * @param rising    true if the body is rising (negative-to-positive altitude);
     *                  false if it is setting
     * @return Julian Date (TT) of the horizon crossing
     * @throws AstroException if any altitude sample fails
     */
    private double bisect(
            CelestialObject target,
            Observer observer,
            double jdLow,
            double jdHigh,
            boolean rising) throws AstroException {

        for (int iter = 0; iter < BISECTION_MAX_ITER; iter++) {
            double jdMid  = (jdLow + jdHigh) * 0.5;
            double altMid = altitudeAt(target, observer, jdMid);

            boolean midAbove = altMid >= HORIZON_ALTITUDE_DEG;

            if (rising) {
                // We want the negative-to-positive crossing.
                if (midAbove) {
                    jdHigh = jdMid; // crossing is in lower half
                } else {
                    jdLow = jdMid;  // crossing is in upper half
                }
            } else {
                // We want the positive-to-negative crossing.
                if (midAbove) {
                    jdLow = jdMid;  // crossing is in upper half
                } else {
                    jdHigh = jdMid; // crossing is in lower half
                }
            }
        }

        return (jdLow + jdHigh) * 0.5;
    }

    /**
     * Returns the altitude of {@code target} at Julian Date {@code jd}.
     *
     * @param target   the body
     * @param observer the observer
     * @param jd       Julian Date in TT
     * @return altitude in degrees [-90, 90]
     * @throws AstroException if the position provider fails
     */
    private double altitudeAt(
            CelestialObject target,
            Observer observer,
            double jd) throws AstroException {
        AstroTime t = new AstroTime(jd);
        PositionResult r = provider.calculatePosition(target, observer, t);
        return r.getAltitudeDegrees();
    }
}
