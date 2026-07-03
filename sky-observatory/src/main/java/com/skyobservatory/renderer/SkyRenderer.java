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

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.skyobservatory.api.ObservableObject;
import com.skyobservatory.api.SkySnapshot;
import com.skyobservatory.camera.SensorController;
import com.skyobservatory.camera.SkyCamera;
import com.skyobservatory.touch.TouchController;
import com.skyobservatory.math.Matrix4;
import com.skyobservatory.resources.SkyResources;
import com.skyobservatory.scene.MeshRenderer;
import com.skyobservatory.shaders.ShaderManager;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Data-driven OpenGL renderer for the celestial sky.
// Architecture contract
// ─────────────────────
// Input:  {@link SkySnapshot}  (from engine, via {@link #updateSnapshot})
// {@link com.skyobservatory.api.CameraOrientation} (from sensors)
// Output: GPU draw calls -- nothing else.
// The renderer does NOT:
// * know about Sun, Moon, or any specific object type
// * compute or override astronomy data
// * access SuperNOVAS or the engine directly
// Object pipeline
// ───────────────
// updateSnapshot(SkySnapshot)
// -> rebuild entry list via CelestialObjectFactory   O(n)
// onDrawFrame()
// -> iterate entries, place each body, draw           O(n)
// All per-type branching lives in {@link CelestialObjectFactory}.
// Batching strategy (GPU instancing -- future)
// ───────────────────────────────────────────
// Each ObjectCategory maps to a single SphereMesh prototype in the factory.
// When upgrading to GL_DRAW_ARRAYS_INSTANCED, replace the per-object loop
// with a single instanced draw call per category, uploading a per-instance
// transform buffer (SSBO or instance VBO).  The entry list already groups
// data by prototype, so the grouping step is O(n) without structural change.
public class SkyRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "SkyRenderer";

    /** World-space scale: radius of the virtual celestial sphere in metres. */
    private static final float WORLD_SCALE = 10f;

    /** Half-height of a label quad in NDC units (viewport-independent). */
    private static final float LABEL_NDC_HALF_H = 0.035f;

    /**
     * NDC y-shift applied from the body centre to the label centre.
     * Negative = downward on screen.  Tuned so the label sits just below
     * the body disc without overlapping for the largest bodies (Sun/Moon).
     */
    private static final float LABEL_NDC_OFFSET_Y = -0.065f;

    private final Context context;
    private final SkyCamera camera = new SkyCamera();
    private final TouchController touchController = new TouchController();
    private SensorController sensorController;

    private ShaderManager shaders;
    private SkyResources resources;
    private CelestialObjectFactory objectFactory;

    // Scalable object list -- populated once per snapshot, iterated per frame.
    private final List<ObservableObjectEntry> objectEntries = new ArrayList<>();

    // Grid and horizon
    private List<CelestialGrid.GridLine> gridLines = new ArrayList<>();
    private MeshRenderer horizonRing;
    private MeshRenderer[] cardinalMarkers = new MeshRenderer[4];

    // Viewport dimensions -- updated in onSurfaceChanged, used by the 2D label pass.
    private int viewportW = 1, viewportH = 1;

    // Last snapshot; survives EGL context loss so positions are restored on resume.
    private volatile SkySnapshot pendingSnapshot;

    private boolean ready;

    public SkyRenderer(Context context) {
        this.context = context;
    }

    public TouchController getTouchController() { return touchController; }
    public void setSensorController(SensorController sc) { this.sensorController = sc; }

    // Public data interface

    // Replaces the current celestial object list with the contents of
