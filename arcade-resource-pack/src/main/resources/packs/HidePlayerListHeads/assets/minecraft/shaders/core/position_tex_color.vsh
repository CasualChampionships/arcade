#version 150

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl and projection.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
    float LineWidth;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform sampler2D Sampler0;

out vec2 texCoord0;
out vec4 vertexColor;

const vec2 corners[4] = vec2[4](vec2(1.0, 0.0),vec2(0.0, 0.0),vec2(0.0 ,1.0),vec2(1.0, 1.0));

vec4 getVertexColor(sampler2D Sampler, int vertexID, vec2 coords){
    vec2 size = 1.0 / textureSize(Sampler, 0);
    return textureLod(Sampler, coords - (corners[vertexID % 4] - 0.5) * size, -9999);
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;

    vec4 color = getVertexColor(Sampler0, gl_VertexID, texCoord0);
    if (color.r == 37.0 / 255.0 && color.g == 40.0 / 255.0 && color.b == 30.0 / 255.0) {
        gl_Position = vec4(2, 2, 2, 1);
    }
}

