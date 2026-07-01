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
import com.skyobservatory.scene.MeshRenderer;

/**
 * Renderer-side entry for a single celestial object.
 *
 * Bundles the GPU mesh, texture ID, and label texture together with
 * the source {@link ObservableObject} so the draw loop can treat all
 * objects uniformly without any per-type branching.
 *
 * This is produced by {@link CelestialObjectFactory} and consumed only
 * by {@link SkyRenderer}. The renderer never inspects object type
 * except to delegate visual sizing to the factory.
 */
final class ObservableObjectEntry {

    final ObservableObject source;
    final MeshRenderer bodyMesh;
    final MeshRenderer labelMesh;
    final int textureId;
    final int labelTextureId;

    /**
     * Width-to-height ratio of the label texture bitmap.
     * Used by the 2D screen-space label pass to size the quad correctly
     * so that no character is clipped regardless of name length.
     */
    final float labelAspect;

    /** Display name cached here to avoid repeated JNI calls in the draw loop. */
    final String displayName;

    ObservableObjectEntry(
            ObservableObject source,
            MeshRenderer bodyMesh,
            MeshRenderer labelMesh,
            int textureId,
            int labelTextureId,
            float labelAspect,
            String displayName) {
        this.source        = source;
        this.bodyMesh      = bodyMesh;
        this.labelMesh     = labelMesh;
        this.textureId     = textureId;
        this.labelTextureId = labelTextureId;
        this.labelAspect   = labelAspect;
        this.displayName   = displayName;
    }
}
