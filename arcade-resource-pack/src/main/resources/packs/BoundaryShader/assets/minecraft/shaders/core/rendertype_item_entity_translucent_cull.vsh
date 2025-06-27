#version 150

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec2 texCoord2;

// == Boundary Start ==
out float isBoundary;
out float height;
out float width;
out vec2 minTexCoord;
out vec2 uv;
out vec2 scale;
// == Boundary End ==

// == Boundary Start ==
const vec2 quadCorners[4] = vec2[4](
    vec2(0.0, 0.0), 
    vec2(1.0, 0.0), 
    vec2(1.0, 1.0), 
    vec2(0.0, 1.0)  
);
const vec2 uvCorners[4] = vec2[4](
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
);

vec2 getVertexCornerUV(sampler2D tex, vec2 uv, int vertexID) {
    vec2 texSize = vec2(textureSize(tex, 0));
    vec2 texelSize = 1.0 / texSize;

    vec2 texelUV = floor(uv * texSize) / texSize;
    int cornerIndex = ((vertexID % 4) & 1) == 0 ? (vertexID + 2) % 4 : vertexID % 4;
    texelUV += (quadCorners[cornerIndex] - vec2(1)) * texelSize;
    return texelUV;
}

uvec2 unpackShorts(ivec4 data) {
    uint low = uint(data.x) | (uint(data.y) << 8);
    uint high = uint(data.z)  | (uint(data.w) << 8);
    return uvec2(high, low);
}
// == Boundary End ==

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // == Boundary Start ==
    vec2 cornerUV = getVertexCornerUV(Sampler0, UV0, gl_VertexID);
    vec4 color = texture(Sampler0, cornerUV);
    if (color.r == 66.0 / 255.0 && color.g == 70.0 / 255.0 && color.b == 50.0 / 255.0) {
        isBoundary = 1.0;

        sphericalVertexDistance = fog_spherical_distance(Position);
        cylindricalVertexDistance = fog_cylindrical_distance(Position);
        texCoord0 = UV0;
        texCoord1 = UV1;
        texCoord2 = UV2;    

        int cornerIndex = gl_VertexID % 4;
        uv = uvCorners[cornerIndex];

        vec2 size = textureSize(Sampler0, 0);
        scale = vec2(32, 32 * (size.y / size.x));
        minTexCoord = texCoord0.xy - uv / scale;

        uvec2 dimensions = unpackShorts(ivec4(Color * 255));
        width = dimensions.x;
        height = dimensions.y;

        vertexColor = vec4(1.0);
        return;
    } else {
        isBoundary = 0.0;
        width = 0;
        height = 0;
        minTexCoord = vec2(0, 0);
        uv = vec2(0, 0);
    }
    // == Boundary End ==

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color) * texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0 = UV0;
    texCoord1 = UV1;
    texCoord2 = UV2;
}
