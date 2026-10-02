/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import it.unimi.dsi.fastutil.floats.FloatFloatPair
import it.unimi.dsi.fastutil.ints.IntIntPair
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.model.ArcadeModelEngine
import net.casual.arcade.model.animation.molang.MolangVec3
import net.casual.arcade.model.animation.timeline.BoneTimeline
import net.casual.arcade.model.animation.timeline.EffectKeyframe
import net.casual.arcade.model.animation.timeline.Keyframe
import net.casual.arcade.model.animation.timeline.KeyframeChannel
import net.casual.arcade.model.definition.BoneTag
import net.casual.arcade.model.definition.ModelAnimation
import net.casual.arcade.model.definition.ModelBounds
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode
import net.casual.arcade.model.definition.ModelTexture
import net.casual.arcade.model.format.ModelFormatException
import net.casual.arcade.model.format.ModelLoadOptions
import net.casual.arcade.model.format.blockbench.BlockbenchProject.Outliner
import net.casual.arcade.model.geometry.BoneGeometry
import net.casual.arcade.model.geometry.Cube
import net.casual.arcade.model.geometry.CubeFace
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.Identifier
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.collection.component1
import net.casual.arcade.utils.collection.component2
import net.casual.arcade.utils.math.Easing
import net.casual.arcade.utils.string.isUUID
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import org.joml.Vector3f
import org.joml.Vector3fc
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.iterator
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal class BlockbenchProjectConverter(
    private val id: Identifier,
    private val project: BlockbenchProject,
    private val options: ModelLoadOptions
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
        val nodesByUUID = roots.flatMap { node -> node.descendents() + node }
            .associateBy { node -> node.uuid }
        val animations = this.project.animations.associate { animation ->
            animation.name to this.convertAnimation(animation, nodesByUUID)
        }

        return ModelDefinition.create(this.id, roots, animations, this.textures, this.bounds())
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
        val geometry = when {
            cubes.isEmpty() || !group.export || tags.contains(BoneTag.HITBOX) -> null
            this.rotates(group.uuid) -> this.createGeometry(name, cubes, Vector3f())
            else -> this.createGeometry(name, cubes, pivot)
        }
        return ModelNode.Bone(name, UUID.fromString(group.uuid), pivot, rotation, tags, children, geometry)
    }

    private fun createGeometry(name: String, cubes: List<Cube>, pivot: Vector3fc): BoneGeometry {
        val shift = Vector3f(pivot).mul(16.0F)
        val own = extent(cubes)
        val shifted = cubes.map { it.translate(shift) }
        val parented = extent(shifted)
        val useParent = parented <= max(own, BoneGeometry.HALF_EXTENT)
        val chosen = if (useParent) shifted else cubes
        val extent = if (useParent) parented else own
        val offset = if (useParent) pivot.negate(Vector3f()) else Vector3f()

        val scale = min(1.0F, BoneGeometry.HALF_EXTENT / extent)
        val fitted = chosen.map { cube ->
            cube.scale(scale).translate(BoneGeometry.CENTER)
        }
        val itemModel = Identifier(this.id.namespace, "model/${this.id.path}/${this.sanitize(name)}")
        return BoneGeometry.create(fitted, itemModel, 1.0F / scale, offset)
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
        val coverage = EnumUtils.mapOf<Direction, Coverage>()
        for ((key, face) in element.faces) {
            val direction = Direction.byName(key) ?: continue
            val texture = this.getTextureIndex(face.texture) ?: continue
            val uv = face.uv ?: continue
            if (uv.size < 4) {
                continue
            }

            val (scaleU, scaleV) = this.uvScale(texture)
            faces[direction] = CubeFace(uv[0] * scaleU, uv[1] * scaleV, uv[2] * scaleU, uv[3] * scaleV, texture, face.rotation)
            if (this.options.addBackfaces) {
                coverage[direction] = this.coverage(texture, uv)
            }
        }

        val backfaces = ArrayList<Cube>()
        if (this.options.addBackfaces) {
            this.addCubeBackfacesToBone(element, cubeOrigin, rotation, from, to, faces, coverage, backfaces)
        }

        cubes.add(Cube.create(from, to, cubeOrigin, rotation, faces, element.shade, element.lightEmission))
        cubes.addAll(backfaces)
    }

    private fun addCubeBackfacesToBone(
        element: BlockbenchProject.Element,
        cubeOrigin: Vector3fc,
        rotation: Vector3fc,
        from: Vector3fc,
        to: Vector3fc,
        faces: MutableMap<Direction, CubeFace>,
        coverage: Map<Direction, Coverage>,
        cubes: MutableList<Cube>
    ) {
        if (Direction.entries.all { dir -> coverage[dir] == Coverage.Full }) {
            return
        }
        for ((direction, face) in faces.entries.toList()) {
            if (coverage[direction] == Coverage.None || this.project.textures[face.texture].renderSides == "front") {
                continue
            }
            val axis = direction.axis
            val opposite = direction.opposite
            val back = this.mirror(face, axis)
            if (from[axis.ordinal] == to[axis.ordinal]) {
                if (coverage[opposite] == null || coverage[opposite] == Coverage.None) {
                    faces[opposite] = back
                }
                continue
            }
            val plane = (if (direction.axisDirection == Direction.AxisDirection.POSITIVE) to else from)[axis.ordinal]
            val backFrom = Vector3f(from).setComponent(axis.ordinal, plane)
            val backTo = Vector3f(to).setComponent(axis.ordinal, plane)
            val backFaces = EnumUtils.mapOf<Direction, CubeFace>()
            backFaces[opposite] = back
            cubes.add(Cube.create(backFrom, backTo, cubeOrigin, rotation, backFaces, element.shade, element.lightEmission))
        }
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
        return this.options.boneTagResolver.resolve(name, parentTags)
    }

    private fun convertAnimation(animation: BlockbenchProject.Animation, nodes: Map<UUID, ModelNode>): ModelAnimation {
        val timelines = Object2ObjectOpenHashMap<UUID, BoneTimeline>()
        val effects = ArrayList<EffectKeyframe>()
        for ((key, animator) in animation.animators) {
            if (animator.type == "effect") {
                effects.addAll(this.convertEffects(animator))
                continue
            }
            if (animator.type == "bone" && key.isUUID()) {
                val uuid = UUID.fromString(key)
                if (nodes.containsKey(uuid)) {
                    timelines[uuid] = this.convertTimeline(animator)
                }
            }
        }
        return ModelAnimation.create(
            animation.name,
            animation.length,
            animation.loop,
            animation.override,
            animation.startDelay,
            animation.loopDelay,
            effects,
            timelines
        )
    }

    private fun convertEffects(animator: BlockbenchProject.Animator): List<EffectKeyframe> {
        val effects = ArrayList<EffectKeyframe>()
        for (keyframe in animator.keyframes) {
            for (point in keyframe.dataPoints) {
                when (keyframe.channel) {
                    "sound" -> {
                        val sound = Identifier.tryParse(point.effect ?: continue) ?: continue
                        effects.add(EffectKeyframe.Sound(keyframe.time, sound))
                    }
                    "timeline" -> {
                        val script = point.script ?: continue
                        effects.add(EffectKeyframe.Command(keyframe.time, script))
                    }
                }
            }
        }
        return effects
    }

    private fun convertTimeline(animator: BlockbenchProject.Animator): BoneTimeline {
        val channels = animator.keyframes.groupBy { keyframe -> keyframe.channel }
        val position = this.convertKeyframeChannel(channels["position"], KeyframeChannel.Type.Position)
        val rotation = this.convertKeyframeChannel(channels["rotation"], KeyframeChannel.Type.Rotation)
        val scale = this.convertKeyframeChannel(channels["scale"], KeyframeChannel.Type.Scale)
        return BoneTimeline(position, rotation, scale)
    }

    private fun convertKeyframeChannel(
        keyframes: List<BlockbenchProject.Keyframe>?,
        type: KeyframeChannel.Type
    ): KeyframeChannel? {
        if (keyframes != null) {
            return KeyframeChannel.create(keyframes.map { keyframe -> this.convertKeyframe(keyframe, type) })
        }
        return null
    }

    private fun convertKeyframe(keyframe: BlockbenchProject.Keyframe, type: KeyframeChannel.Type): Keyframe {
        val pre = this.convertVector(keyframe.dataPoints.getOrNull(0), type)
        val post = this.convertVector(keyframe.dataPoints.getOrNull(0), type) { pre }
        val bezier: Keyframe.BezierHandles? = null
        if (keyframe.bezierLeftTime != null || keyframe.bezierRightTime != null) {
            Keyframe.BezierHandles(
                keyframe.bezierLeftTime ?: DEFAULT_LEFT_TIME,
                this.flipVectorForKeyframeChannel(Vector3f(keyframe.bezierLeftValue), type),
                keyframe.bezierRightTime ?: DEFAULT_RIGHT_TIME,
                this.flipVectorForKeyframeChannel(Vector3f(keyframe.bezierRightValue), type)
            )
        }
        val easing = BlockbenchEasing.from(keyframe.easing, keyframe.easingArgs ?: doubleArrayOf()) ?: Easing.LINEAR
        return Keyframe(keyframe.time, pre, post, keyframe.interpolation, easing, bezier)
    }

    private fun convertVector(
        point: BlockbenchProject.DataPoint?,
        type: KeyframeChannel.Type,
        fallback: () -> MolangVec3 = { this.fallbackVectorForKeyframeChannel(type) }
    ): MolangVec3 {
        if (point == null) {
            return fallback.invoke()
        }
        val x = point.x
        val y = point.y
        val z = point.z
        if (this.project.flipAnimationAxes) {
            return when (type) {
                KeyframeChannel.Type.Position -> MolangVec3(x.negate(), y, z)
                KeyframeChannel.Type.Rotation -> MolangVec3(x.negate(), y.negate(), z)
                KeyframeChannel.Type.Scale -> MolangVec3(x, y, z)
            }
        }
        return MolangVec3(x, y, z)
    }

    private fun flipVectorForKeyframeChannel(vector: Vector3f, type: KeyframeChannel.Type): Vector3f {
        if (this.project.flipAnimationAxes) {
            return when (type) {
                KeyframeChannel.Type.Position -> vector.mul(-1.0F, 1.0F, 1.0F)
                KeyframeChannel.Type.Rotation -> vector.mul(-1.0F, -1.0F, 1.0F)
                KeyframeChannel.Type.Scale -> vector
            }
        }
        return vector
    }

    private fun fallbackVectorForKeyframeChannel(type: KeyframeChannel.Type): MolangVec3 {
        return when (type) {
            KeyframeChannel.Type.Position -> MolangVec3.ZERO
            KeyframeChannel.Type.Rotation -> MolangVec3.ZERO
            KeyframeChannel.Type.Scale -> MolangVec3.ONE
        }
    }

    private fun mirror(face: CubeFace, axis: Direction.Axis): CubeFace {
        val rotation = (360 - face.rotation) % 360
        return if (axis == Direction.Axis.Y) {
            CubeFace(face.u0, face.v1, face.u1, face.v0, face.texture, rotation)
        } else {
            CubeFace(face.u1, face.v0, face.u0, face.v1, face.texture, rotation)
        }
    }

    private fun coverage(texture: Int, uv: FloatArray): Coverage {
        val image = this.textures[texture].image
        val scale = image.width.toFloat() / this.uvSize(texture).firstInt()
        val x0 = floor(min(uv[0], uv[2]) * scale).toInt().coerceIn(0, image.width)
        val x1 = ceil(max(uv[0], uv[2]) * scale).toInt().coerceIn(0, image.width)
        val y0 = floor(min(uv[1], uv[3]) * scale).toInt().coerceIn(0, image.height)
        val y1 = ceil(max(uv[1], uv[3]) * scale).toInt().coerceIn(0, image.height)
        if (x0 >= x1 || y0 >= y1) {
            return Coverage.None
        }
        var painted = false
        var opaque = true
        for (y in y0 until y1) {
            for (x in x0 until x1) {
                val alpha = image.getRGB(x, y) ushr 24
                painted = painted || alpha != 0
                opaque = opaque && alpha == 0xFF
            }
        }
        return if (!painted) Coverage.None else if (opaque) Coverage.Full else Coverage.Partial
    }

    private fun extent(cubes: List<Cube>): Float {
        var extent = 0.0F
        for (cube in cubes) {
            for (i in 0..2) {
                extent = max(extent, max(abs(cube.from[i]), abs(cube.to[i])))
            }
        }
        return extent
    }

    private fun bounds(): ModelBounds {
        if (this.options.boundsOverride != null) {
            return this.options.boundsOverride
        }

        if (this.min.x > this.max.x) {
            return ModelBounds(1.0F, 1.0F)
        }
        val width = max(this.max.x - this.min.x, this.max.z - this.min.z) / 16.0F
        val height = (this.max.y - this.min.y) / 16.0F
        return ModelBounds(width, height)
    }

    private fun rotates(uuid: String): Boolean {
        return this.project.animations.any { animation ->
            animation.animators[uuid]?.keyframes?.any { it.channel == "rotation" } == true
        }
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

    private enum class Coverage {
        None, Partial, Full
    }

    private companion object {
        const val BASE64_PREFIX = "base64,"

        val ROOT_UUID: UUID = UUID.nameUUIDFromBytes("${ArcadeModelEngine.MOD_ID}:root".toByteArray())

        val DEFAULT_LEFT_TIME: Vector3fc = Vector3f(-0.1F)
        val DEFAULT_RIGHT_TIME: Vector3fc = Vector3f(0.1F)
    }
}