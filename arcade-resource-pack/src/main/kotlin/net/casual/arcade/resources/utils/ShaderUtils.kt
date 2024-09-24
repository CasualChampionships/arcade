package net.casual.arcade.resources.utils

import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.util.FastColor

public object ShaderUtils {
    private const val NL_INDENT = "\n            "

    internal fun getOutlineJsonShader(): String {
        return """
        {
            "vertex": "rendertype_outline",
            "fragment": "rendertype_outline",
            "samplers": [
                { "name": "Sampler0" }
            ],
            "uniforms": [
                { "name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
                { "name": "ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
                { "name": "GameTime", "type": "float", "count": 1, "values": [ 0.0 ] },
                { "name": "ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] }
            ]
        }
        """.trimIndent()
    }
    
    internal fun getOutlineVertexShader(
        colors: Int2IntMap,
        rainbow: Int?
    ): String {
        var first = true
        val builder = StringBuilder()
        for (entry in colors.int2IntEntrySet()) {
            this.appendConditional(builder, !first, entry.intKey) {
                val (r, g, b) = this.intToFloatColor(entry.intValue)
                builder.append("    glow = vec3($r, $g, $b); // #${entry.intValue.toString(16)} $NL_INDENT")
            }
            first = false
        }
        if (rainbow != null) {
            this.appendConditional(builder, !first, rainbow) {
                builder.append("    float animation = GameTime * 1000.0;$NL_INDENT")
                builder.append("    vec3 offset = vec3(0.0, -0.33333, 0.33333);$NL_INDENT")
                builder.append("    glow = 0.5 * cos(6.283 * (animation + offset)) + 0.5;$NL_INDENT")
            }
        }

        return """
        #version 150

        in vec3 Position;
        in vec4 Color;
        in vec2 UV0;

        uniform mat4 ModelViewMat;
        uniform mat4 ProjMat;
        ${if (rainbow != null) "uniform float GameTime;" else ""}

        out vec4 vertexColor;
        out vec2 texCoord0;

        void main() {
            gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

            vec3 glow = Color.rgb;
            $builder
            vertexColor = vec4(glow, Color.a);
            texCoord0 = UV0;
        }
        """.trimIndent()
    }

    private inline fun appendConditional(
        builder: StringBuilder,
        isElse: Boolean,
        color: Int,
        block: () -> Unit
    ) {
        if (isElse) {
            builder.append(" else ")
        }
        val (r, g, b) = this.intToFloatColor(color)
        builder.append("if (glow.r == $r && glow.g == $g && glow.b == $b) { // #${color.toString(16)} $NL_INDENT")
        block()
        builder.append("}")
    }

    private fun intToFloatColor(color: Int): FloatColor {
        return FloatColor(
            FastColor.ARGB32.red(color) / 255.0F,
            FastColor.ARGB32.green(color) / 255.0F,
            FastColor.ARGB32.blue(color) / 255.0F
        )
    }

    private data class FloatColor(val r: Float, val g: Float, val b: Float)

    public class ColorReplacer {
        private val map = Int2IntOpenHashMap()
        private var rainbow: Int? = null

        public fun set(formatting: ChatFormatting, replacement: Int) {
            val original = formatting.color ?:
                throw IllegalArgumentException("Invalid color provided: $formatting")
            this.map[original] = replacement
        }

        public fun rainbow(formatting: ChatFormatting) {
            this.rainbow = formatting.color ?:
                throw IllegalArgumentException("Invalid color provided: $formatting")
        }

        internal fun getMap(): Int2IntMap {
            return this.map
        }

        internal fun getRainbow(): Int? {
            return this.rainbow
        }
    }
}