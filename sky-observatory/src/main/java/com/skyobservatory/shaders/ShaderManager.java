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

package com.skyobservatory.shaders;

import android.opengl.GLES30;

public final class ShaderManager {

    // Sky dome
    public final int skyProgram;
    public final int skyMvp;
    public final int skyTex;

    // Cardinal / grid / line tinted pass
    public final int cardinalProgram;
    public final int cardMvp;
    public final int cardTex;
    public final int cardColor;

    // Celestial body (textured sphere)
    public final int bodyProgram;
    public final int bodyMvp;
    public final int bodyTex;

    // Camera-facing billboard label
    public final int labelProgram;
    /** NDC xy position of the projected parent body centre. */
    public final int labelNdcCenter;
    /** Downward shift from body centre in NDC units (negative value). */
    public final int labelNdcOffsetY;
    /** Half-width of the label quad in NDC units. */
    public final int labelNdcHalfW;
    /** Half-height of the label quad in NDC units. */
    public final int labelNdcHalfH;
    public final int labelTex;

    // Horizon glow ring
    public final int horizonProgram;
    public final int horizonMvp;
    public final int horizonColor;

    // Kept for API compatibility; delegates to bodyProgram internally
    public final int texturedProgram;
    public final int texturedMvp;
    public final int texturedModel;
    public final int texturedTex;
    public final int texturedColor;

    public ShaderManager() {
        skyProgram     = createProgram(ShaderSources.SKY_VERTEX,      ShaderSources.SKY_FRAGMENT);
        cardinalProgram = createProgram(ShaderSources.CARDINAL_VERTEX, ShaderSources.CARDINAL_FRAGMENT);
        bodyProgram    = createProgram(ShaderSources.BODY_VERTEX,      ShaderSources.BODY_FRAGMENT);
        labelProgram   = createProgram(ShaderSources.LABEL_VERTEX,     ShaderSources.LABEL_FRAGMENT);
        horizonProgram = createProgram(ShaderSources.HORIZON_VERTEX,   ShaderSources.HORIZON_FRAGMENT);
        texturedProgram = createProgram(ShaderSources.TEXTURED_VERTEX, ShaderSources.TEXTURED_FRAGMENT);

        skyMvp  = GLES30.glGetUniformLocation(skyProgram,  "uMvpMatrix");
        skyTex  = GLES30.glGetUniformLocation(skyProgram,  "uTexture");

        cardMvp   = GLES30.glGetUniformLocation(cardinalProgram, "uMvpMatrix");
        cardTex   = GLES30.glGetUniformLocation(cardinalProgram, "uTexture");
        cardColor = GLES30.glGetUniformLocation(cardinalProgram, "uColor");

        bodyMvp = GLES30.glGetUniformLocation(bodyProgram, "uMvpMatrix");
        bodyTex = GLES30.glGetUniformLocation(bodyProgram, "uTexture");

        labelNdcCenter  = GLES30.glGetUniformLocation(labelProgram, "uNdcCenter");
        labelNdcOffsetY = GLES30.glGetUniformLocation(labelProgram, "uNdcOffsetY");
        labelNdcHalfW   = GLES30.glGetUniformLocation(labelProgram, "uNdcHalfW");
        labelNdcHalfH   = GLES30.glGetUniformLocation(labelProgram, "uNdcHalfH");
        labelTex        = GLES30.glGetUniformLocation(labelProgram, "uTexture");

        horizonMvp   = GLES30.glGetUniformLocation(horizonProgram, "uMvpMatrix");
        horizonColor = GLES30.glGetUniformLocation(horizonProgram, "uColor");

        texturedMvp   = GLES30.glGetUniformLocation(texturedProgram, "uMvpMatrix");
        texturedModel = GLES30.glGetUniformLocation(texturedProgram, "uModelMatrix");
        texturedTex   = GLES30.glGetUniformLocation(texturedProgram, "uTexture");
        texturedColor = GLES30.glGetUniformLocation(texturedProgram, "uColor");
    }

    private static int createProgram(String vs, String fs) {
        int vertex = compileShader(GLES30.GL_VERTEX_SHADER, vs);
        int frag   = compileShader(GLES30.GL_FRAGMENT_SHADER, fs);
        int prog   = GLES30.glCreateProgram();
        GLES30.glAttachShader(prog, vertex);
        GLES30.glAttachShader(prog, frag);
        GLES30.glLinkProgram(prog);
        int[] ok = new int[1];
        GLES30.glGetProgramiv(prog, GLES30.GL_LINK_STATUS, ok, 0);
        GLES30.glDeleteShader(vertex);
        GLES30.glDeleteShader(frag);
        if (ok[0] == 0) throw new RuntimeException("Link: " + GLES30.glGetProgramInfoLog(prog));
        return prog;
    }

    private static int compileShader(int type, String src) {
        int s = GLES30.glCreateShader(type);
        GLES30.glShaderSource(s, src);
        GLES30.glCompileShader(s);
        int[] ok = new int[1];
        GLES30.glGetShaderiv(s, GLES30.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == 0) throw new RuntimeException("Compile: " + GLES30.glGetShaderInfoLog(s));
        return s;
    }
}
