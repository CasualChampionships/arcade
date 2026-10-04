package net.casual.arcade.model.format

import net.casual.arcade.model.TestModels
import net.casual.arcade.model.animation.AnimationLoop
import net.casual.arcade.model.animation.ModelAnimator
import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.pose.ModelPoser
import net.casual.arcade.model.animation.timeline.EffectKeyframe
import net.casual.arcade.model.animation.timeline.KeyframeInterpolation
import net.casual.arcade.model.cubeCount
import net.casual.arcade.model.definition.BoneTag
import net.casual.arcade.model.definition.BoneTagResolver
import net.casual.arcade.model.definition.ModelBounds
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.geometry.BoneGeometry
import net.casual.arcade.model.poses
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import org.joml.Matrix4f
import org.joml.Vector3f
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import java.util.*

class BlockbenchModelLoaderTests {
    @Test
    fun `robot loads`() {
        val model = TestModels.load(TestModels.ROBOT)
        assertEquals(listOf("body"), model.roots.map { it.name })
        assertEquals(
            setOf("body", "arms", "legs", "head", "antenna", "tail"),
            model.bones().map { it.name }.toSet()
        )

        val body = model.bone("body")!!
        val head = model.bone("head")!!
        assertSame(body, head.parent)
        assertEquals(Vector3f(0.0F, 11.0F / 16, 0.0F), Vector3f(head.pivot))
        assertTrue(head.has(BoneTag.HEAD))
        assertFalse(body.has(BoneTag.HEAD))
        assertEquals(Identifier.fromNamespaceAndPath("test", "model/robot/head"), head.geometry!!.model)

        val mouth = model.locator("mouth")!!
        assertSame(head, mouth.parent)
        assertTrue(mouth.has(BoneTag.HEAD_CHILD))
        assertEquals(Vector3f(0.0F, 1.0F / 16, -3.0F / 16), Vector3f(mouth.pivot))

        assertNull(model.bone("tail")!!.geometry)

        assertEquals(1, model.textures.size)
        assertEquals("texture", model.textures[0].name)
    }

    @Test
    fun `cubes are recentred and rescaled to fit item models`() {
        val model = TestModels.load(TestModels.ROBOT)
        val headCube = model.bone("head")!!.geometry!!.cubes.single()
        assertEquals(Vector3f(5.0F, 8.0F, 5.0F), Vector3f(headCube.from))
        assertEquals(Vector3f(11.0F, 14.0F, 11.0F), Vector3f(headCube.to))
        assertEquals(Vector3f(8.0F, 8.0F, 8.0F), Vector3f(headCube.origin))
        assertFalse(headCube.rotated)
        assertEquals(1.0F, model.bone("head")!!.geometry!!.scale)
        assertEquals(7.0F, headCube.faces.getValue(Direction.NORTH).u1)

        val arms = model.bone("arms")!!.geometry!!.cubes
        assertEquals(Vector3f(42.5F, 0.0F, -5.0F), Vector3f(arms[0].rotation))
        assertEquals(Vector3f(-15.0F, 0.0F, 12.5F), Vector3f(arms[1].rotation))

        val antenna = model.bone("antenna")!!.geometry!!
        assertEquals(28.0F / 24.0F, antenna.scale, 0.001F)
        assertEquals(BoneGeometry.MAX_EXTENT, antenna.cubes.single().to.y(), 0.001F)
        assertFitted(model)
    }

