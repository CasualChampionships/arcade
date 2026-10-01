/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation

import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.pose.BoneTransform
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode
import net.casual.arcade.utils.error.RichResult
import org.joml.Vector3f
import java.util.PriorityQueue
import java.util.TreeSet

public class ModelAnimator(
    private val definition: ModelDefinition
) {
    private val instances = ArrayList<ModelAnimationInstance>()
    private val sample = BoneTransform()

    public val idle: Boolean
        get() = this.instances.isEmpty()

    public fun play(name: String): ModelAnimationInstance? {
        val existing = this.get(name)
        if (existing != null) {
            existing.resume()
            return existing
        }
        return this.replay(name)
    }

    public fun replay(name: String): ModelAnimationInstance? {
        val animation = this.definition.animation(name) ?: return null
        this.stop(name)
        val instance = ModelAnimationInstance(animation)
        this.instances.add(instance)
        return instance
    }

    public fun pause(name: String) {
        this.get(name)?.pause()
    }

    public fun resume(name: String) {
        this.get(name)?.resume()
    }

    public fun stop(name: String, fade: Int = 0): Boolean {
        val instance = this.get(name) ?: return false
        instance.stop(fade)
        return true
    }

    public fun stop() {
        for (instance in this.instances) {
            instance.stop()
        }
    }

    public fun get(name: String): ModelAnimationInstance? {
        return this.instances.firstOrNull { instance ->
            instance.name == name && !instance.stopped
        }
    }

    public fun isPlaying(name: String): Boolean {
        return this.get(name) != null
    }

    public fun playing(): List<ModelAnimationInstance> {
        return this.instances.filter { instance -> !instance.stopped }
    }

    public fun instances(): List<ModelAnimationInstance> {
        return this.instances.filter { instance -> !instance.finished }
    }

    internal fun animate(node: ModelNode, scope: MolangScope, dest: BoneTransform) {
        for (instance in this.instances) {
            if (!instance.affects(node)) {
                continue
            }
            val weight = instance.weight()
            if (weight <= 0.0F) {
                continue
            }
            val sample = instance.sample(node, scope, this.sample)
            if (instance.override) {
                dest.position.lerp(sample.position, weight)
                dest.rotation.lerp(sample.rotation, weight)
                dest.scale.lerp(sample.scale, weight)
            } else {
                dest.position.fma(weight, sample.position)
                dest.rotation.fma(weight, sample.rotation)
                dest.scale.mul(Vector3f(1.0F).lerp(sample.scale, weight))
            }
        }
    }

    internal fun tick() {
        val iterator = this.instances.iterator()
        while (iterator.hasNext()) {
            val instance = iterator.next()
            instance.tick()
            if (instance.finished) {
                iterator.remove()
            }
        }
    }
}