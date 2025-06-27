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
// == Boundary End ==

out vec4 fragColor;

void main() {
    // == Boundary Start ==
    if (isBoundary > 0.5) {
        // Width isn't working correctly at the moment so just assume it's square

        vec2 localUV = uv;
        vec2 size0 = textureSize(Sampler0, 0);
        float ratio = size0.x / size0.y;
        // localUV.x *= ratio;

        vec2 repeat = vec2(height, height);
        vec2 tiledUV = fract(localUV * repeat);
        vec2 atlasUV = minTexCoord + tiledUV / scale;

        vec4 color = texture(Sampler0, atlasUV) * vertexColor;
        if (color.a < 0.1) {
            discard;
        }

        // fragColor = vec4(ratio, 0.0, 0.0, 1.0);
        fragColor = color;
        // fragColor = vec4(minTexCoord.x, minTexCoord.y, 0.0, 1.0);
        return;
    }
    // == Boundary End ==

    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
