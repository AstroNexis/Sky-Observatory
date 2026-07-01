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

package com.skyobservatory.resources;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.skyobservatory.scene.MeshRenderer;
import com.skyobservatory.scene.SphereMesh;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Central GPU resource registry for the renderer.
 *
 * New in this refactor:
 *   - {@link #getOrCreateLabelTexture(String)} generates label textures on demand
 *     and caches them by name. Supports future localisation by keying on the
 *     display string (not the NAIF ID).
 *   - {@link #defaultStarTextureId} provides a fallback for any object whose
 *     texture is not yet in the asset bundle.
 *   - {@link #generateLabelBitmap(String)} is the single place where font
 *     size, colour, and shadow are set, enabling easy global tuning.
 */
public final class SkyResources {

    public int sunTextureId;
    public int moonTextureId;
    public int skyTextureId;
    public int defaultStarTextureId;
    public int[] cardinalTextureIds = new int[4];
    public MeshRenderer skySphereMesh;

    public static final String[] CARDINAL_LABELS = {"N", "E", "S", "W"};

    private AssetManager assets;
    private final Map<String, Integer> labelTextureCache = new HashMap<>();
    /** Aspect ratio (width / height) for each cached label texture. */
    private final Map<String, Float> labelAspectCache   = new HashMap<>();

    private static final float LABEL_TEXT_SIZE = 52f;

    /** Horizontal padding added on each side of the measured text width (px). */
    private static final int LABEL_PAD_X = 12;
    /** Vertical padding above and below the text (px). */
    private static final int LABEL_PAD_Y = 10;

    public void setAssetManager(AssetManager am) { this.assets = am; }

    public void generateAll() {
        defaultStarTextureId  = generateStarTexture();
        sunTextureId          = loadTextureFromAssets("sun.jpg");
        moonTextureId         = loadTextureFromAssets("moon.jpg");
        skyTextureId          = generateSkyGradient();

        for (int i = 0; i < 4; i++) {
            cardinalTextureIds[i] = generateLetterTexture(CARDINAL_LABELS[i]);
        }

        SphereMesh sky = new SphereMesh(200f, 24, 32);
        skySphereMesh = new MeshRenderer();
        skySphereMesh.upload(sky);
    }

    /**
     * Returns the GPU texture ID for a label string, generating and caching
     * it on first access.  Thread-safe only when called from the GL thread.
     *
     * @param displayName the localised or raw object name to render as a label
     * @return GL texture ID (non-zero)
     */
    public int getOrCreateLabelTexture(String displayName) {
        Integer cached = labelTextureCache.get(displayName);
        if (cached != null) return cached;
        Bitmap bmp = generateLabelBitmap(displayName);
        float aspect = (float) bmp.getWidth() / (float) bmp.getHeight();
        int id = upload(bmp);
        labelTextureCache.put(displayName, id);
        labelAspectCache.put(displayName, aspect);
        return id;
    }

    /**
     * Returns the width-to-height aspect ratio of the label texture for
     * {@code displayName}.  Must be called after {@link #getOrCreateLabelTexture}.
     *
     * @param displayName the label string
     * @return aspect ratio >= 1.0; defaults to 1.0 if not yet generated
     */
    public float getLabelAspect(String displayName) {
        Float v = labelAspectCache.get(displayName);
        return v != null ? v : 1f;
    }

    // Private helpers

    private int loadTextureFromAssets(String fileName) {
        if (assets == null) return defaultStarTextureId;
        try {
            InputStream is  = assets.open(fileName);
            Bitmap bmp      = BitmapFactory.decodeStream(is);
            is.close();
            return bmp != null ? upload(bmp) : defaultStarTextureId;
        } catch (IOException e) {
            return 0;
        }
    }

    private int generateSkyGradient() {
        int w = 1, h = 256;
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            float t  = (float) y / h;
            int ri   = (int)(3f  + 8f  * t);
            int gi   = (int)(3f  + 10f * t);
            int bi   = (int)(18f + 22f * t);
            pixels[y * w] = (255 << 24) | (ri << 16) | (gi << 8) | bi;
        }
        return upload(Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888));
    }

    /** Soft circular glow used for generic stars. */
    private int generateStarTexture() {
        int size = 32;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c   = new Canvas(bmp);
        Paint p    = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx   = size / 2f;
        for (int r = size / 2; r > 0; r--) {
            float alpha = (float)(size / 2 - r) / (size / 2);
            p.setARGB((int)(alpha * 255), 220, 220, 255);
            c.drawCircle(cx, cx, r, p);
        }
        return upload(bmp);
    }

    private int generateLetterTexture(String letter) {
        int size = 64;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c   = new Canvas(bmp);
        Paint p    = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFFFFFFF);
        p.setTextSize(size * 0.7f);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(letter, size / 2f, size * 0.75f, p);
        return upload(bmp);
    }

    /**
     * Renders a small white label on a transparent background.
     * A subtle drop-shadow improves legibility against both bright and dark
     * regions of the sky.
     *
     * The bitmap dimensions are computed from the actual measured text width
     * so that no character is ever clipped -- a fixed-size canvas would truncate
     * long names (e.g. "Moon" lost its trailing "n" at 128 px wide).
     */
    private Bitmap generateLabelBitmap(String name) {
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(0xFFE8E8FF);
        text.setTextSize(LABEL_TEXT_SIZE);
        text.setTextAlign(Paint.Align.LEFT);

        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setColor(0xAA000000);
        shadow.setTextSize(LABEL_TEXT_SIZE);
        shadow.setTextAlign(Paint.Align.LEFT);

        // Measure the exact text width so the bitmap is never too narrow.
        float textWidth  = text.measureText(name);
        Paint.FontMetrics fm = text.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;

        int bmpW = (int)(textWidth  + LABEL_PAD_X * 2 + 2f); // +2 for shadow offset
        int bmpH = (int)(textHeight + LABEL_PAD_Y * 2 + 2f);

        Bitmap bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        Canvas c   = new Canvas(bmp);

        // Baseline: ascent is negative, so baseline = -ascent + top padding.
        float baseline = -fm.ascent + LABEL_PAD_Y;

        c.drawText(name, LABEL_PAD_X + 1f, baseline + 1f, shadow);
        c.drawText(name, LABEL_PAD_X,      baseline,      text);
        return bmp;
    }

    private static int upload(Bitmap bmp) {
        if (bmp == null) return 0;
        int[] id = new int[1];
        GLES30.glGenTextures(1, id, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();
        return id[0];
    }
}
