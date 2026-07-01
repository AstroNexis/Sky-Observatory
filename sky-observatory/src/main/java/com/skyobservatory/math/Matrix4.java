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

package com.skyobservatory.math;

public final class Matrix4 {

    private final float[] data;

    public Matrix4() {
        this.data = new float[16];
    }

    private Matrix4(float[] data) {
        this.data = data;
    }

    public static Matrix4 identity() {
        Matrix4 m = new Matrix4();
        m.data[0] = 1f; m.data[5] = 1f; m.data[10] = 1f; m.data[15] = 1f;
        return m;
    }

    public static Matrix4 perspective(float fovDeg, float aspect, float near, float far) {
        float f = (float) (1.0 / Math.tan(Math.toRadians(fovDeg / 2.0)));
        Matrix4 m = new Matrix4();
        m.data[0] = f / aspect;
        m.data[5] = f;
        m.data[10] = (far + near) / (near - far);
        m.data[11] = -1f;
        m.data[14] = (2f * far * near) / (near - far);
        m.data[15] = 0f;
        return m;
    }

    public Matrix4 multiply(Matrix4 other) {
        float[] r = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0f;
                for (int k = 0; k < 4; k++) {
                    sum += data[i + k * 4] * other.data[k + j * 4];
                }
                r[i + j * 4] = sum;
            }
        }
        return new Matrix4(r);
    }

    public void set(int index, float value) { data[index] = value; }
    public float get(int index) { return data[index]; }
    public float[] floatArray() { return data; }
}
