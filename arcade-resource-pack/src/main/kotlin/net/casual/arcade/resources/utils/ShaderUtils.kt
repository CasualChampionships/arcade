package net.casual.arcade.resources.utils

import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.util.FastColor

public object ShaderUtils {
    private const val NL_INDENT = "\n            "

    public fun createOutlineShader(block: ColorReplacer.() -> Unit): String {
        val replacer = ColorReplacer()
        replacer.block()
        return this.getOutlineShader(replacer.getMap())
    }

    private fun getOutlineShader(colors: Int2IntMap): String {
        var first = true
        val builder = StringBuilder()
        for (entry in colors.int2IntEntrySet()) {
            val (originalR, originalG, originalB) = this.intToFloatColor(entry.intKey)
            val (replacementR, replacementG, replacementB) = this.intToFloatColor(entry.intValue)
            if (!first) {
                builder.append(" else ")
            }
            first = false
            builder.append("if (glow.r == $originalR && glow.g == $originalG && glow.b == $originalB) {$NL_INDENT")
            builder.append("    glow = vec3($replacementR, $replacementG, $replacementB);$NL_INDENT")
            builder.append("}")
        }

        return """
        #version 150

        in vec3 Position;
        in vec4 Color;
        in vec2 UV0;

        uniform mat4 ModelViewMat;
        uniform mat4 ProjMat;

        out vec4 vertexColor;
        out vec2 texCoord0;

        void main() {
            gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

            vec3 glow = Color.rgb
            $builder
            vertexColor = Vec4(glow, Color.a);
            texCoord0 = UV0;
        }
        """.trimIndent()
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

        public fun set(formatting: ChatFormatting, replacement: Int) {
            val original = formatting.color ?:
                throw IllegalArgumentException("Invalid color provided: $formatting")
            this.map[original] = replacement
        }

        internal fun getMap(): Int2IntMap {
            return this.map
        }
    }
}