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

import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.resources.SkyResources;
import com.skyobservatory.scene.MeshRenderer;
import com.skyobservatory.scene.SphereMesh;

/**
 * Builds GPU-ready {@link ObservableObjectEntry} instances from
 * {@link ObservableObject} descriptors.
 *
 * All object-type-specific logic (sphere radius, texture selection) lives
 * here and nowhere else. {@link SkyRenderer} only iterates the resulting
 * list -- it has no knowledge of Sun, Moon, or any other specific body.
 *
 * Prepared for GPU instancing: all bodies of the same category share the
 * same {@link SphereMesh} prototype (created once, reused by reference).
 */
final class CelestialObjectFactory {

    private static final float RADIUS_SUN     = 0.75f;
    private static final float RADIUS_MOON    = 0.5f;
    private static final float RADIUS_PLANET  = 0.2f;
    private static final float RADIUS_DEFAULT = 0.15f;

    private static final int STACKS = 16;
    private static final int SLICES = 24;

    // Shared sphere prototypes -- one per size bucket.
    private final SphereMesh largeSphere  = new SphereMesh(RADIUS_SUN,     STACKS, SLICES);
    private final SphereMesh mediumSphere = new SphereMesh(RADIUS_MOON,    STACKS, SLICES);
    private final SphereMesh smallSphere  = new SphereMesh(RADIUS_PLANET,  STACKS, SLICES);
    private final SphereMesh starSphere   = new SphereMesh(RADIUS_DEFAULT, STACKS, SLICES);

    private final SkyResources resources;

    CelestialObjectFactory(SkyResources resources) {
        this.resources = resources;
    }

    /**
     * Produces an {@link ObservableObjectEntry} for {@code obj}.
     *
     * The body mesh is positioned off-screen until the first position
     * update arrives via {@link SkyRenderer#updateSnapshot}.
     */
    ObservableObjectEntry build(ObservableObject obj) {
        SphereMesh proto = protoFor(obj.getCategory());
        int texId        = textureFor(obj);
        String name      = obj.getTarget().getName();
        int labelTexId   = resources.getOrCreateLabelTexture(name);
        float aspect     = resources.getLabelAspect(name);

        MeshRenderer body = new MeshRenderer();
        body.upload(proto);
        body.modelMatrix.set(13, -500f); // hidden until first update

        MeshRenderer label = buildLabelQuad();

        return new ObservableObjectEntry(obj, body, label, texId, labelTexId, aspect, name);
    }

    private SphereMesh protoFor(ObservableObject.ObjectCategory cat) {
        switch (cat) {
            case SOLAR_SYSTEM_BODY: return largeSphere;
            case MOON:              return mediumSphere;
            case PLANET:            return smallSphere;
            default:                return starSphere;
        }
    }

    private int textureFor(ObservableObject obj) {
        switch (obj.getTarget().getNaifId()) {
            case com.skyobservatory.api.CelestialObject.NAIF_SUN:  return resources.sunTextureId;
            case com.skyobservatory.api.CelestialObject.NAIF_MOON: return resources.moonTextureId;
            default: return resources.defaultStarTextureId;
        }
    }

    /**
     * Creates a unit quad for the 2D screen-space label pass.
     *
     * Local coordinates are in [-1, +1] so the vertex shader can scale them
     * to NDC half-extents (uNdcHalfW / uNdcHalfH) without a separate
     * model matrix.  Attribute location 0 is used as a vec2; the Z component
     * is unused by the label shader but set to 0 for buffer layout
     * compatibility with {@link MeshRenderer#uploadBillboard}.
     */
    private MeshRenderer buildLabelQuad() {
        // Six vertices (two triangles), local xy in [-1,+1], z = 0.
        float[] verts = {
            -1f, -1f, 0f,   1f, -1f, 0f,   1f, 1f, 0f,
            -1f, -1f, 0f,   1f,  1f, 0f,  -1f, 1f, 0f,
        };
        float[] uvs = {
            0f, 1f,  1f, 1f,  1f, 0f,
            0f, 1f,  1f, 0f,  0f, 0f,
        };
        MeshRenderer mr = new MeshRenderer();
        mr.uploadBillboard(verts, uvs, 0f, -500f, 0f);
        return mr;
    }
}
