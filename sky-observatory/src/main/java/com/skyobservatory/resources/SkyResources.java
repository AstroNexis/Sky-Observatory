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

import com.skyobservatory.api.CelestialObject;
import com.skyobservatory.api.PlanetData;
import com.skyobservatory.scene.MeshRenderer;
import com.skyobservatory.scene.SphereMesh;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Central GPU resource registry for the renderer.
 *
 * Object textures are loaded on demand from the asset name stored in
 * {@link CelestialObject#getAssetName()}. To give a body its own texture,
 * drop the file into {@code assets/} and set the name in the catalog.
 * No code change is needed here.
 */
public final class SkyResources {

    public int skyTextureId;
    public int defaultStarTextureId;
    public int[] cardinalTextureIds = new int[4];
    public MeshRenderer skySphereMesh;

    public static final String[] CARDINAL_LABELS = {"N", "E", "S", "W"};

    private AssetManager assets;
    private final Map<Integer, Integer>  objectTextureCache = new HashMap<>();
    private final Map<String,  Integer>  labelTextureCache  = new HashMap<>();
    private final Map<String,  Float>    labelAspectCache   = new HashMap<>();
    private Integer saturnRingTextureId;

    private static final float LABEL_TEXT_SIZE = 52f;
    private static final int   LABEL_PAD_X     = 12;
    private static final int   LABEL_PAD_Y     = 10;

    public void setAssetManager(AssetManager am) { this.assets = am; }

    public void generateAll() {
        defaultStarTextureId = generateStarTexture();
        skyTextureId         = generateSkyGradient();

        for (int i = 0; i < 4; i++) {
            cardinalTextureIds[i] = generateLetterTexture(CARDINAL_LABELS[i]);
        }

        SphereMesh sky = new SphereMesh(200f, 24, 32);
        skySphereMesh = new MeshRenderer();
        skySphereMesh.upload(sky);
    }

    /**
     * Returns the GL texture ID for {@code def}, loading from assets on first
     * access and caching by NAIF ID.
     *
     * Falls back to {@link #defaultStarTextureId} if the body has no asset
     * or the asset cannot be loaded.
     */
    public int getOrCreateObjectTexture(CelestialObject def) {
        Integer cached = objectTextureCache.get(def.getNaifId());
        if (cached != null) return cached;

        int id = defaultStarTextureId;
        if (def.getAssetName() != null) {
            int loaded = loadTextureFromAssets(def.getAssetName());
            if (loaded != 0) id = loaded;
        }

        objectTextureCache.put(def.getNaifId(), id);
        return id;
    }

    /**
     * Returns the GL texture ID for a label string, generating and caching
     * it on first access. Thread-safe only when called from the GL thread.
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

    public float getLabelAspect(String displayName) {
        Float v = labelAspectCache.get(displayName);
        return v != null ? v : 1f;
    }

    /**
     * Returns the GL texture ID for Saturn's ring, loading on first access.
     * Returns 0 if the asset is missing or cannot be loaded.
     */
    public int getOrCreateSaturnRingTexture() {
        if (saturnRingTextureId != null) return saturnRingTextureId;
        int id = loadTextureFromAssets(PlanetData.SATURN_RING_TEXTURE);
        saturnRingTextureId = id;
        return id;
    }

    // Private helpers

    private int loadTextureFromAssets(String fileName) {
        if (assets == null) return 0;
        try {
            InputStream is = assets.open(fileName);
            Bitmap bmp     = BitmapFactory.decodeStream(is);
            is.close();
            return bmp != null ? upload(bmp) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    private int generateSkyGradient() {
        int w = 1, h = 256;
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            float t = (float) y / h;
            int ri  = (int)(3f  + 8f  * t);
            int gi  = (int)(3f  + 10f * t);
            int bi  = (int)(18f + 22f * t);
            pixels[y * w] = (255 << 24) | (ri << 16) | (gi << 8) | bi;
        }
        return upload(Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888));
    }

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

    private Bitmap generateLabelBitmap(String name) {
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(0xFFE8E8FF);
        text.setTextSize(LABEL_TEXT_SIZE);
        text.setTextAlign(Paint.Align.LEFT);

        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setColor(0xAA000000);
        shadow.setTextSize(LABEL_TEXT_SIZE);
        shadow.setTextAlign(Paint.Align.LEFT);

        float textWidth      = text.measureText(name);
        Paint.FontMetrics fm = text.getFontMetrics();
        float textHeight     = fm.descent - fm.ascent;

        int bmpW = (int)(textWidth  + LABEL_PAD_X * 2 + 2f);
        int bmpH = (int)(textHeight + LABEL_PAD_Y * 2 + 2f);

        Bitmap bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        Canvas c   = new Canvas(bmp);

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
