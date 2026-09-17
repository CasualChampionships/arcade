/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.model.format.blockbench.serializer.LenientFloat
import net.casual.arcade.model.format.blockbench.serializer.LenientTextureId
import net.casual.arcade.model.format.blockbench.serializer.RawMolangExpression

@Serializable
internal sealed class BlockbenchProject {
    abstract val meta: Metadata
    abstract val name: String?
    abstract val resolution: Resolution?
    abstract val elements: List<Element>
    abstract val textures: List<Texture>
    abstract val animations: List<Animation>

    abstract val flipAnimationAxes: Boolean

    abstract fun outliner(): List<Any>

    sealed interface Outliner {
        class Element(val uuid: String): Outliner

        class Group(
            val name: String,
            val uuid: String,
            val origin: FloatArray,
            val rotation: FloatArray,
            val export: Boolean,
            val children: List<Outliner>
        ): Outliner
    }

    @Serializable
    class Metadata(
        @SerialName("format_version")
        val formatVersion: String = "5.0",
        @SerialName("model_format")
        val modelFormat: String? = null,
        @SerialName("box_uv")
        val boxUv: Boolean = false
    )

    @Serializable
    class Resolution(
        val width: Int = 16,
        val height: Int = 16
    )

    @Serializable
    class Element(
        val name: String = "",
        val type: String = "cube",
        val uuid: String,
        val from: FloatArray? = null,
        val to: FloatArray? = null,
        val origin: FloatArray? = null,
        val rotation: FloatArray? = null,
        val position: FloatArray? = null,
        val inflate: Float = 0.0F,
        val shade: Boolean = true,
        @SerialName("light_emission")
        val lightEmission: Int = 0,
        @SerialName("box_uv")
        val boxUv: Boolean? = null,
        @SerialName("uv_offset")
        val uvOffset: FloatArray? = null,
        @SerialName("mirror_uv")
        val mirrorUv: Boolean = false,
        val export: Boolean = true,
        val faces: Map<String, Face> = mapOf()
    )
    @Serializable
    class Face(
        val uv: FloatArray? = null,
        val texture: LenientTextureId = null,
        val rotation: Int = 0
    )

    @Serializable
    class Texture(
        val name: String = "",
        val id: String? = null,
        val uuid: String? = null,
        val width: Int = 0,
        val height: Int = 0,
        @SerialName("uv_width")
        val uvWidth: Int = 0,
        @SerialName("uv_height")
        val uvHeight: Int = 0,
        @SerialName("frame_time")
        val frameTime: Int = 1,
        @SerialName("render_sides")
        val renderSides: String = "auto",
        val source: String? = null
    )

    @Serializable
    class Animation(
        val uuid: String? = null,
        val name: String,
        val loop: String = "once",
        val override: Boolean = false,
        val length: Float = 0.0F,
        @SerialName("start_delay")
        val startDelay: LenientFloat = 0.0F,
        @SerialName("loop_delay")
        val loopDelay: LenientFloat = 0.0F,
        val animators: Map<String, Animator> = mapOf()
    )

    @Serializable
    class Animator(
        val name: String? = null,
        val type: String = "bone",
        val keyframes: List<Keyframe> = listOf()
    )

    @Serializable
    class Keyframe(
        val channel: String,
        @SerialName("data_points")
        val dataPoints: List<DataPoint> = listOf(),
        val time: Float = 0.0F,
        val interpolation: String = "linear",
        val easing: String? = null,
        val easingArgs: DoubleArray? = null,
        @SerialName("bezier_left_time")
        val bezierLeftTime: FloatArray? = null,
        @SerialName("bezier_left_value")
        val bezierLeftValue: FloatArray? = null,
        @SerialName("bezier_right_time")
        val bezierRightTime: FloatArray? = null,
        @SerialName("bezier_right_value")
        val bezierRightValue: FloatArray? = null
    )

    @Serializable
    class DataPoint(
        val x: RawMolangExpression = null,
        val y: RawMolangExpression = null,
        val z: RawMolangExpression = null,
        val effect: String? = null,
        val file: String? = null,
        val script: String? = null
    )

}

