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
internal class BlockbenchProjectV5(
    override val meta: Metadata,
    override val name: String?,
    override val resolution: Resolution?,
    override val elements: List<Element>,
    override val textures: List<Texture>,
    override val animations: List<Animation>,
    private val outliner: List<OutlinerV5>,
    private val groups: List<Group>
): BlockbenchProject() {
    override val flipAnimationAxes: Boolean
        get() = false

    override fun outliner(): List<Outliner> {
        val groups = this.groups.associateBy { group -> group.uuid }
        return this.outliner.map { unresolved -> this.resolve(unresolved, groups) }
    }

    private fun resolve(outliner: OutlinerV5, groups: Map<String, Group>): Outliner {
        return when (outliner) {
            is OutlinerV5.Element -> Outliner.Element(outliner.uuid)
            is OutlinerV5.Group -> this.resolveGroup(outliner, groups)
        }
    }

    private fun resolveGroup(unresolved: OutlinerV5.Group, groups: Map<String, Group>): Outliner.Group {
        val resolved = groups[unresolved.uuid]
        return Outliner.Group(
            resolved?.name ?: "",
            unresolved.uuid,
            resolved?.origin ?: Vector3f(),
            resolved?.rotation ?: Vector3f(),
            resolved?.export ?: true,
            unresolved.children.map { child -> this.resolve(child, groups) }
        )
    }

    @Serializable(with = OutlinerV5.Serializer::class)
    sealed interface OutlinerV5 {
        @Serializable(with = Element.Serializer::class)
        class Element(val uuid: String): OutlinerV5 {
            object Serializer: InlinedStringSerializer<Element>(Element::uuid, ::Element)
        }

        @Serializable
        class Group(
            val uuid: String,
            val children: List<OutlinerV5> = listOf()
        ): OutlinerV5

        object Serializer: JsonContentPolymorphicSerializer<OutlinerV5>(OutlinerV5::class) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OutlinerV5> {
                return if (element is JsonPrimitive) Element.serializer() else Group.serializer()
            }
        }
    }

    @Serializable
    class Group(
        val name: String = "",
        val uuid: String,
        val origin: SerializableVector3fc = Vector3f(),
        val rotation: SerializableVector3fc = Vector3f(),
        val export: Boolean = true
    )
}