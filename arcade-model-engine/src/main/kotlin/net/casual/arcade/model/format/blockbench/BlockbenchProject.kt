/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import net.casual.arcade.model.format.blockbench.serializer.LenientFloat
import net.casual.arcade.model.format.blockbench.serializer.LenientTextureId
import net.casual.arcade.model.format.blockbench.serializer.RawMolangExpression
import net.casual.arcade.model.format.blockbench.serializer.SerializableVector3fc
import net.fabricmc.loader.api.SemanticVersion
import net.fabricmc.loader.impl.util.version.VersionParser
import org.joml.Vector3f
import org.joml.Vector3fc

@Serializable(with = BlockbenchProject.Serializer::class)
internal sealed class BlockbenchProject {
    abstract val meta: Metadata
    abstract val name: String?
    abstract val resolution: Resolution?
    abstract val elements: List<Element>
    abstract val textures: List<Texture>
    abstract val animations: List<Animation>

    abstract val flipAnimationAxes: Boolean

    abstract fun outliner(): List<Outliner>

    sealed interface Outliner {
        class Element(val uuid: String): Outliner

        class Group(
            val name: String,
            val uuid: String,
            val origin: Vector3fc,
            val rotation: Vector3fc,
            val export: Boolean,
            val children: Collection<Outliner>
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
        val from: SerializableVector3fc = Vector3f(),
        val to: SerializableVector3fc = Vector3f(),
        val origin: SerializableVector3fc = Vector3f(),
        val rotation: SerializableVector3fc = Vector3f(),
        val position: SerializableVector3fc = Vector3f(),
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
        val bezierLeftTime: SerializableVector3fc? = null,
        @SerialName("bezier_left_value")
        val bezierLeftValue: SerializableVector3fc = Vector3f(),
        @SerialName("bezier_right_time")
        val bezierRightTime: SerializableVector3fc? = null,
        @SerialName("bezier_right_value")
        val bezierRightValue: SerializableVector3fc = Vector3f()
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

    object Serializer: JsonContentPolymorphicSerializer<BlockbenchProject>(BlockbenchProject::class) {
        private val V5: SemanticVersion = VersionParser.parseSemantic("5.0")

        override fun selectDeserializer(element: JsonElement): KSerializer<out BlockbenchProject> {
            val version = (element.jsonObject["meta"]?.jsonObject?.get("format_version") as? JsonPrimitive)?.contentOrNull
                ?: return BlockbenchProjectV4.serializer()
            val parsed = VersionParser.parse(version, false)
            return if (parsed >= V5) BlockbenchProjectV5.serializer() else BlockbenchProjectV4.serializer()
        }
    }
}

