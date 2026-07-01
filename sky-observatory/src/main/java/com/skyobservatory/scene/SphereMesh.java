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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class SphereMesh {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer normalBuffer;
    private final FloatBuffer texCoordBuffer;
    private final ShortBuffer indexBuffer;
    private final int indexCount;
    private final int vertexCount;

    public SphereMesh(float radius, int stacks, int slices) {
        int nv = (stacks + 1) * (slices + 1);
        if (nv > 32767) {
            throw new IllegalArgumentException("SphereMesh vertex count " + nv + " exceeds short index range");
        }
        int ni = stacks * slices * 6;
        this.vertexCount = nv;
        this.indexCount = ni;

        float[] verts = new float[nv * 3];
        float[] norms = new float[nv * 3];
        float[] uvs = new float[nv * 2];
        short[] idx = new short[ni];

        int vi = 0, ti = 0;
        for (int i = 0; i <= stacks; i++) {
            float theta = (float) (i * Math.PI / stacks);
            float st = (float) Math.sin(theta);
            float ct = (float) Math.cos(theta);
            for (int j = 0; j <= slices; j++) {
                float phi = (float) (j * 2.0 * Math.PI / slices);
                float sp = (float) Math.sin(phi);
                float cp = (float) Math.cos(phi);
                float nx = st * cp, ny = ct, nz = st * sp;
                norms[vi] = nx; norms[vi + 1] = ny; norms[vi + 2] = nz;
                verts[vi] = nx * radius; verts[vi + 1] = ny * radius; verts[vi + 2] = nz * radius;
                uvs[ti] = (float) j / slices; uvs[ti + 1] = (float) i / stacks;
                vi += 3; ti += 2;
            }
        }

        int ix = 0;
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int f = i * (slices + 1) + j;
                int s = f + slices + 1;
                idx[ix++] = (short) f;  idx[ix++] = (short) s;  idx[ix++] = (short) (f + 1);
                idx[ix++] = (short) (f + 1); idx[ix++] = (short) s; idx[ix++] = (short) (s + 1);
            }
        }

        vertexBuffer = toFloatBuffer(verts);
        normalBuffer = toFloatBuffer(norms);
        texCoordBuffer = toFloatBuffer(uvs);
        indexBuffer = toShortBuffer(idx);
    }

    public SphereMesh(float[] verts, float[] uvs) {
        this.vertexCount = verts.length / 3;
        this.indexCount = verts.length / 3;
        this.vertexBuffer = toFloatBuffer(verts);
        this.texCoordBuffer = toFloatBuffer(uvs);
        this.normalBuffer = toFloatBuffer(new float[verts.length]);
        short[] idx = new short[vertexCount];
        for (int i = 0; i < vertexCount; i++) idx[i] = (short) i;
        this.indexBuffer = toShortBuffer(idx);
    }

    public FloatBuffer getVertexBuffer() { return vertexBuffer; }
    public FloatBuffer getNormalBuffer() { return normalBuffer; }
    public FloatBuffer getTexCoordBuffer() { return texCoordBuffer; }
    public ShortBuffer getIndexBuffer() { return indexBuffer; }
    public int getIndexCount() { return indexCount; }
    public int getVertexCount() { return vertexCount; }

    private static FloatBuffer toFloatBuffer(float[] d) {
        ByteBuffer bb = ByteBuffer.allocateDirect(d.length * 4).order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(d); fb.position(0);
        return fb;
    }

    private static ShortBuffer toShortBuffer(short[] d) {
        ByteBuffer bb = ByteBuffer.allocateDirect(d.length * 2).order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(d); sb.position(0);
        return sb;
    }
}
