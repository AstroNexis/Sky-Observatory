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

package com.skyobservatory.scene;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A flat ring (washer) mesh, used for Saturn's rings.
 *
 * <p>The ring lies in the XZ plane (Y=0) with configurable inner and outer
 * radii. UVs map u to the radial direction (0 = inner edge, 1 = outer edge)
 * and v to the angular position around the ring.</p>
 */
public final class RingMesh {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer texCoordBuffer;
    private final ShortBuffer indexBuffer;
    private final int indexCount;
    private final int vertexCount;

    /**
     * @param innerRadius  distance from centre to the inner edge
     * @param outerRadius  distance from centre to the outer edge
     * @param segments     number of subdivisions around the circumference
     */
    public RingMesh(float innerRadius, float outerRadius, int segments) {
        this.vertexCount = (segments + 1) * 2;
        this.indexCount = segments * 6;

        float[] verts = new float[vertexCount * 3];
        float[] uvs   = new float[vertexCount * 2];
        short[] idx   = new short[indexCount];

        int vi = 0, ti = 0;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2.0 * Math.PI / segments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Inner vertex
            verts[vi]     = innerRadius * cos;
            verts[vi + 1] = 0f;
            verts[vi + 2] = innerRadius * sin;
            uvs[ti]       = 0f;
            uvs[ti + 1]   = (float) i / segments;
            vi += 3;
            ti += 2;

            // Outer vertex
            verts[vi]     = outerRadius * cos;
            verts[vi + 1] = 0f;
            verts[vi + 2] = outerRadius * sin;
            uvs[ti]       = 1f;
            uvs[ti + 1]   = (float) i / segments;
            vi += 3;
            ti += 2;
        }

        int ix = 0;
        for (int i = 0; i < segments; i++) {
            int inner0 = i * 2;
            int outer0 = i * 2 + 1;
            int inner1 = (i + 1) * 2;
            int outer1 = (i + 1) * 2 + 1;

            idx[ix++] = (short) inner0;
            idx[ix++] = (short) outer0;
            idx[ix++] = (short) inner1;

            idx[ix++] = (short) inner1;
            idx[ix++] = (short) outer0;
            idx[ix++] = (short) outer1;
        }

        vertexBuffer   = MeshUtils.toFloatBuffer(verts);
        texCoordBuffer = MeshUtils.toFloatBuffer(uvs);
        indexBuffer    = MeshUtils.toShortBuffer(idx);
    }

    public FloatBuffer getVertexBuffer()   { return vertexBuffer; }
    public FloatBuffer getTexCoordBuffer() { return texCoordBuffer; }
    public ShortBuffer getIndexBuffer()    { return indexBuffer; }
    public int getIndexCount()             { return indexCount; }
    public int getVertexCount()            { return vertexCount; }
}