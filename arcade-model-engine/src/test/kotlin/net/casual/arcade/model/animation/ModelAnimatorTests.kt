package net.casual.arcade.model.animation

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.casual.arcade.model.TestModels
import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.pose.BonePose
import net.casual.arcade.model.animation.pose.BoneTransform
import net.casual.arcade.model.animation.pose.ModelPoser
import net.casual.arcade.model.animation.timeline.EffectKeyframe
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode
import net.casual.arcade.model.isCollapsed
import net.casual.arcade.model.poses
import net.casual.arcade.model.tick
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import java.util.*
import kotlin.time.Duration.Companion.seconds

class ModelAnimatorTests {
    private val model = TestModels.load(TestModels.ROBOT)
    private val head = this.model.bone("head")!!
    private val scope = MolangScope()

    @Test
    fun `start delay then playback then looping`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around")!!
        assertEquals(ModelAnimationInstance.State.Delayed, instance.state)
        animator.tick(10)
        assertEquals(ModelAnimationInstance.State.Playing, instance.state)
        assertEquals(0.0F, instance.time, 0.0001F)

        animator.tick(10)
        assertEquals(0.5F, instance.time, 0.0001F)
        assertEquals(-90.0F, animator.sample(head).rotation.y, 0.001F)

        animator.tick(10)
        assertEquals(1, instance.loops)
        assertEquals(0.0F, instance.time, 0.0001F)
        assertTrue(animator.isPlaying("look_around"))
    }

    @Test
    fun `speed and weight scale the result`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around", ModelAnimationOptions(speed = 2.0F, weight = 0.5F))!!
        animator.tick(10)
        assertEquals(0.5F, instance.time, 0.0001F)
        assertEquals(-45.0F, animator.sample(head).rotation.y, 0.001F)
    }

    @Test
    fun `once animations finish and fade out`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around", ModelAnimationOptions(loop = AnimationLoop.Once, fadeOut = 4))!!
        animator.tick(30)
        assertEquals(ModelAnimationInstance.State.Fading, instance.state)
        assertFalse(instance.finished)
        animator.tick(4)
        assertTrue(instance.finished)
        assertTrue(animator.idle)
        runBlocking { withTimeout(1.seconds) { instance.awaitFinish() } }
    }

    @Test
    fun `fade in ramps weight once playback starts`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around", ModelAnimationOptions(fadeIn = 4))!!
        assertEquals(0.0F, instance.weight(), 0.0001F)
        animator.tick(10)
        assertEquals(0.0F, instance.weight(), 0.0001F)
        animator.tick(2)
        assertEquals(0.5F, instance.weight(), 0.0001F)
        animator.tick(2)
        assertEquals(1.0F, instance.weight(), 0.0001F)
    }

    @Test
    fun `effects fire once when crossed`() {
        val animator = ModelAnimator(model)
        val effects = ArrayList<EffectKeyframe>()
        animator.play("look_around", ModelAnimationOptions(loop = AnimationLoop.Once))
        repeat(30) { animator.tick { _, effect -> effects.add(effect) } }
        assertEquals(listOf(0.25F, 0.5F), effects.map { it.time })
        assertInstanceOf<EffectKeyframe.Sound>(effects[0])
        assertInstanceOf<EffectKeyframe.Command>(effects[1])
    }

    @Test
    fun `override animations replace lower priority ones`() {
        val animator = ModelAnimator(model)
        animator.play("look_around")
        animator.play("look_around")
        animator.tick(20)
        assertEquals(-90.0F, animator.sample(head).rotation.y, 0.001F)

        val overriding = ModelAnimator(model)
        overriding.replay("look_around", ModelAnimationOptions(priority = 1, override = true, weight = 0.5F))
        overriding.tick(20)
        assertEquals(-45.0F, overriding.sample(head).rotation.y, 0.001F)
    }

    @Test
    fun `paused animations do not advance`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around")!!
        animator.tick(15)
        animator.pause("look_around")
        assertEquals(ModelAnimationInstance.State.Paused, instance.state)
        animator.tick(10)
        assertEquals(0.25F, instance.time, 0.0001F)
        assertSame(instance, animator.play("look_around"))
        assertEquals(ModelAnimationInstance.State.Playing, instance.state)
    }

    @Test
    fun `poser composes hierarchy`() {
        val animator = ModelAnimator(model)
        animator.play("look_around")
        animator.tick(20)

        val poses = model.poses()
        ModelPoser(model).pose(animator, this.scope, 1.0F, poses)

        val position = poses.getValue(head.uuid).position()
        assertEquals(0.0F, position.x, 0.001F)
        assertEquals(11.0F / 16, position.y, 0.001F)
        assertEquals(0.0F, position.z, 0.001F)

        val mouth = poses.getValue(model.locator("mouth")!!.uuid).position()
        assertEquals(-3.0F / 16, mouth.x, 0.001F)
        assertEquals(12.0F / 16, mouth.y, 0.001F)
        assertEquals(0.0F, mouth.z, 0.001F)

        val scaled = model.poses()
        ModelPoser(model).pose(ModelAnimator(model), this.scope, 2.0F, scaled)
        assertEquals(22.0F / 16, scaled.getValue(head.uuid).position().y, 0.001F)
    }

    @Test
    fun `zero scale keyframes collapse bones`() {
        val sunflower = TestModels.load(TestModels.SUNFLOWER)
        val animator = ModelAnimator(sunflower)
        animator.play("animation_sunflower.produce")
        val poses = sunflower.poses()
        val poser = ModelPoser(sunflower)
        val mouth1 = sunflower.bone("mouth1")!!.uuid
        val mouth3 = sunflower.bone("mouth3")!!.uuid

        poser.pose(animator, this.scope, 1.0F, poses)
        assertFalse(poses.getValue(mouth1).isCollapsed())
        assertTrue(poses.getValue(mouth3).isCollapsed())

        // one second of start delay, then the mouths swap at 0.875s
        animator.tick(40)
        poser.pose(animator, this.scope, 1.0F, poses)
        assertTrue(poses.getValue(mouth1).isCollapsed())
        assertFalse(poses.getValue(mouth3).isCollapsed())
    }

    @Test
    fun `held animations fade out when stopped`() {
        val sunflower = TestModels.load(TestModels.SUNFLOWER)
        val animator = ModelAnimator(sunflower)
        val plant = animator.play("animation_sunflower.plant")!!
        animator.tick(80)
        assertEquals(ModelAnimationInstance.State.Held, plant.state)

        plant.stop(4)
        assertEquals(ModelAnimationInstance.State.Fading, plant.state)
        animator.tick(4)
        assertTrue(plant.finished)
        assertTrue(animator.playing().isEmpty())

        val produce = animator.play("animation_sunflower.produce")!!
        animator.tick(40)
        assertEquals(ModelAnimationInstance.State.Playing, produce.state)
        assertEquals(1.0F, animator.sample(sunflower.bone("mouth3")!!).scale.x, 0.001F)
    }

    @Test
    fun `stop all uses each animation's fade out`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around", ModelAnimationOptions(fadeOut = 4))!!
        animator.stop()
        assertEquals(ModelAnimationInstance.State.Fading, instance.state)
    }

    @Test
    fun `stopping by name uses the animation's fade out`() {
        val animator = ModelAnimator(model)
        val instance = animator.play("look_around", ModelAnimationOptions(fadeOut = 4))!!
        animator.stop("look_around")
        assertEquals(ModelAnimationInstance.State.Fading, instance.state)
        animator.stop("look_around", 0)
        assertEquals(ModelAnimationInstance.State.Fading, instance.state)

        val other = animator.play("look_around")!!
        animator.stop("look_around", 0)
        assertTrue(other.finished)
    }

    @Test
    fun `effect handlers can change animations while ticking`() {
        val animator = ModelAnimator(model)
        animator.play("look_around", ModelAnimationOptions(loop = AnimationLoop.Once))
        repeat(30) {
            animator.tick { _, _ ->
                animator.replay("look_around")
                animator.stop()
            }
        }
        assertTrue(animator.idle)
    }

    @Test
    fun `pose rotation ignores scale`() {
        val poses = model.poses()
        ModelPoser(model).pose(ModelAnimator(model), this.scope, 3.0F, poses)
        val rotation = poses.getValue(head.uuid).rotation()
        assertEquals(1.0F, rotation.lengthSquared(), 0.0001F)
    }

    private fun ModelAnimator.sample(node: ModelNode): BoneTransform {
        val transform = BoneTransform()
        this.animate(node, scope, transform)
        return transform
    }
}
