/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.casual.arcade.model.format.blockbench.serializer.InlinedStringSerializer
import net.casual.arcade.model.format.blockbench.serializer.SerializableVector3fc
import org.joml.Vector3f

@Serializable
internal class BlockbenchProjectV4(
    override val meta: Metadata,
    override val name: String?,
    override val resolution: Resolution?,
    override val elements: List<Element>,
    override val textures: List<Texture>,
    override val animations: List<Animation>,
    private val outliner: List<OutlinerV4>
): BlockbenchProject() {
    override val flipAnimationAxes: Boolean
        get() = true

    override fun outliner(): List<Outliner> {
        return this.outliner.map(this::resolve)
    }

    private fun resolve(outliner: OutlinerV4): Outliner {
        return when (outliner) {
            is OutlinerV4.Element -> Outliner.Element(outliner.uuid)
            is OutlinerV4.Group -> Outliner.Group(
                outliner.name, outliner.uuid, outliner.origin, outliner.rotation, outliner.export, outliner.children.map(this::resolve)
            )
        }
    }

    @Serializable(with = OutlinerV4.Serializer::class)
    sealed interface OutlinerV4 {
        @Serializable(with = Element.Serializer::class)
        class Element(val uuid: String): OutlinerV4 {
            object Serializer: InlinedStringSerializer<Element>(Element::uuid, ::Element)
        }

        @Serializable
        class Group(
            val name: String = "",
            val uuid: String,
            val origin: SerializableVector3fc = Vector3f(),
            val rotation: SerializableVector3fc = Vector3f(),
            val export: Boolean = true,
            val children: List<OutlinerV4> = listOf()
        ): OutlinerV4

        object Serializer: JsonContentPolymorphicSerializer<OutlinerV4>(OutlinerV4::class) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OutlinerV4> {
                return if (element is JsonPrimitive) Element.serializer() else Group.serializer()
            }
        }
    }
}