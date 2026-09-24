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

/**
 * Utility methods for creating direct NIO buffers used by OpenGL.
 */
final class MeshUtils {

    private MeshUtils() {}

    static FloatBuffer toFloatBuffer(float[] d) {
        ByteBuffer bb = ByteBuffer.allocateDirect(d.length * 4).order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(d);
        fb.position(0);
        return fb;
    }

    static ShortBuffer toShortBuffer(short[] d) {
        ByteBuffer bb = ByteBuffer.allocateDirect(d.length * 2).order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(d);
        sb.position(0);
        return sb;
    }
}