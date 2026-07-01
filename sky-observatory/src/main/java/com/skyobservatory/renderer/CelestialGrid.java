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

package com.skyobservatory.renderer;

import com.skyobservatory.scene.MeshRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * International Celestial Reference System (ICRS) / NASA-style full-sphere grid.
 *
 * Produces:
 *   - Declination circles  (−90deg to +90deg, every 15deg)
 *   - Right-ascension half-circles (0h to 23h, every 1h = every 15deg)
 *   - Altitude / azimuth reference at 0deg altitude (horizon equatorial overlay)
 *
 * The grid spans the full 360deg celestial sphere so the observer is completely
 * surrounded. Grid lines below the mathematical horizon are included and
 * clipped by the depth buffer, matching NASA planetarium / Stellarium behaviour.
 *
 * Coordinate mapping (ENU world space, X=east, Y=up, Z=south):
 *   x =  cos(dec) * sin(ra)
 *   y =  sin(dec)
 *   z = -cos(dec) * cos(ra)
 * scaled to unit sphere radius R = 9.5 (inside sky dome at 200).
 */
public final class CelestialGrid {

    public static final class GridLine {
        public final MeshRenderer renderer;
        public final GridType type;

        public GridLine(MeshRenderer renderer, GridType type) {
            this.renderer = renderer;
            this.type     = type;
        }
    }

    public enum GridType {
        DECLINATION,
        RIGHT_ASCENSION,
        EQUATOR,
        PRIME_MERIDIAN
    }

    private static final float R          = 9.5f;
    private static final int   SEGMENTS   = 64;

    // Declination step: every 15deg -> 13 circles (−90 … +90 including poles)
    private static final int DEC_STEP     = 15;
    // RA step: every 15deg = 1 h -> 24 half-meridians
    private static final int RA_STEP      = 15;

    public List<GridLine> build() {
        List<GridLine> lines = new ArrayList<>();

        // --- Declination circles -90deg … +90deg every 15deg ---
        for (int decDeg = -90; decDeg <= 90; decDeg += DEC_STEP) {
            double decRad   = Math.toRadians(decDeg);
            double cosD     = Math.cos(decRad);
            double sinD     = Math.sin(decRad);
            double circleR  = R * cosD;
            double circleY  = R * sinD;

            if (Math.abs(circleR) < 0.05) {
                // Poles: draw a tiny cross instead of a degenerate circle
                lines.add(buildPoleMarker((float) circleY));
                continue;
            }

            float[] verts = new float[SEGMENTS * 2 * 3];
            int idx = 0;
            for (int i = 0; i < SEGMENTS; i++) {
                double ra1 = i       * 2.0 * Math.PI / SEGMENTS;
                double ra2 = (i + 1) * 2.0 * Math.PI / SEGMENTS;
                verts[idx++] = (float)(circleR * Math.sin(ra1));
                verts[idx++] = (float) circleY;
                verts[idx++] = (float)(-circleR * Math.cos(ra1));
                verts[idx++] = (float)(circleR * Math.sin(ra2));
                verts[idx++] = (float) circleY;
                verts[idx++] = (float)(-circleR * Math.cos(ra2));
            }
            GridType type = (decDeg == 0) ? GridType.EQUATOR : GridType.DECLINATION;
            lines.add(makeGridLine(verts, type));
        }

        // --- Right-ascension half-meridians 0deg…345deg every 15deg ---
        for (int raDeg = 0; raDeg < 360; raDeg += RA_STEP) {
            double raRad = Math.toRadians(raDeg);
            // Full meridian: dec −90deg to +90deg
            float[] verts = new float[SEGMENTS * 2 * 3];
            int idx = 0;
            for (int i = 0; i < SEGMENTS; i++) {
                double dec1 = -Math.PI / 2.0 + i       * Math.PI / SEGMENTS;
                double dec2 = -Math.PI / 2.0 + (i + 1) * Math.PI / SEGMENTS;
                verts[idx++] = (float)(R * Math.cos(dec1) * Math.sin(raRad));
                verts[idx++] = (float)(R * Math.sin(dec1));
                verts[idx++] = (float)(-R * Math.cos(dec1) * Math.cos(raRad));
                verts[idx++] = (float)(R * Math.cos(dec2) * Math.sin(raRad));
                verts[idx++] = (float)(R * Math.sin(dec2));
                verts[idx++] = (float)(-R * Math.cos(dec2) * Math.cos(raRad));
            }
            GridType type = (raDeg == 0) ? GridType.PRIME_MERIDIAN : GridType.RIGHT_ASCENSION;
            lines.add(makeGridLine(verts, type));
        }

        return lines;
    }

    /** Small cross marker at a celestial pole. */
    private GridLine buildPoleMarker(float y) {
        float arm = 0.3f;
        float[] verts = {
            -arm, y, 0f,  arm, y, 0f,
             0f, y, -arm,  0f, y, arm,
        };
        MeshRenderer mr = new MeshRenderer();
        mr.uploadLines(verts);
        return new GridLine(mr, GridType.DECLINATION);
    }

    private GridLine makeGridLine(float[] verts, GridType type) {
        MeshRenderer mr = new MeshRenderer();
        mr.uploadLines(verts);
        return new GridLine(mr, type);
    }
}