// {@code snapshot}.  Safe to call from any thread; the GL thread will
// rebuild geometry on the next frame.
// This is the only method the activity needs to call.  No per-object
// special-casing is required.
// @param snapshot the latest sky snapshot from the engine
    public void updateSnapshot(SkySnapshot snapshot) {
        pendingSnapshot = snapshot;
    }

    // GLSurfaceView.Renderer

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            ready = false;
            objectEntries.clear();
            GLES30.glClearColor(0.01f, 0.01f, 0.04f, 1f);
            GLES30.glDepthMask(true);
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
            GLES30.glDisable(GLES30.GL_CULL_FACE);

            shaders       = new ShaderManager();
            resources     = new SkyResources();
            resources.setAssetManager(context.getAssets());
            resources.generateAll();
            objectFactory = new CelestialObjectFactory(resources);

            buildHorizonRing();
            buildCardinalMarkers();

            CelestialGrid grid = new CelestialGrid();
            gridLines = grid.build();

            // Wire the camera's live FOV so TouchController can scale drag sensitivity
            // proportionally (narrower FOV = zoomed in = smaller angular step per pixel).
            // This matches MapMover.onDrag: pixelsToRadians = fieldOfView / (height * r2d).
            touchController.setFovDegSupplier(camera::getFovDeg);

            // Restore last known snapshot after context loss / resume.
            SkySnapshot snap = pendingSnapshot;
            if (snap != null) {
                applySnapshot(snap);
            }

            ready = true;
        } catch (Exception e) {
            Log.e(TAG, "init failed", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        GLES30.glViewport(0, 0, w, h);
        camera.setAspect((float) w / (float) h);
        viewportW = w;
        viewportH = h;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!ready) return;
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        SkySnapshot snap = pendingSnapshot;
        if (snap != null) {
            applySnapshot(snap);
            pendingSnapshot = null;
        }

        updateCamera();

        Matrix4 vp   = camera.getVpMatrix();

        drawSkyBackground(vp);
        drawGrid(vp);
        drawCardinals(vp);
        drawCelestialObjects(vp);
        drawHorizon(vp);
    }

    // Snapshot application -- O(n)

    /**
     * Rebuilds the entry list from a new snapshot.
     * Must be called on the GL thread (from queueEvent or onDrawFrame).
     */
    private void applySnapshot(SkySnapshot snapshot) {
        objectEntries.clear();
        for (ObservableObject obj : snapshot.getObjects()) {
            ObservableObjectEntry entry = objectFactory.build(obj);
            placeEntry(entry, obj);
            objectEntries.add(entry);
        }
    }

    // Computes the world-space position for {@code obj} and writes it into
// the entry's body and label model matrices.
// ENU world space: X = east, Y = up, Z = south.
// Azimuth 0 = north, 90 = east.
// x =  cos(alt) * sin(az)
// y =  sin(alt)
// z = -cos(alt) * cos(az)
    private void placeEntry(ObservableObjectEntry entry, ObservableObject obj) {
        double azRad  = Math.toRadians(obj.getAzimuthDegrees());
        double altRad = Math.toRadians(obj.getAltitudeDegrees());
        double cosAlt = Math.cos(altRad);

        float wx = (float)( cosAlt * Math.sin(azRad)) * WORLD_SCALE;
        float wy = (float)( Math.sin(altRad))         * WORLD_SCALE;
        float wz = (float)(-cosAlt * Math.cos(azRad)) * WORLD_SCALE;

        entry.bodyMesh.modelMatrix = Matrix4.identity();
        entry.bodyMesh.modelMatrix.set(12, wx);
        entry.bodyMesh.modelMatrix.set(13, wy);
        entry.bodyMesh.modelMatrix.set(14, wz);

        // Label anchor stored as billboard world position; shader applies view offset.
        entry.labelMesh.modelMatrix = Matrix4.identity();
        entry.labelMesh.modelMatrix.set(12, wx);
        entry.labelMesh.modelMatrix.set(13, wy);
        entry.labelMesh.modelMatrix.set(14, wz);
    }

    // Draw passes

    private void drawSkyBackground(Matrix4 vp) {
        GLES30.glDepthMask(false);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glUseProgram(shaders.skyProgram);
        GLES30.glUniformMatrix4fv(shaders.skyMvp, 1, false, vp.floatArray(), 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, resources.skyTextureId);
        GLES30.glUniform1i(shaders.skyTex, 0);
        resources.skySphereMesh.draw();
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthMask(true);
    }

    // Full celestial sphere grid.
