/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.pack

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.IntArraySet
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.geometry.BoneGeometry
import net.casual.arcade.model.geometry.Cube
import net.casual.arcade.model.geometry.CubeFace
import net.casual.arcade.utils.Identifier
import net.minecraft.resources.Identifier
import org.jetbrains.annotations.ApiStatus.Internal
import org.joml.Vector3fc
import java.awt.image.BufferedImage
import kotlin.collections.iterator

public class ModelResources(
    private val definition: ModelDefinition
) {
    public val id: Identifier
        get() = this.definition.id

    @Internal
    public fun getTextures(): List<ModelTexture> {
        val textures = ArrayList<ModelTexture>()
        for ((index, texture) in this.definition.textures.withIndex()) {
            var meta: JsonObject? = null
            if (texture.animated) {
                val animation = JsonObject()
                animation.addProperty("frametime", texture.frameTime)
                meta = JsonObject()
                meta.add("animation", animation)
            }

            textures.add(ModelTexture(this.getTextureId(index), texture.image, meta))
        }
        return textures
    }

    @Internal
    public fun getModelJsons(): Map<Identifier, JsonObject> {
        val models = LinkedHashMap<Identifier, JsonObject>()
        for (bone in this.definition.bones()) {
            val geometry = bone.geometry ?: continue
            models[geometry.model] = this.createModelJson(geometry)
        }
        return models
    }

    @Internal
    public fun getItemJsons(): Map<Identifier, JsonObject> {
        val items = LinkedHashMap<Identifier, JsonObject>()
        for (bone in this.definition.bones()) {
            val model = bone.geometry?.model ?: continue
            items[model] = this.createItemJson(model)
        }
        return items
    }

    private fun createModelJson(geometry: BoneGeometry): JsonObject {
        val textures = JsonObject()
        val used = geometry.getTextureIndices()
        for (index in used) {
            textures.addProperty(index.toString(), this.getTextureId(index).toString())
        }
        if (used.isNotEmpty()) {
            val index = used.first()
            textures.addProperty("particle", this.getTextureId(index).toString())
        }

        val elements = JsonArray()
        for (cube in geometry.cubes) {
            elements.add(this.createElementJson(cube))
        }

        val rotation = JsonArray()
        rotation.add(0)
        // Display.Item renders rotated 180º for some reason
        rotation.add(180)
        rotation.add(0)
        val head = JsonObject()
        head.add("rotation", rotation)
        val display = JsonObject()
        display.add("head", head)

        val model = JsonObject()
        model.add("textures", textures)
        model.add("elements", elements)
        model.add("display", display)
        return model
    }

    private fun BoneGeometry.getTextureIndices(): IntArray {
        val textures = IntArraySet()
        for (cube in this.cubes) {
            for (face in cube.faces.values) {
                textures.add(face.texture)
            }
        }
        return textures.toIntArray()
    }

    private fun createElementJson(cube: Cube): JsonObject {
        val element = JsonObject()
        element.add("from", cube.from.toJson())
        element.add("to", cube.to.toJson())
        if (cube.rotated) {
            val rotation = JsonObject()
            rotation.add("origin", cube.origin.toJson())
            rotation.addProperty("x", cube.rotation.x())
            rotation.addProperty("y", cube.rotation.y())
            rotation.addProperty("z", cube.rotation.z())
            element.add("rotation", rotation)
        }
        if (!cube.shade) {
            element.addProperty("shade", false)
        }
        if (cube.lightEmission > 0) {
            element.addProperty("light_emission", cube.lightEmission)
        }
        val faces = JsonObject()
        for ((direction, face) in cube.faces) {
            faces.add(direction.serializedName, this.createFaceJson(face))
        }
        element.add("faces", faces)
        return element
    }

    private fun createFaceJson(face: CubeFace): JsonObject {
        val json = JsonObject()
        val uv = JsonArray()
        uv.add(face.u0)
        uv.add(face.v0)
        uv.add(face.u1)
        uv.add(face.v1)
        json.add("uv", uv)
        json.addProperty("texture", "#${face.texture}")
        json.addProperty("tintindex", 0)
        if (face.rotation != 0) {
            json.addProperty("rotation", face.rotation)
        }
        return json
    }

    private fun createItemJson(model: Identifier): JsonObject {
        val tint = JsonObject()
        tint.addProperty("type", "minecraft:dye")
        tint.addProperty("default", 0xFFFFFF)
        val tints = JsonArray()
        tints.add(tint)

        val inner = JsonObject()
        inner.addProperty("type", "minecraft:model")
        inner.addProperty("model", model.toString())
        inner.add("tints", tints)

        val json = JsonObject()
        json.add("model", inner)
        return json
    }

    private fun getTextureId(index: Int): Identifier {
        val texture = this.definition.textures[index]
        return Identifier(this.id.namespace, "item/model/${this.id.path}/${texture.name}")
    }

    private fun Vector3fc.toJson(): JsonArray {
        val array = JsonArray()
        array.add(this.x())
        array.add(this.y())
        array.add(this.z())
        return array
    }

    public data class ModelTexture(val id: Identifier, val image: BufferedImage, val meta: JsonObject?)
}