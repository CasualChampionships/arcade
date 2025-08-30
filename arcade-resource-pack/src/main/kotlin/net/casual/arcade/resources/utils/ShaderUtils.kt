/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.utils

import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.ChatFormatting
import net.minecraft.util.ARGB

public object ShaderUtils {
    private const val NL_INDENT = "\n            "
    
    internal fun getOutlineVertexShader(
        colors: Int2IntMap,
        rainbow: Int?
    ): String {
        var first = true
        val builder = StringBuilder()
        for (entry in colors.int2IntEntrySet()) {
            this.appendConditional(builder, !first, entry.intKey) {
                val (r, g, b) = this.intToFloatColor(entry.intValue)
                val hex = "#%06X".format(entry.intValue)
                builder.append("    glow = vec3($r, $g, $b); // #$hex $NL_INDENT")
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

        #moj_import <minecraft:dynamictransforms.glsl>
        #moj_import <minecraft:projection.glsl>
        ${if (rainbow != null) "#moj_import <minecraft:globals.glsl>" else ""}

        in vec3 Position;
        in vec4 Color;
        in vec2 UV0;
        
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
        val hex = "#%06X".format(color)
        builder.append("if (glow.r == $r && glow.g == $g && glow.b == $b) { // #${hex} $NL_INDENT")
        block()
        builder.append("}")
    }

    private fun intToFloatColor(color: Int): FloatColor {
        return FloatColor(
            ARGB.red(color) / 255.0F,
            ARGB.green(color) / 255.0F,
            ARGB.blue(color) / 255.0F
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