#version 330
#extension GL_ARB_separate_shader_objects : require

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl and projection.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

uniform sampler2D Sampler0;

layout(location = 0) out vec2 texCoord0;
layout(location = 1) out vec4 vertexColor;

const ivec2 cornerProbes[4] = ivec2[4](
    ivec2(0, 0),
    ivec2(0, -1),
    ivec2(-1, -1),
    ivec2(-1, 0)
);

bool isHiddenHead(sampler2D tex, vec2 coord) {
    ivec2 texSize = textureSize(tex, 0);
    ivec2 base = ivec2(round(coord * vec2(texSize)));

    for (int i = 0; i < 4; i++) {
        vec4 texel = texelFetch(tex, clamp(base + cornerProbes[i], ivec2(0), texSize - 1), 0);
        ivec4 key = ivec4(round(texel * 255.0));
        if (key == ivec4(37, 40, 30, 255)) {
            return true;
        }
    }
    return false;
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;

    if (isHiddenHead(Sampler0, UV0)) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    }
}
