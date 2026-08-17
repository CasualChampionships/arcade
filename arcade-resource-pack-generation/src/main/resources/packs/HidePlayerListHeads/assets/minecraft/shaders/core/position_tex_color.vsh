#version 330

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl and projection.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
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

vec4 getVertexColor(sampler2D Sampler) {
    ivec2 size = textureSize(Sampler, 0);
    int maxLevel = int(floor(log2(float(max(size.x, size.y)))));
    return texelFetch(Sampler, ivec2(0), maxLevel);
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;

    vec4 color = getVertexColor(Sampler0);
    vec3 key = vec3(37.0, 40.0, 30.0) / 255.0;
    if (all(lessThan(abs(color.rgb - key), vec3(0.5 / 255.0)))) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    }
}

