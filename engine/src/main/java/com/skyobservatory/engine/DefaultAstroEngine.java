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

import com.skyobservatory.api.AstroEngine;
import com.skyobservatory.api.AstroException;
import com.skyobservatory.api.AstroTime;
import com.skyobservatory.api.CartesianCoordinate;
import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.EphemerisResult;
import com.skyobservatory.api.HorizontalCoordinate;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.Observer;
import com.skyobservatory.api.PositionResult;
import com.skyobservatory.api.SkyCoordinate;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.api.Viewport;
import com.skyobservatory.api.VisibleSkyRegion;
import com.skyobservatory.api.VisibilityState;
import com.skyobservatory.engine.projector.CoordinateProjector;
import com.skyobservatory.engine.viewport.VisibilityResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * The default {@link AstroEngine} implementation.
 *
 * This class is intentionally package-private. It is not part of the public API.
 * Applications always interact through the {@link AstroEngine} interface obtained
 * from {@link com.skyobservatory.api.AstroSdk}.
 */
final class DefaultAstroEngine implements AstroEngine {

    private final PositionCalculator positionCalculator;
    private final CoordinateProjector projector;
    private final VisibilityResolver visibilityResolver;
    private final EphemerisCalculator ephemerisCalculator;

    DefaultAstroEngine(PositionCalculator positionCalculator, EphemerisCalculator ephemerisCalculator) {
        this.positionCalculator   = positionCalculator;
        this.ephemerisCalculator  = ephemerisCalculator;
        this.projector            = new CoordinateProjector();
        this.visibilityResolver   = new VisibilityResolver();
    }

    @Override
    public PositionResult calculatePosition(
            CelestialObject target,
            Observer observer,
            AstroTime time) throws AstroException {
        return positionCalculator.calculate(target, observer, time);
    }

    @Override
    public EphemerisResult calculateEphemeris(
            CelestialObject target,
            Observer observer,
            AstroTime time) throws AstroException {
        return ephemerisCalculator.calculate(target, observer, time);
    }

    @Override
    public SkyCoordinate project(HorizontalCoordinate coordinate) {
        return projector.project(coordinate.getAzimuthDegrees(), coordinate.getAltitudeDegrees());
    }

    @Override
    public SkySnapshot createSnapshot(
            List<CelestialObject> targets,
            Observer observer,
            AstroTime time) throws AstroException {

        List<ObservableObject> objects = new ArrayList<>(targets.size());

        for (CelestialObject target : targets) {
            PositionResult result = positionCalculator.calculate(target, observer, time);
            boolean isAbove = result.isAboveHorizon();
            VisibilityState visibility = isAbove
                    ? VisibilityState.VISIBLE : VisibilityState.BELOW_HORIZON;

            HorizontalCoordinate horizontal = new HorizontalCoordinate(
                    result.getAzimuthDegrees(), result.getAltitudeDegrees());
            CartesianCoordinate cartesian = projector.toCartesian(horizontal);
            SkyCoordinate position = new SkyCoordinate(horizontal, cartesian);

            ObservableObject.ObjectCategory category = resolveCategory(target);

            objects.add(new ObservableObject(target, position, visibility, category));
        }

        return new SkySnapshot.Builder(time, observer, objects).build();
    }

    @Override
    public VisibleSkyRegion calculateVisibleRegion(SkySnapshot snapshot, Viewport viewport) {
        return visibilityResolver.resolve(snapshot, viewport);
    }

    private static ObservableObject.ObjectCategory resolveCategory(CelestialObject target) {
        int id = target.getNaifId();
        if (id == CelestialObject.NAIF_SUN) return ObservableObject.ObjectCategory.STAR;
        if (id == CelestialObject.NAIF_MOON) return ObservableObject.ObjectCategory.MOON;
        if (id == CelestialObject.NAIF_MERCURY
                || id == CelestialObject.NAIF_VENUS
                || id == CelestialObject.NAIF_MARS
                || id == CelestialObject.NAIF_JUPITER
                || id == CelestialObject.NAIF_SATURN
                || id == CelestialObject.NAIF_URANUS
                || id == CelestialObject.NAIF_NEPTUNE) {
            return ObservableObject.ObjectCategory.PLANET;
        }
        return ObservableObject.ObjectCategory.SOLAR_SYSTEM_BODY;
    }
}
