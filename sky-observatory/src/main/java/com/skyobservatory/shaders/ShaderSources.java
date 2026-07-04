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

public final class ShaderSources {

    // Textured sphere (unused by scalable path but kept for compatibility)

    public static final String TEXTURED_VERTEX = ""
        + "#version 300 es\n"
        + "uniform mat4 uMvpMatrix;\n"
        + "uniform mat4 uModelMatrix;\n"
        + "layout(location = 0) in vec3 aPosition;\n"
        + "layout(location = 1) in vec3 aNormal;\n"
        + "layout(location = 2) in vec2 aTexCoord;\n"
        + "out vec2 vTexCoord;\n"
        + "out vec3 vNormal;\n"
        + "void main() {\n"
        + "    gl_Position = uMvpMatrix * vec4(aPosition, 1.0);\n"
        + "    vTexCoord = aTexCoord;\n"
        + "    vNormal = normalize(mat3(uModelMatrix) * aNormal);\n"
        + "}\n";

    public static final String TEXTURED_FRAGMENT = ""
        + "#version 300 es\n"
        + "precision mediump float;\n"
        + "uniform sampler2D uTexture;\n"
        + "uniform vec4 uColor;\n"
        + "in vec2 vTexCoord;\n"
        + "in vec3 vNormal;\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "void main() {\n"
        + "    vec4 tex = texture(uTexture, vTexCoord);\n"
        + "    float light = max(dot(vNormal, normalize(vec3(0.5, 0.8, 0.6))), 0.15);\n"
        + "    fragColor = tex * vec4(vec3(light), 1.0) * uColor;\n"
        + "}\n";

    // Sky dome

    public static final String SKY_VERTEX = ""
        + "#version 300 es\n"
        + "uniform mat4 uMvpMatrix;\n"
        + "layout(location = 0) in vec3 aPosition;\n"
        + "layout(location = 2) in vec2 aTexCoord;\n"
        + "out vec2 vTexCoord;\n"
        + "void main() {\n"
        + "    vec4 p = uMvpMatrix * vec4(aPosition, 1.0);\n"
        + "    gl_Position = p.xyww;\n"
        + "    vTexCoord = aTexCoord;\n"
        + "}\n";

    public static final String SKY_FRAGMENT = ""
        + "#version 300 es\n"
        + "precision mediump float;\n"
        + "uniform sampler2D uTexture;\n"
        + "in vec2 vTexCoord;\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "void main() {\n"
        + "    fragColor = texture(uTexture, vTexCoord);\n"
        + "}\n";

    // Cardinal markers, grid lines, horizon ring (colour-tinted, optional tex)

    public static final String CARDINAL_VERTEX = ""
        + "#version 300 es\n"
        + "uniform mat4 uMvpMatrix;\n"
        + "layout(location = 0) in vec3 aPosition;\n"
        + "layout(location = 2) in vec2 aTexCoord;\n"
        + "out vec2 vTexCoord;\n"
        + "void main() {\n"
        + "    gl_Position = uMvpMatrix * vec4(aPosition, 1.0);\n"
        + "    vTexCoord = aTexCoord;\n"
        + "}\n";

    public static final String CARDINAL_FRAGMENT = ""
        + "#version 300 es\n"
        + "precision mediump float;\n"
        + "uniform sampler2D uTexture;\n"
        + "uniform vec4 uColor;\n"
        + "in vec2 vTexCoord;\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "void main() {\n"
        + "    vec4 tex = texture(uTexture, vTexCoord);\n"
        + "    fragColor = tex * uColor;\n"
        + "}\n";

    // Celestial body -- textured sphere with soft diffuse lighting.
    // Shared by every celestial object regardless of type.

    public static final String BODY_VERTEX = ""
        + "#version 300 es\n"
        + "uniform mat4 uMvpMatrix;\n"
        + "layout(location = 0) in vec3 aPosition;\n"
        + "layout(location = 1) in vec3 aNormal;\n"
        + "layout(location = 2) in vec2 aTexCoord;\n"
        + "out vec2 vTexCoord;\n"
        + "out vec3 vNormal;\n"
        + "void main() {\n"
        + "    gl_Position = uMvpMatrix * vec4(aPosition, 1.0);\n"
        + "    vTexCoord = aTexCoord;\n"
        + "    vNormal = aNormal;\n"
        + "}\n";

    public static final String BODY_FRAGMENT = ""
        + "#version 300 es\n"
        + "precision mediump float;\n"
        + "uniform sampler2D uTexture;\n"
        + "in vec2 vTexCoord;\n"
        + "in vec3 vNormal;\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "void main() {\n"
        + "    vec4 tex = texture(uTexture, vTexCoord);\n"
        + "    float light = max(dot(normalize(vNormal), normalize(vec3(0.5, 0.8, 0.6))), 0.20);\n"
        + "    fragColor = tex * light;\n"
        + "}\n";

    // Screen-space label overlay -- rendered as a 2D UI pass after the 3D scene.
    //
    // The vertex shader receives the already-projected NDC position of the
    // parent body (uNdcCenter) together with a screen-space offset in NDC
    // units (uNdcOffsetY) and the quad's half-extents in NDC (uNdcHalfW,
    // uNdcHalfH).  No depth testing -- labels always paint over the sky.
    //
    // aPosition.xy carries the [-1,+1] local quad coordinates:
    //   (-1,-1) bottom-left ... (+1,+1) top-right.

    public static final String LABEL_VERTEX = ""
        + "#version 300 es\n"
        + "uniform vec2  uNdcCenter;\n"   // projected center of parent body (NDC xy)
        + "uniform float uNdcOffsetY;\n"  // downward shift from body center (NDC units, negative)
        + "uniform float uNdcHalfW;\n"    // half-width  of the quad in NDC
        + "uniform float uNdcHalfH;\n"    // half-height of the quad in NDC
        + "layout(location = 0) in vec2 aLocalPos;\n"  // local quad corner in [-1,+1]
        + "layout(location = 2) in vec2 aTexCoord;\n"
        + "out vec2 vTexCoord;\n"
        + "void main() {\n"
        + "    vec2 pos = uNdcCenter\n"
        + "             + vec2(aLocalPos.x * uNdcHalfW,\n"
        + "                    uNdcOffsetY + aLocalPos.y * uNdcHalfH);\n"
        + "    // z = 0, w = 1 -- paints at mid-depth, depth test disabled.\n"
        + "    gl_Position = vec4(pos, 0.0, 1.0);\n"
        + "    vTexCoord = aTexCoord;\n"
        + "}\n";

    public static final String LABEL_FRAGMENT = ""
        + "#version 300 es\n"
        + "precision mediump float;\n"
        + "uniform sampler2D uTexture;\n"
        + "in vec2 vTexCoord;\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "void main() {\n"
        + "    fragColor = texture(uTexture, vTexCoord);\n"
        + "}\n";

    // Horizon glow ring -- deep-ocean blue with additive blend edge softening.
    //
    // The ring lives at Y = 0 in world space (altitude = 0deg, full 360deg circle).
    // To guarantee it is always visible regardless of camera pitch or near-
    // plane distance we force the clip-space depth to the far plane by writing
    // p.xyww (same trick used by the sky dome).  The ring is drawn before
    // depth writes are re-enabled so it never fights with celestial bodies.

    public static final String HORIZON_VERTEX = ""
        + "#version 300 es\n"
        + "uniform mat4 uMvpMatrix;\n"
        + "layout(location = 0) in vec3 aPosition;\n"
        + "void main() {\n"
        + "    vec4 p = uMvpMatrix * vec4(aPosition, 1.0);\n"
        + "    // Force to far-plane depth so the ring is never clipped.\n"
        + "    gl_Position = p.xyww;\n"
        + "}\n";

    public static final String HORIZON_FRAGMENT = ""
        + "#version 300 es\n"
        + "precision mediump float;\n"
        + "uniform vec4 uColor;\n"
        + "layout(location = 0) out vec4 fragColor;\n"
        + "void main() {\n"
        + "    fragColor = uColor;\n"
        + "}\n";
}
