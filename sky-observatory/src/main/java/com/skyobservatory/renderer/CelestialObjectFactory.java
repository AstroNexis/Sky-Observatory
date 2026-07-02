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

import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.resources.SkyResources;
import com.skyobservatory.scene.MeshRenderer;
import com.skyobservatory.scene.SphereMesh;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds GPU-ready {@link ObservableObjectEntry} instances from
 * {@link ObservableObject} descriptors.
 *
 * All object-type-specific data (sphere radius, texture) comes from
 * {@link CelestialObject}'s catalog fields. No switch statements on
 * NAIF IDs or categories live here.
 *
 * Sphere meshes are shared per radius bucket to avoid duplicate GPU uploads.
 */
final class CelestialObjectFactory {

    private static final int STACKS = 16;
    private static final int SLICES = 24;

    private final SkyResources resources;
    /** Reuse mesh objects that share the same radius to avoid duplicate uploads. */
    private final Map<Float, SphereMesh> sphereCache = new HashMap<>();

    CelestialObjectFactory(SkyResources resources) {
        this.resources = resources;
    }

    ObservableObjectEntry build(ObservableObject obj) {
        CelestialObject def  = obj.getTarget();
        SphereMesh proto     = sphereFor(def.getRenderRadius());
        int texId            = resources.getOrCreateObjectTexture(def);
        String name          = def.getName();
        int labelTexId       = resources.getOrCreateLabelTexture(name);
        float aspect         = resources.getLabelAspect(name);

        MeshRenderer body = new MeshRenderer();
        body.upload(proto);
        body.modelMatrix.set(13, -500f);

        MeshRenderer label = buildLabelQuad();

        return new ObservableObjectEntry(obj, body, label, texId, labelTexId, aspect, name);
    }

    private SphereMesh sphereFor(float radius) {
        SphereMesh cached = sphereCache.get(radius);
        if (cached != null) return cached;
        SphereMesh mesh = new SphereMesh(radius, STACKS, SLICES);
        sphereCache.put(radius, mesh);
        return mesh;
    }

    private MeshRenderer buildLabelQuad() {
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
