/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation

import kotlinx.coroutines.Job
import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.pose.BoneTransform
import net.casual.arcade.model.definition.ModelAnimation
import net.casual.arcade.model.definition.ModelNode
import net.minecraft.util.Mth
import kotlin.math.max

// TODO: Effect keyframes
public class ModelAnimationInstance(
    private val animation: ModelAnimation
) {
    private var age = 0
    private var delayRemaining = this.animation.startDelay

    private var fadeInTotal = 0
    private var fadeOutTotal = 0
    private var fadeOutRemaining = 0

    private var speed: Float = 1.0F
    private var weight: Float = 1.0F

    private val completion = Job()

    public val name: String
        get() = this.animation.name

    public val loop: AnimationLoop
        get() = this.animation.loop

    public val override: Boolean
        get() = this.animation.override

    public val stopped: Boolean
        get() = this.state == State.Fading || this.state == State.Finished

    public val finished: Boolean
        get() = this.state == State.Finished

    public var time: Float = 0.0F
        private set

    public var state: State = if (this.delayRemaining > 0.0F) State.Delayed else State.Playing
        private set

    public var loops: Int = 0
        private set

    public fun pause() {
        if (this.state == State.Playing || this.state == State.Delayed) {
            this.state = State.Paused
        }
    }

    public fun resume() {
        if (this.state == State.Paused) {
            this.state = if (this.delayRemaining > 0.0F) State.Delayed else State.Playing
        }
    }

    public fun stop(fade: Int = 0) {
        if (this.state == State.Finished || this.state == State.Fading) {
            return
        }
        if (fade <= 0) {
            this.finish()
            return
        }
        this.state = State.Fading
        this.fadeOutRemaining = fade
        this.fadeOutTotal = fade
    }

    public fun seek(time: Float) {
        this.time = time.coerceIn(0.0F, this.animation.length)
    }

    public fun weight(): Float {
        var weight = this.weight
        if (this.fadeInTotal > 0 && this.age < this.fadeInTotal) {
            weight *= this.age.toFloat() / this.fadeInTotal
        }
        if (this.state == State.Fading && this.fadeOutTotal > 0) {
            weight *= max(0.0F, this.fadeOutRemaining.toFloat() / this.fadeOutTotal)
        }
        return weight
    }

    public suspend fun awaitFinish() {
        this.completion.join()
    }

    internal fun tick() {
        this.age++
        when (this.state) {
            State.Paused, State.Finished, State.Held -> {}
            State.Fading -> {
                this.advance()
                if (--this.fadeOutRemaining <= 0) {
                    this.finish()
                }
            }
            State.Delayed -> {
                this.delayRemaining -= this.speed / 20.0F
                if (this.delayRemaining <= Mth.EPSILON) {
                    this.delayRemaining = 0.0F
                    this.state = State.Playing
                }
            }
            State.Playing -> this.advance()
        }
    }

    internal fun affects(node: ModelNode): Boolean {
        return !this.finished && this.animation.affects(node.uuid)
    }

    internal fun sample(node: ModelNode, scope: MolangScope, dest: BoneTransform): BoneTransform {
        val timeline = this.animation.timeline(node.uuid) ?: return dest.identity()
        scope.animtime = this.time
        return timeline.sample(this.time, scope, dest)
    }

    private fun advance() {
        val length = this.animation.length
        val previous = this.time
        var current = previous + this.speed / 20.0F
        if (current < length || length <= 0.0F) {
            this.time = current
            return
        }

        when (this.loop) {
            AnimationLoop.Once -> {
                this.time = length
                if (this.state != State.Fading) {
                    this.stop()
                }
            }
            AnimationLoop.Hold -> {
                this.time = length
                if (this.state != State.Fading) {
                    this.state = State.Held
                }
            }
            AnimationLoop.Loop -> {
                this.loops++
                current -= length
                this.time = current
                if (this.animation.loopDelay > 0.0F && this.state != State.Fading) {
                    this.delayRemaining = this.animation.loopDelay
                    this.state = State.Delayed
                }
            }
        }
    }

    private fun finish() {
        this.state = State.Finished
        this.completion.complete()
    }

    public enum class State {
        Delayed, Playing, Paused, Held, Fading, Finished
    }
}