    @Test
    fun `animations are converted`() {
        val model = TestModels.load(TestModels.ROBOT)
        val wave = model.animation("look_around")!!
        assertEquals(AnimationLoop.Loop, wave.loop)
        assertEquals(0.5F, wave.startDelay)
        assertEquals(1.0F, wave.length)

        val head = model.bone("head")!!
        assertTrue(wave.affects(head.uuid))
        val timeline = wave.timeline(head.uuid)!!
        assertTrue(timeline.isConstant)
        assertNull(timeline.position)
        assertNotNull(timeline.rotation)

        val scope = MolangScope()
        // bbv4 negates rot x/y
        assertEquals(-45.0F, timeline.rotation.sample(0.25F, scope).y, 0.001F)
        assertEquals(-90.0F, timeline.rotation.sample(0.5F, scope).y, 0.001F)
        assertEquals(-45.0F, timeline.rotation.sample(0.75F, scope).y, 0.001F)
        assertEquals(KeyframeInterpolation.Linear, timeline.rotation.keyframes[0].interpolation)

        val antenna = wave.timeline(model.bone("antenna")!!.uuid)!!
        assertFalse(antenna.isConstant)
        scope.animtime = 0.25F
        // bbv4 negates pos x
        assertEquals(-1.0F, antenna.position!!.sample(0.0F, scope).x, 0.001F)

        assertEquals(
            listOf(
                EffectKeyframe.Sound(0.25F, Identifier.withDefaultNamespace("block.note_block.pling")),
                EffectKeyframe.Command(0.5F, "say hi")
            ),
            wave.effects
        )
    }

    @Test
    fun `pigeon loads`() {
        val model = TestModels.load(TestModels.PIGEON)
        assertEquals(1, model.roots.size)
        assertEquals("root", model.roots[0].name)
        assertEquals(9, model.cubeCount())
        assertNull(model.bone("root")!!.geometry)
        assertTrue(model.bone("head")!!.has(BoneTag.HEAD))
        assertEquals(model.bone("body"), model.bone("head")!!.parent)
        assertEquals("pigeon", model.textures[0].name)
        assertNotNull(model.animation("animation.idle"))
        assertFitted(model)
    }

    @Test
    fun `sunflower loads`() {
        val model = TestModels.load(TestModels.SUNFLOWER)
        assertEquals(1, model.roots.size)
        assertEquals(10, model.cubeCount())
        assertEquals(
            setOf("leaves", "stem", "head", "mouth1", "mouth2", "mouth3", "leaf_1", "leaf_2", "leaf_3", "leaf_4", "root"),
            model.bones().map { it.name }.toSet()
        )
        assertTrue(model.bone("head")!!.has(BoneTag.HEAD))
        assertTrue(model.bone("mouth1")!!.has(BoneTag.HEAD_CHILD))
        assertTrue(model.bone("leaf_1")!!.geometry!!.cubes.all { it.rotated })
        assertEquals(4, model.animations().size)
        assertTrue(model.bounds.height > 1.0F)
        assertFitted(model)
    }

    @Test
    fun `format 5 animations are not flipped`() {
        val model = TestModels.load(TestModels.SUNFLOWER)
        val stem = model.bone("stem")!!
        val plant = model.animation("animation_sunflower.plant")!!
        val rotation = plant.timeline(stem.uuid)!!.rotation!!.sample(0.5F, MolangScope())
        assertEquals(25.0F, rotation.x, 0.001F)
        val position = plant.timeline(stem.uuid)!!.position!!.sample(0.0F, MolangScope())
        assertEquals(-18.0F, position.y, 0.001F)
    }

    @Test
    fun `static bones are built around the parent pivot`() {
        val model = TestModels.load(TestModels.SUNFLOWER)
        val mouth = model.bone("mouth1")!!
        val head = model.bone("head")!!
        assertNotNull(mouth.geometry)
        assertTrue(Vector3f().sub(mouth.pivot).equals(mouth.geometry.offset, 0.0F))
        assertTrue(Vector3f().equals(head.geometry!!.offset, 0.0F))
        val cube = mouth.geometry.cubes.single { it.faces.size == 6 }
        assertEquals(8.0F - 2.11F, cube.from.z(), 0.001F)

        val poses = model.poses()
        ModelPoser(model).pose(ModelAnimator(model), MolangScope(), 1.0F, poses)
        val display = Matrix4f(poses.getValue(mouth.uuid).matrix()).translate(mouth.geometry.offset).scale(mouth.geometry.scale)
        val corner = Vector3f(cube.from).sub(8.0F, 8.0F, 8.0F).div(16.0F).mulPosition(display)
        assertEquals(3.11F / 16, corner.z, 0.0001F)
        assertEquals(9.0F / 16, corner.y, 0.0001F)
    }

