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

package com.skyobservatory.camera;

import com.skyobservatory.math.Matrix4;

public class SkyCamera {

    private float fovDeg = 60f;
    private float aspect = 1f;
    private float near = 0.1f;
    private float far = 300f;

    // Default forward = -Z (into the screen / toward north) in our world space.
    // Default up = +Y (world up).
    private float forwardX = 0, forwardY = 0, forwardZ = -1;
    private float upX = 0, upY = 1, upZ = 0;

    private float yawDeg, pitchDeg;

    public void buildViewFromVectors(float fx, float fy, float fz,
                                     float ux, float uy, float uz) {
        forwardX = fx; forwardY = fy; forwardZ = fz;
        upX = ux; upY = uy; upZ = uz;
    }

    public void applyYawDelta(float degrees) {
        yawDeg += degrees;
        float pitchRad = (float) Math.toRadians(pitchDeg);
        float cp = (float) Math.cos(pitchRad);
        float yawRad = (float) Math.toRadians(yawDeg);
        // Yaw around world Y: forward starts at -Z (north) and rotates east for positive yaw.
        forwardX = (float) ( Math.sin(yawRad) * cp);
        forwardY = (float)   Math.sin(pitchRad);
        forwardZ = (float) (-Math.cos(yawRad) * cp);
        // Reset up -- will be recomputed from cross product in getViewMatrix
        upX = 0; upY = 1; upZ = 0;
    }

    public void applyPitchDelta(float degrees) {
        pitchDeg += degrees;
        if (pitchDeg > 89f) pitchDeg = 89f;
        if (pitchDeg < -89f) pitchDeg = -89f;
        float pitchRad = (float) Math.toRadians(pitchDeg);
        float cp = (float) Math.cos(pitchRad);
        float yawRad = (float) Math.toRadians(yawDeg);
        forwardX = (float) ( Math.sin(yawRad) * cp);
        forwardY = (float)   Math.sin(pitchRad);
        forwardZ = (float) (-Math.cos(yawRad) * cp);
        upX = 0; upY = 1; upZ = 0;
    }

    public void zoomBy(float amount) {
        fovDeg -= amount * 0.1f;
        if (fovDeg < 10f) fovDeg = 10f;
        if (fovDeg > 120f) fovDeg = 120f;
    }

    public Matrix4 getViewMatrix() {
        float fx = forwardX, fy = forwardY, fz = forwardZ;
        float ux = upX, uy = upY, uz = upZ;

        float fl = (float) Math.sqrt(fx*fx + fy*fy + fz*fz);
        if (fl > 1e-10f) { fx /= fl; fy /= fl; fz /= fl; }

        // right = forward x up
        float rx = fy*uz - fz*uy;
        float ry = fz*ux - fx*uz;
        float rz = fx*uy - fy*ux;
        float rl = (float) Math.sqrt(rx*rx + ry*ry + rz*rz);
        if (rl > 1e-10f) { rx /= rl; ry /= rl; rz /= rl; }
        else { rx = 1; ry = 0; rz = 0; }

        // Reorthogonalise up = right x forward
        float uxn = ry*fz - rz*fy;
        float uyn = rz*fx - rx*fz;
        float uzn = rx*fy - ry*fx;

        // Standard right-handed lookAt view matrix (rows = right, up, -forward).
        // Column-major storage for OpenGL (glUniformMatrix4fv with transpose=false).
        Matrix4 m = Matrix4.identity();
        m.set( 0, rx);  m.set( 1, ry);  m.set( 2, rz);  m.set( 3, 0f);
        m.set( 4, uxn); m.set( 5, uyn); m.set( 6, uzn); m.set( 7, 0f);
        m.set( 8, -fx); m.set( 9, -fy); m.set(10, -fz); m.set(11, 0f);
        m.set(12, 0f);  m.set(13, 0f);  m.set(14, 0f);  m.set(15, 1f);
        return m;
    }

    public Matrix4 getProjectionMatrix() {
        return Matrix4.perspective(fovDeg, aspect, near, far);
    }

    public Matrix4 getVpMatrix() {
        return getProjectionMatrix().multiply(getViewMatrix());
    }

    public void setAspect(float aspect) { this.aspect = aspect; }
    public float getAspect() { return aspect; }

    /** Returns the current vertical field of view in degrees. */
    public float getFovDeg() { return fovDeg; }
}
