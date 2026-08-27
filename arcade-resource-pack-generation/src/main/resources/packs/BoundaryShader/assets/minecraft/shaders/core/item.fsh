#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
// == Boundary Start ==
#moj_import <minecraft:globals.glsl>
// == Boundary End ==

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

// == Boundary Start ==
in float isBoundary;
in float height;
in float width;
in vec2 minTexCoord;
in vec2 uv;
in vec2 scale;
in vec3 position;
// == Boundary End ==

out vec4 fragColor;

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
        fragColor = vec4(color.rgb, color.a * alpha);
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
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