    @Test
    fun `back faces are opt in`() {
        val model = TestModels.load(TestModels.PIGEON)
        val cubes = model.bone("left_leg")!!.geometry!!.cubes
        assertTrue(cubes.all { it.faces.size == 6 })
    }

    @Test
    fun `flat cubes are double sided`() {
        val model = TestModels.load(TestModels.SUNFLOWER, ModelLoadOptions(addBackfaces = true))
        val petals = model.bone("head")!!.geometry!!.cubes.single { it.from.z() == it.to.z() }
        val north = petals.faces.getValue(Direction.NORTH)
        val south = petals.faces.getValue(Direction.SOUTH)
        assertEquals(north.u1, south.u0)
        assertEquals(north.u0, south.u1)
        assertEquals(north.v0, south.v0)
        assertEquals(north.v1, south.v1)

        val stem = model.bone("stem")!!.geometry!!.cubes.single()
        assertNotEquals(stem.faces.getValue(Direction.NORTH).u0, stem.faces.getValue(Direction.SOUTH).u1)
    }

    @Test
    fun `see-through cubes get back faces`() {
        val model = TestModels.load("pigeon", ModelLoadOptions(addBackfaces = true))
        val cubes = model.bone("left_leg")!!.geometry!!.cubes
        val leg = cubes.single { it.faces.size == 6 }
        val backs = cubes - leg
        assertTrue(backs.isNotEmpty())
        for (back in backs) {
            val (direction, face) = back.faces.entries.single()
            val axis = direction.axis.ordinal
            assertEquals(back.from[axis], back.to[axis])
            val front = leg.faces.getValue(direction.opposite)
            if (direction.axis == Direction.Axis.Y) {
                assertEquals(front.v0, face.v1)
                assertEquals(front.v1, face.v0)
            } else {
                assertEquals(front.u0, face.u1)
                assertEquals(front.u1, face.u0)
            }
        }
    }

    @Test
    fun `load options override bounds and tags`() {
        val bounds = ModelBounds(3.0F, 4.0F)
        val options = ModelLoadOptions(
            boundsOverride = bounds,
            boneTagResolver = BoneTagResolver.None
        )
        val model = TestModels.load("robot", options)
        assertEquals(bounds, model.bounds)
        assertFalse(model.bone("head")!!.has(BoneTag.HEAD))
    }

    @Test
    fun `models without animations load`() {
        val json = """
        {
          "meta": {"format_version": "5.0"},
          "name": "inline",
          "resolution": {"width": 16, "height": 16},
          "elements": [],
          "textures": [],
          "groups": [{"name": "bone", "uuid": "$BONE", "origin": [0, 0, 0]}],
          "outliner": [{"uuid": "$BONE", "children": []}]
        }
        """.trimIndent()
        assertTrue(TestModels.loadInline(json).animations().isEmpty())
    }

    @Test
    fun `bounds only cover rendered cubes`() {
        val json = """
        {
          "meta": {"format_version": "4.10"},
          "elements": [
            {"name": "a", "uuid": "00000000-0000-0000-0000-00000000000a", "from": [0, 0, 0], "to": [16, 16, 16], "inflate": 1, "faces": $FACE},
            {"name": "b", "uuid": "00000000-0000-0000-0000-00000000000b", "from": [0, 0, 0], "to": [64, 64, 64], "faces": $FACE}
          ],
          "textures": [$TEXTURE],
          "outliner": [
            {"name": "body", "uuid": "00000000-0000-0000-0000-000000000001", "children": ["00000000-0000-0000-0000-00000000000a"]},
            {"name": "hitbox", "uuid": "00000000-0000-0000-0000-000000000002", "children": ["00000000-0000-0000-0000-00000000000b"]}
          ]
        }
        """.trimIndent()
        assertEquals(ModelBounds(18.0F / 16, 18.0F / 16), TestModels.loadInline(json).bounds)
    }