// Depth writing is disabled for the grid pass -- grid lines are cosmetic
// and must never occlude celestial bodies drawn in the same frame.
// Depth testing is kept enabled so the grid respects the sky-dome depth
// and does not bleed through geometry behind the observer.
// Color coding:
// Equator        -- brighter cyan tint
// Prime meridian -- brighter green tint
// Declination    -- standard dim blue-grey
// RA lines       -- slightly dimmer
    private void drawGrid(Matrix4 vp) {
        GLES30.glDepthMask(false);
        GLES30.glUseProgram(shaders.cardinalProgram);
        GLES30.glUniformMatrix4fv(shaders.cardMvp, 1, false, vp.floatArray(), 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUniform1i(shaders.cardTex, 0);

        for (CelestialGrid.GridLine line : gridLines) {
            switch (line.type) {
                case EQUATOR:
                    GLES30.glUniform4f(shaders.cardColor, 0.10f, 0.35f, 0.45f, 1f);
                    break;
                case PRIME_MERIDIAN:
                    GLES30.glUniform4f(shaders.cardColor, 0.10f, 0.40f, 0.20f, 1f);
                    break;
                case DECLINATION:
                    GLES30.glUniform4f(shaders.cardColor, 0.12f, 0.14f, 0.22f, 1f);
                    break;
                case RIGHT_ASCENSION:
                default:
                    GLES30.glUniform4f(shaders.cardColor, 0.10f, 0.10f, 0.18f, 1f);
                    break;
            }
            line.renderer.draw();
        }
        GLES30.glDepthMask(true);
    }

    // Deep-ocean blue horizon ring with a slight glow effect achieved by
    // drawing two concentric rings at different Y offsets and blending.
    //
    // The horizon shader pins every vertex to the far clip plane
    // (gl_Position = p.xyww), the same trick used for the sky dome. That
    // means depth testing -- not draw order -- is what should decide
    // whether the ring paints over a pixel: real geometry (bodies,
    // cardinal markers, anything with a genuine depth < 1.0) always wins
    // the GL_LESS test, so the ring only shows through where nothing
    // closer has been drawn yet. Depth *writes* stay off so the ring
    // itself never blocks anything drawn after it.
    //
    // This pass runs last, after all real 3D geometry, so the depth
    // buffer already holds every object's true depth by the time the
    // ring is tested against it -- celestial bodies correctly overlap
    // the horizon line rather than the other way around.
    // The horizon shader forces vertices to the far clip plane (gl_Position = p.xyww,
    // depth = 1.0).  The sky dome uses the same trick but draws with depth *writes* off,
    // leaving the depth buffer at its cleared value of 1.0 everywhere the sky shows.
    // GL_LESS (1.0 < 1.0) therefore always fails for horizon pixels that lie over open
    // sky, making the ring invisible.  Disabling the depth test for this pass lets the
    // ring always composite over the sky background.  Depth writes stay off so the ring
    // never occludes 3D objects (celestial bodies, cardinal markers) that have already
    // written a depth < 1.0 into the buffer earlier in the frame.
    private void drawHorizon(Matrix4 vp) {
        GLES30.glDepthMask(false);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);   // sky dome + horizon both live at depth 1.0
        GLES30.glUseProgram(shaders.horizonProgram);
        GLES30.glUniformMatrix4fv(shaders.horizonMvp, 1, false, vp.floatArray(), 0);
        // Primary ring -- solid deep-ocean blue
        GLES30.glUniform4f(shaders.horizonColor, 0.05f, 0.28f, 0.60f, 1.0f);
        horizonRing.draw();
        // Glow layer -- wider, translucent
        GLES30.glUniform4f(shaders.horizonColor, 0.08f, 0.35f, 0.75f, 0.35f);
        horizonRing.draw();
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthMask(true);
    }

    private void drawCardinals(Matrix4 vp) {
        GLES30.glUseProgram(shaders.cardinalProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glUniform4f(shaders.cardColor, 1f, 1f, 1f, 1f);
        for (int i = 0; i < 4; i++) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, resources.cardinalTextureIds[i]);
            GLES30.glUniform1i(shaders.cardTex, 0);
            Matrix4 mvp = vp.multiply(cardinalMarkers[i].modelMatrix);
            GLES30.glUniformMatrix4fv(shaders.cardMvp, 1, false, mvp.floatArray(), 0);
            cardinalMarkers[i].draw();
        }
    }

    // Generic object draw loop -- O(n), no per-type branching.
