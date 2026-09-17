/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import it.unimi.dsi.fastutil.floats.FloatFloatPair
import it.unimi.dsi.fastutil.ints.IntIntPair
import net.casual.arcade.model.ArcadeModelEngine
import net.casual.arcade.model.definition.BoneGeometry
import net.casual.arcade.model.definition.BoneTag
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode
import net.casual.arcade.model.definition.ModelTexture
import net.casual.arcade.model.format.ModelFormatException
import net.casual.arcade.model.format.blockbench.BlockbenchProject.Outliner
import net.casual.arcade.model.geometry.Cube
import net.casual.arcade.model.geometry.CubeFace
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.collection.component1
import net.casual.arcade.utils.collection.component2
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import org.joml.Vector3f
import org.joml.Vector3fc
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

internal class BlockbenchProjectConverter(
    private val id: Identifier,
    private val project: BlockbenchProject,
) {
    private val elementsByUUID = this.project.elements.associateBy { element -> element.uuid }

    private val textureIndices = HashMap<String, Int>()
    private val textures = ArrayList<ModelTexture>()

    private val existingNodeNames = HashSet<String>()

    private val min = Vector3f(Float.MAX_VALUE)
    private val max = Vector3f(-Float.MAX_VALUE)

    fun convert(): ModelDefinition {
        this.validate()
        this.loadTextures()

        val outliner = this.project.outliner().toMutableList()
        val dangling = outliner.filterIsInstanceTo<Outliner.Element, _>(LinkedHashSet())
        if (dangling.isNotEmpty()) {
            outliner.removeAll(dangling)
            outliner.add(Outliner.Group("root", "$ROOT_UUID", Vector3f(), Vector3f(), true, dangling))
        }

        val roots = outliner.filterIsInstance<Outliner.Group>().map { group ->
            this.convertGroupToBone(group, Vector3f(), setOf())
        }

        // TODO: Animations, width/height
        return ModelDefinition.create(this.id, roots, mapOf(), this.textures, 0.0F, 0.0F)
    }

    private fun convertGroupToBone(
        group: Outliner.Group,
        parentOrigin: Vector3fc,
        parentTags: Set<BoneTag>
    ): ModelNode.Bone {
        val name = this.uniqueNodeName(group.name)
        val origin = group.origin
        val rotation = group.rotation
        val tags = this.computeBoneTags(group.name, parentTags)

        val children = ArrayList<ModelNode>()
        val cubes = ArrayList<Cube>()
        for (child in group.children) {
            when (child) {
                is Outliner.Group -> children.add(this.convertGroupToBone(child, origin, tags))
                is Outliner.Element -> this.addElementToBone(child, origin, tags, children, cubes)
            }
        }

        val pivot = origin.sub(parentOrigin, Vector3f()).div(16.0F)
        val geometry = this.createGeometry(name, cubes, pivot)
        return ModelNode.Bone(name, UUID.fromString(group.uuid), pivot, rotation, tags, children, geometry)
    }

    private fun createGeometry(name: String, cubes: List<Cube>, pivot: Vector3fc): BoneGeometry {
        TODO()
    }

    private fun addElementToBone(
        unresolved: Outliner.Element,
        origin: Vector3fc,
        tags: Set<BoneTag>,
        children: MutableList<ModelNode>,
        cubes: MutableList<Cube>
    ) {
        val element = this.elementsByUUID[unresolved.uuid] ?: return
        when (element.type) {
            "cube" -> if (element.export) this.addCubesToBone(element, origin, cubes)
            "locator" -> children.add(this.convertElementToLocator(element, origin, tags))
        }
    }

    private fun addCubesToBone(
        element: BlockbenchProject.Element,
        origin: Vector3fc,
        cubes: MutableList<Cube>
    ) {
        val from = Vector3f(element.from)
        val to = Vector3f(element.to)
        this.min.min(from)
        this.max.max(to)

        val inflate = element.inflate
        from.sub(inflate, inflate, inflate).sub(origin)
        to.add(inflate, inflate, inflate).sub(origin)

        val cubeOrigin = element.origin.sub(origin, Vector3f())
        val rotation = element.rotation

        val faces = EnumUtils.mapOf<Direction, CubeFace>()
        for ((key, face) in element.faces) {
            val direction = Direction.byName(key) ?: continue
            val texture = this.getTextureIndex(face.texture) ?: continue
            val uv = face.uv ?: continue
            if (uv.size < 4) {
                continue
            }

            val (scaleU, scaleV) = this.uvScale(texture)
            faces[direction] = CubeFace(uv[0] * scaleU, uv[1] * scaleV, uv[2] * scaleU, uv[3] * scaleV, texture, face.rotation)
        }

        cubes.add(Cube(from, to, cubeOrigin, rotation, faces, element.shade, element.lightEmission))

        // TODO: Add backfaces?
    }

    private fun convertElementToLocator(
        element: BlockbenchProject.Element,
        origin: Vector3fc,
        parentTags: Set<BoneTag>
    ): ModelNode {
        val name = this.uniqueNodeName(element.name)
        val position = element.position.sub(origin, Vector3f()).div(16.0F)
        val rotation = element.rotation
        val tags = this.computeBoneTags(element.name, parentTags)
        return ModelNode.Locator(name, UUID.fromString(element.uuid), position, rotation, tags)
    }

    private fun computeBoneTags(name: String, parentTags: Set<BoneTag>): Set<BoneTag> {
        // TODO: Compute tags from name
        return parentTags
    }

    private fun validate() {
        val format = this.project.meta.modelFormat
        if (format == "animated_java_blueprint") {
            throw ModelFormatException("AJ is not supported yet")
        }

        for (element in this.project.elements) {
            when (element.type) {
                "cube", "locator" -> {}
                "mesh" -> throw ModelFormatException("Model ${this.id} contains unsupported mesh element '${element.name}'")
                else -> throw ModelFormatException("Model ${this.id} contains unsupported element type '${element.type}' for '${element.name}'")
            }
        }
    }

    private fun loadTextures() {
        for ((index, texture) in this.project.textures.withIndex()) {
            val source = texture.source ?: throw ModelFormatException("Texture '${texture.name}' in ${this.id} has no embedded image")
            val image = try {
                val bytes = Base64.getDecoder().decode(source.substringAfter(BASE64_PREFIX))
                ImageIO.read(ByteArrayInputStream(bytes))
            } catch (e: IOException) {
                throw ModelFormatException("Texture '${texture.name}' couldn't be read!", e)
            }
            val name = this.uniqueTextureName(texture.name)
            this.textures.add(ModelTexture(name, image, texture.width, texture.height, texture.frameTime))
            if (texture.uuid != null) {
                this.textureIndices[texture.uuid] = index
            }
            this.textureIndices[index.toString()] = index
        }
    }

    private fun getTextureIndex(key: String?): Int? {
        return if (key != null) this.textureIndices[key] else null
    }

    private fun uvSize(texture: Int): IntIntPair {
        val raw = this.project.textures[texture]
        val resolution = this.project.resolution
        val width = if (raw.uvWidth > 0) raw.uvWidth else resolution?.width ?: 16
        val height = if (raw.uvHeight > 0) raw.uvHeight else resolution?.height ?: 16
        return IntIntPair.of(width, height)
    }

    private fun uvScale(texture: Int): FloatFloatPair {
        val (width, height) = this.uvSize(texture)
        return FloatFloatPair.of(16.0F / width, 16.0F / height)
    }

    private fun uniqueTextureName(original: String): String {
        var base = original.lowercase().removeSuffix(".png")
        while (base.endsWith(".png")) {
            base = base.removeSuffix(".png")
        }
        base = sanitize(base)
        var name = base
        var counter = 2
        while (this.textures.any { it.name == name }) {
            name = "${base}_${counter++}"
        }
        return name
    }

    private fun uniqueNodeName(original: String): String {
        val base = original.ifBlank { "node" }
        var name = base
        var counter = 2
        while (!this.existingNodeNames.add(name)) {
            name = "${base}_${counter++}"
        }
        return name
    }

    private fun sanitize(name: String): String {
        return name.lowercase().map {
            if (IdentifierUtils.isValidNamespaceChar(it)) it else '_'
        }.joinToString("")
    }

    private companion object {
        const val BASE64_PREFIX = "base64,"

        val ROOT_UUID: UUID = UUID.nameUUIDFromBytes("${ArcadeModelEngine.MOD_ID}:root".toByteArray())
    }
}