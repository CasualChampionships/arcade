#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;

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
        vec2 repeat = vec2(width, height);
        localUV -= (GameTime * 256.0) / repeat;
        vec2 tiledUV = fract(localUV * repeat);
        vec2 atlasUV = minTexCoord + tiledUV / scale;

        vec4 color = texture(Sampler0, atlasUV) * vertexColor;
        if (color.a < 0.1) {
            discard;
        }

        float spherical = fog_spherical_distance(position);
        float cylindrical = fog_cylindrical_distance(position);
        if (cylindrical > FogRenderDistanceEnd) {
            discard;
        }
        fragColor = apply_fog(color, spherical, cylindrical, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
        return;
    }
    // == Boundary End ==

    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
