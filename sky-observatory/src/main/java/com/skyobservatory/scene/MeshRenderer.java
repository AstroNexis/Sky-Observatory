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

import android.opengl.GLES30;

import com.skyobservatory.math.Matrix4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class MeshRenderer {

    private int vao;
    private int vboPos, vboNorm, vboUv, ebo;
    private int indexCount;
    private int vertexCount;
    private int mode = GLES30.GL_TRIANGLES;
    private boolean initialized;

    public Matrix4 modelMatrix;

    public MeshRenderer() {
        modelMatrix = Matrix4.identity();
    }

    public void upload(SphereMesh mesh) {
        int[] ids = new int[5];
        GLES30.glGenVertexArrays(1, ids, 0);
        GLES30.glGenBuffers(4, ids, 1);
        vao = ids[0];
        vboPos = ids[1];
        vboNorm = ids[2];
        vboUv = ids[3];
        ebo = ids[4];

        GLES30.glBindVertexArray(vao);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboPos);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
                mesh.getVertexCount() * 3 * 4, mesh.getVertexBuffer(), GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboNorm);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
                mesh.getVertexCount() * 3 * 4, mesh.getNormalBuffer(), GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(1);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboUv);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
                mesh.getVertexCount() * 2 * 4, mesh.getTexCoordBuffer(), GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(2);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,
                mesh.getIndexCount() * 2, mesh.getIndexBuffer(), GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);

        indexCount = mesh.getIndexCount();
        initialized = true;
    }

    public void uploadLines(float[] verts) {
        int[] ids = new int[2];
        GLES30.glGenVertexArrays(1, ids, 0);
        GLES30.glGenBuffers(1, ids, 1);
        vao = ids[0];
        vboPos = ids[1];

        FloatBuffer buf = toFloatBuffer(verts);

        GLES30.glBindVertexArray(vao);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboPos);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, verts.length * 4, buf, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);

        vertexCount = verts.length / 3;
        mode = GLES30.GL_LINES;
        initialized = true;
    }

    public void uploadBillboard(SphereMesh mesh, float x, float y, float z) {
        upload(mesh);
        modelMatrix = Matrix4.identity();
        modelMatrix.set(12, x);
        modelMatrix.set(13, y);
        modelMatrix.set(14, z);
    }

    public void uploadBillboard(float[] verts, float[] uvs, float x, float y, float z) {
        int[] ids = new int[3];
        GLES30.glGenVertexArrays(1, ids, 0);
        GLES30.glGenBuffers(2, ids, 1);
        vao = ids[0];
        vboPos = ids[1];
        vboUv = ids[2];

        GLES30.glBindVertexArray(vao);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboPos);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, verts.length * 4, toFloatBuffer(verts), GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboUv);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, uvs.length * 4, toFloatBuffer(uvs), GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(2);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);

        vertexCount = verts.length / 3;
        indexCount = vertexCount;
        mode = GLES30.GL_TRIANGLES;
        initialized = true;

        modelMatrix = Matrix4.identity();
        modelMatrix.set(12, x);
        modelMatrix.set(13, y);
        modelMatrix.set(14, z);
    }

    public void draw() {
        if (!initialized) return;
        GLES30.glBindVertexArray(vao);
        if (mode == GLES30.GL_LINES || ebo == 0) {
            GLES30.glDrawArrays(mode, 0, vertexCount > 0 ? vertexCount : indexCount);
        } else {
            GLES30.glDrawElements(mode, indexCount, GLES30.GL_UNSIGNED_SHORT, 0);
        }
        GLES30.glBindVertexArray(0);
    }

    private static FloatBuffer toFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }
}
