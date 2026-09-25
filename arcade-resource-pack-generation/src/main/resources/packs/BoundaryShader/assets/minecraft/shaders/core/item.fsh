#version 330
#extension GL_ARB_separate_shader_objects : require

// == Boundary Start ==
#include <minecraft:globals.glsl>
// == Boundary End ==
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:oit.glsl>

uniform sampler2D Sampler0;

#ifdef GLINT
uniform sampler2D GlintSampler;
#endif

#ifndef OIT_ALPHA_ONLY
layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
#endif
layout(location = 2) in vec4 vertexColor;
#ifndef OIT_ALPHA_ONLY
layout(location = 3) in vec4 lightMapColor;
layout(location = 4) in vec4 overlayColor;
#endif
layout(location = 5) in vec2 texCoord0;
#ifdef GLINT
layout(location = 6) in vec2 texCoordGlint;
#endif

// == Boundary Start ==
layout(location = 7) in float isBoundary;
layout(location = 8) in float height;
layout(location = 9) in float width;
layout(location = 10) in vec2 minTexCoord;
layout(location = 11) in vec2 uv;
layout(location = 12) in vec2 scale;
layout(location = 13) in vec3 position;
// == Boundary End ==

#ifndef OIT_ALPHA_ONLY
layout(location = 0) out vec4 fragColor;
#endif

#ifndef OIT_ALPHA_ONLY
vec4 calculateFinalColor(vec4 color) {
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    #ifdef GLINT
    vec4 glintColor = GlintAlpha * texture(GlintSampler, texCoordGlint);// Glint color modulator?
    // Matches BlendFuntion.GLINT
    color.rgb += glintColor.rgb * glintColor.rgb;
    #endif

    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    vec4 fogColor = vec4(FogColor.rgb * color.a, FogColor.a);
    #else
    vec4 fogColor = FogColor;
    #endif

    return apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, fogColor);
}
#endif

void main() {
    // == Boundary Start ==
    if (isBoundary > 0.5) {
        vec2 localUV = uv;
        vec2 repeat = vec2(floor(width), floor(height));
        localUV -= (GameTime * 256.0) / repeat;
        vec2 tiledUV = fract(localUV * repeat);
        vec2 atlasUV = minTexCoord + tiledUV / scale;

        vec4 color = texture(Sampler0, atlasUV) * vertexColor;
        if (color.a < 0.1) {
            discard;
        }

        float fogStart = 272; // 16 (blocks per chunk) * 17 (view distance)
        float fogEnd = fogStart + 48;

        bool insideBox = all(lessThanEqual(abs(position), vec3(fogEnd)));
        if (!insideBox) {
            discard;
        }

        float dist = max(abs(position.x), max(abs(position.y), abs(position.z)));
        float alpha = 1.0 - smoothstep(fogStart, fogEnd, dist);
        color = vec4(color.rgb, color.a * alpha);

        #ifdef OIT_ALPHA_ONLY
        executeAlphaOnlyPhase(gl_FragCoord.z, color.a);
        #elif defined(OIT_ACCUMULATE)
        fragColor = sampleColorForAccumulation(color);
        #else
        fragColor = color;
        #endif
        return;
    }
    // == Boundary End ==

    vec4 color = texture(Sampler0, texCoord0);
    #ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    #endif

    color *= vertexColor * ColorModulator;

    #ifdef GLINT
    color.a = max(color.a, GlintAlpha);
    #endif

    #ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, color.a);
    #else
    fragColor = calculateFinalColor(color);
    #endif
}