// Pass 1: draw all body spheres (3D, depth-tested).
// Pass 2: draw all labels as 2D screen-space overlay after the 3D pass.
// Labels are never occluded, never clipped by projection, and
// are always correctly centered regardless of name length.
    private void drawCelestialObjects(Matrix4 vp) {
        // Pass 1 -- body spheres
        GLES30.glUseProgram(shaders.bodyProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glUniform1i(shaders.bodyTex, 0);

        for (ObservableObjectEntry entry : objectEntries) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, entry.textureId);
            Matrix4 mvp = vp.multiply(entry.bodyMesh.modelMatrix);
            GLES30.glUniformMatrix4fv(shaders.bodyMvp, 1, false, mvp.floatArray(), 0);
            entry.bodyMesh.draw();
        }

        // Pass 2 -- 2D screen-space label overlay
        //
        // Depth writes and testing are disabled so labels always paint over
        // the scene.  Each label's NDC centre is computed by projecting the
        // body's world position through the VP matrix; only objects that are
        // in front of the camera (positive clip-space W) receive a label.
        GLES30.glDepthMask(false);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glUseProgram(shaders.labelProgram);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glUniform1i(shaders.labelTex, 0);

        // Aspect ratio correction: NDC x spans [-1,+1] across the screen width,
        // NDC y spans [-1,+1] across the screen height.  To make the label quad
        // appear at the correct pixel aspect ratio we scale the half-width by
        // (height / width), i.e. the inverse screen aspect.
        float invAspect = (viewportW > 0) ? (float) viewportH / (float) viewportW : 1f;

        for (ObservableObjectEntry entry : objectEntries) {
            float wx = entry.bodyMesh.modelMatrix.get(12);
            float wy = entry.bodyMesh.modelMatrix.get(13);
            float wz = entry.bodyMesh.modelMatrix.get(14);

            // Project body centre through VP to clip space.
            float[] vpa = vp.floatArray();
            // Column-major: clip = VP * worldPos
            float cx = vpa[0]*wx + vpa[4]*wy + vpa[8]*wz  + vpa[12];
            float cy = vpa[1]*wx + vpa[5]*wy + vpa[9]*wz  + vpa[13];
            float cw = vpa[3]*wx + vpa[7]*wy + vpa[11]*wz + vpa[15];

            // Skip objects behind the camera or exactly at the camera.
            if (cw <= 0f) continue;

            float ndcX = cx / cw;
            float ndcY = cy / cw;

            // Skip objects whose projected centre is well outside the screen.
            if (ndcX < -1.5f || ndcX > 1.5f || ndcY < -1.5f || ndcY > 1.5f) continue;

            // Label half-extents: height is fixed in NDC; width is scaled by
            // the label's bitmap aspect ratio and then corrected for screen aspect.
            float halfH = LABEL_NDC_HALF_H;
            float halfW = halfH * entry.labelAspect * invAspect;

            GLES30.glUniform2f(shaders.labelNdcCenter,  ndcX, ndcY);
            GLES30.glUniform1f(shaders.labelNdcOffsetY, LABEL_NDC_OFFSET_Y);
            GLES30.glUniform1f(shaders.labelNdcHalfW,   halfW);
            GLES30.glUniform1f(shaders.labelNdcHalfH,   halfH);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, entry.labelTextureId);
            entry.labelMesh.draw();
        }

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthMask(true);
    }

    // Camera

    private void updateCamera() {
        camera.zoomBy(touchController.consumePinch());
        if (sensorController != null && sensorController.hasOrientation()) {
            camera.buildViewFromVectors(
                    sensorController.getForwardX(), sensorController.getForwardY(), sensorController.getForwardZ(),
                    sensorController.getUpX(),      sensorController.getUpY(),      sensorController.getUpZ());
        } else {
            camera.applyYawDelta(touchController.consumeDeltaX());
            camera.applyPitchDelta(-touchController.consumeDeltaY());
        }
    }

    // Scene construction

    /** Deep-ocean blue horizon ring at Y = 0 (world-space ground level). */
    private void buildHorizonRing() {
        int seg = 96;   // high segment count for smooth glow
        float r = WORLD_SCALE;
        float[] verts = new float[seg * 2 * 3];
        int idx = 0;
        for (int i = 0; i < seg; i++) {
            double a1 = i       * 2.0 * Math.PI / seg;
            double a2 = (i + 1) * 2.0 * Math.PI / seg;
            verts[idx++] = r * (float) Math.sin(a1); verts[idx++] = 0f; verts[idx++] = r * (float) Math.cos(a1);
            verts[idx++] = r * (float) Math.sin(a2); verts[idx++] = 0f; verts[idx++] = r * (float) Math.cos(a2);
        }
        horizonRing = new MeshRenderer();
        horizonRing.uploadLines(verts);
    }

    private void buildCardinalMarkers() {
        float r = WORLD_SCALE;
        float[][] worldPos = {
            {0f, 1.5f, -r},    // N
            {r,  1.5f, 0f},    // E
            {0f, 1.5f,  r},    // S
            {-r, 1.5f, 0f},    // W
        };
        float hs = 1.2f;
        float[] verts = {
            -hs, -hs, 0f,  hs, -hs, 0f,  hs,  hs, 0f,
            -hs, -hs, 0f,  hs,  hs, 0f, -hs,  hs, 0f,
        };
        float[] uvs = {
            0f, 0f,  1f, 0f,  1f, 1f,
            0f, 0f,  1f, 1f,  0f, 1f,
        };
        for (int i = 0; i < 4; i++) {
            cardinalMarkers[i] = new MeshRenderer();
            cardinalMarkers[i].uploadBillboard(verts, uvs,
                    worldPos[i][0], worldPos[i][1], worldPos[i][2]);
        }
    }
}
