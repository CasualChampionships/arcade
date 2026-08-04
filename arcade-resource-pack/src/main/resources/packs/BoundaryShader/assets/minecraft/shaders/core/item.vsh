#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

// == Boundary Start ==
uniform sampler2D Sampler0;
// == Boundary End ==
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;

out vec2 texCoord0;

// == Boundary Start ==
out float isBoundary;
out float height;
out float width;
out vec2 minTexCoord;
out vec2 uv;
out vec2 scale;
out vec3 position;
// == Boundary End ==


// == Boundary Start ==
const vec2 uvCorners[4] = vec2[4](
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
);

const ivec2 cornerProbes[4] = ivec2[4](
    ivec2(0, 0),
    ivec2(0, -1),
    ivec2(-1, -1),
    ivec2(-1, 0)
);

// The corners of a boundary texture's border are keyed with r = 66, g = 70 and
// b = 50 + the index of the quad corner they mark. This is what identifies a
// boundary quad.
// Returns -1 for any sprite that isn't a boundary.
int findBoundaryCorner(sampler2D tex, vec2 coord, out vec4 marker) {
    ivec2 texSize = textureSize(tex, 0);
    ivec2 base = ivec2(floor(coord * vec2(texSize)));

    marker = vec4(0.0);
    for (int i = 0; i < 4; i++) {
        vec4 texel = texelFetch(tex, clamp(base + cornerProbes[i], ivec2(0), texSize - 1), 0);
        ivec3 key = ivec3(round(texel.rgb * 255.0));
        if (key.r == 66 && key.g == 70 && key.b >= 50 && key.b <= 53) {
            marker = texel;
            return key.b - 50;
        }
    }
    return -1;
}

float decode16BitFloat(int bits) {
    int exponent = (bits >> 12) & 0xF;
    int mantissa = bits & 0xFFF;
    float scale = exp2(float(exponent - 1));
    return (1.0 + float(mantissa) / 4096.0) * scale;
}

vec2 unpackDimensions(ivec4 color, ivec2 light) {
    int byte0 = color.z;
    int byte1 = color.y;
    int byte2 = color.x;
    int byte3 = (light.x >> 4 | light.y);
    int low = (byte1 << 8) | byte0;
    int high  = (byte3 << 8) | byte2;
    return vec2(decode16BitFloat(high), decode16BitFloat(low));
}

float unpackDimension(ivec4 color, ivec2 light) {
    int byte0 = color.z;
    int byte1 = color.y;
    int byte2 = color.x;
    int byte3 = (light.x >> 4 | light.y);
    return intBitsToFloat((byte3 << 24) | (byte2 << 16) | (byte1 << 8) | (byte0));
}
// == Boundary End ==

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // == Boundary Start ==
    vec4 marker;
    int corner = findBoundaryCorner(Sampler0, UV0, marker);
    if (corner >= 0) {
        isBoundary = 1.0;

        bool isCube = marker.a < 0.9;

        sphericalVertexDistance = fog_spherical_distance(Position);
        cylindricalVertexDistance = fog_cylindrical_distance(Position);
        texCoord0 = UV0;

        uv = uvCorners[corner];

        vec2 size = textureSize(Sampler0, 0);
        texCoord0 += 8 * (uv * -2 + 1) / size;
        scale = size / 16;
        minTexCoord = texCoord0.xy - uv / scale;

        if (isCube) {
            float dimension = unpackDimension(ivec4(Color * 255), UV2);
            width = dimension;
            height = dimension;
        } else {
            vec2 dimensions = unpackDimensions(ivec4(Color * 255), UV2);
            width = dimensions.x;
            height = dimensions.y;
        }

        position = Position;
        vertexColor = vec4(1.0);
        return;
    } else {
        isBoundary = 0.0;
        width = 0;
        height = 0;
        scale = vec2(0);
        minTexCoord = vec2(0, 0);
        uv = vec2(0, 0);
        position = vec3(0);
    }
    // == Boundary End ==

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    lightMapColor = sample_lightmap(Sampler2, UV2);
    overlayColor = texelFetch(Sampler1, UV1, 0);

    texCoord0 = UV0;
}
