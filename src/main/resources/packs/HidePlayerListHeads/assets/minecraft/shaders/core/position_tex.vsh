#version 150

in vec3 Position;
in vec2 UV0;

uniform sampler2D Sampler0;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;

const vec2 corners[4] = vec2[4](vec2(1.0, 0.0),vec2(0.0, 0.0),vec2(0.0 ,1.0),vec2(1.0, 1.0));

vec4 getVertexColor(sampler2D Sampler, int vertexID, vec2 coords){
    vec2 size = 1.0 / textureSize(Sampler, 0);
    return textureLod(Sampler, coords - (corners[vertexID % 4] - 0.5) * size, -9999);
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vec4 color = getVertexColor(Sampler0, gl_VertexID, texCoord0);

    // TODO: Is this needed?
    if (color.r == 37.0 / 255.0 && color.g == 40.0 / 255.0 && color.b == 30.0 / 255.0) {
        gl_Position = ProjMat * ModelViewMat * vec4(Position + vec3(-5.0, 0.0, 0.0), 1.0);
    }
}