    @Test
    fun `cubes without textured faces are dropped`() {
        val json = """
        {
          "meta": {"format_version": "5.0"},
          "elements": [
            {"name": "a", "uuid": "00000000-0000-0000-0000-00000000000a", "from": [0, 0, 0], "to": [1, 1, 1], "faces": {"north": {"uv": [0, 0, 1, 1], "texture": null}}}
          ],
          "groups": [{"name": "bone", "uuid": "$BONE", "origin": [0, 0, 0]}],
          "outliner": [{"uuid": "$BONE", "children": ["00000000-0000-0000-0000-00000000000a"]}]
        }
        """.trimIndent()
        assertNull(TestModels.loadInline(json).bone("bone")!!.geometry)
    }

    @Test
    fun `meshes are rejected`() {
        val json = """{"meta":{"format_version":"4.10"},"elements":[{"type":"mesh","uuid":"a","name":"m"}]}"""
        assertThrows<ModelFormatException> { TestModels.loadInline(json) }
    }

    @Test
    fun `ease in out keyframes are eased`() {
        val model = TestModels.loadInline(animated(""""easing":"easeInOutQuad""""))
        val channel = model.animations().single().timeline(BONE)!!.position!!
        assertEquals(10.0F * 0.125F, channel.sample(0.25F, MolangScope()).y, 0.001F)
    }

    @Test
    fun `ease in keyframes are eased`() {
        val model = TestModels.loadInline(animated(""""easing":"easeInQuad""""))
        val channel = model.animations().single().timeline(BONE)!!.position!!
        assertEquals(10.0F * 0.0625F, channel.sample(0.25F, MolangScope()).y, 0.001F)
    }

    @Test
    fun `second data point is the post value`() {
        val model = TestModels.loadInline(animated(null, first = """{"x":0,"y":0,"z":0},{"x":0,"y":5,"z":0}"""))
        val channel = model.animations().single().timeline(BONE)!!.position!!
        assertEquals(7.5F, channel.sample(0.5F, MolangScope()).y, 0.001F)
    }

    private fun assertFitted(model: ModelDefinition) {
        for (bone in model.bones()) {
            val geometry = bone.geometry ?: continue
            for (cube in geometry.cubes) {
                for (i in 0..2) {
                    assertTrue(cube.from[i] >= BoneGeometry.MIN_EXTENT && cube.to[i] <= BoneGeometry.MAX_EXTENT, "${bone.name} out of bounds")
                }
                for (face in cube.faces.values) {
                    assertTrue(face.u0 in 0.0F..16.0F && face.v1 in 0.0F..16.0F)
                }
            }
        }
    }

    private companion object {
        val BONE: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

        // Single opaque white pixel and a face mapped onto it
        const val TEXTURE = """{"name": "white", "source": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGP4////fwAJ+wP9KobjigAAAABJRU5ErkJggg=="}"""
        const val FACE = """{"north": {"uv": [0, 0, 1, 1], "texture": 0}}"""

        // A format 5 model with a single bone whose position y goes from 0 at t=0 to 10 at t=1
        fun animated(
            secondExtra: String?,
            first: String = """{"x":0,"y":0,"z":0}""",
            firstExtra: String? = null
        ): String {
            val firstFields = if (firstExtra != null) ",$firstExtra" else ""
            val secondFields = if (secondExtra != null) ",$secondExtra" else ""
            return """
            {
              "meta": {"format_version": "5.0"},
              "name": "inline",
              "resolution": {"width": 16, "height": 16},
              "elements": [],
              "textures": [],
              "groups": [{"name": "bone", "uuid": "$BONE", "origin": [0, 0, 0]}],
              "outliner": [{"uuid": "$BONE", "children": []}],
              "animations": [{
                "name": "move", "length": 1,
                "animators": {
                  "$BONE": {"name": "bone", "type": "bone", "keyframes": [
                    {"channel": "position", "time": 0, "data_points": [$first]$firstFields},
                    {"channel": "position", "time": 1, "data_points": [{"x":0,"y":10,"z":0}]$secondFields}
                  ]}
                }
              }]
            }
            """.trimIndent()
        }
    }
}
